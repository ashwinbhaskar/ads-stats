package ads.delivery

import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext
import cats.effect.{IO, ExitCode, IOApp}
import ads.delivery.respository.StatsRepositoryImpl
import ads.delivery.server.Router
import ads.delivery.server.Server
import ads.delivery.config.AllConfigsImpl
import ads.delivery.respository.Migration
import ads.delivery.respository.Database
import ads.delivery.util.Tracing
import com.typesafe.scalalogging.Logger
import natchez.EntryPoint
import cats.effect.kernel.Resource

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
    val tracingContext: Resource[IO, EntryPoint[IO]] =
      if (shouldUseNoOpTracer)
        Tracing.noOpTracingContext[IO]
      else
        Tracing.jaegarTracingContext[IO](configs)

    implicit val ec = ExecutionContext.global
    val database = new Database[IO](configs)
    
    val routeResource = for {
      transactor <- database.getTransactor
      entryPoint <- tracingContext
    } yield {
      val statsRepository = new StatsRepositoryImpl[IO](transactor)
      new Router[IO](statsRepository, entryPoint).routes
    }

    routeResource.use(routes => Server.start[IO](routes, configs))

  }
}
