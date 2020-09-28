package ads.delivery

import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext
import doobie.hikari.HikariTransactor
import cats.effect.{IO, ExitCode, IOApp}
import ads.delivery.respository.StatsRepositoryImpl
import ads.delivery.server.Router
import ads.delivery.server.Server
import ads.delivery.config.AllConfigsImpl
import ads.delivery.respository.Migration
import ads.delivery.respository.Database
import ads.delivery.util.Tracing
import com.colisweb.tracing.core.TracingContextBuilder
import com.typesafe.scalalogging.Logger

object Main extends IOApp {

  private val logger = Logger("Main")

  override def run(args: List[String]): IO[ExitCode] = {
    val tsc = ConfigFactory.load
    val configs = new AllConfigsImpl(tsc)
    Migration.migrate(configs)

    val shouldUseNoOpTracer = System.getProperty("use_no_op_tracer") == "true"
    logger.debug(
      s"use_no_op_tracer property value is ${System.getProperty("use_no_op_tracer")}"
    )
    logger.debug(s"is no op tracer being used? = $shouldUseNoOpTracer")
    implicit val tracingContext: TracingContextBuilder[IO] =
      if (shouldUseNoOpTracer)
        Tracing.noOpTracingContext[IO].unsafeRunSync
      else
        Tracing.jaegarTracingContext[IO](configs).unsafeRunSync

    implicit val ec = ExecutionContext.global
    val database = new Database(configs)
    database.getTransactor.use { t: HikariTransactor[IO] =>
      val statsRepository = new StatsRepositoryImpl(t)
      val routes = new Router(statsRepository).routes
      Server.start[IO](routes, configs)
    }
  }
}
