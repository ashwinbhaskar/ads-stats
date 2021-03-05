package ads.delivery.respository

import scala.concurrent.ExecutionContext
import ads.delivery.config.DBConfig
import cats.effect.kernel.{Sync, Async, Resource}
import doobie.hikari._
import doobie.util.ExecutionContexts

class Database[F[_] : Sync : Async](config: DBConfig)(implicit val ec: ExecutionContext) {

  private lazy val transactor: Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](config.getMaxThreads)
      xa <- HikariTransactor.newHikariTransactor[F](
        config.getDriverClassName,
        config.getUrl,
        config.getUser,
        config.getPass,
        ce
      )
    } yield xa

  def getTransactor: Resource[F, HikariTransactor[F]] = transactor

}
