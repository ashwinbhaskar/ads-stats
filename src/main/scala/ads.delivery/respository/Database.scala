package ads.delivery.respository

import scala.concurrent.ExecutionContext
import ads.delivery.config.DBConfig
import cats.effect.{Blocker, ContextShift, IO, Resource}
import doobie.hikari._
import doobie.util.ExecutionContexts

class Database(config: DBConfig)(implicit val ec: ExecutionContext) {

  private implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(ec)

  private lazy val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](config.getMaxThreads)
      be <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        config.getDriverClassName,
        config.getUrl,
        config.getUser,
        config.getPass,
        ce,
        be
      )
    } yield xa

  def getTransactor: Resource[IO, HikariTransactor[IO]] = transactor

}
