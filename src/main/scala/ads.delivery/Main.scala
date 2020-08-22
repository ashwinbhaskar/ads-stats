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
import cats.effect.Timer

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val tsc = ConfigFactory.load
    val configs = new AllConfigsImpl(tsc)
    Migration.migrate(configs)

    implicit val tracingContext: TracingContextBuilder[IO] =
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
