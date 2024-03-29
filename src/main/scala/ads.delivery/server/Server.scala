package ads.delivery.server

import cats.effect.ExitCode
import cats.effect.kernel.Async
import org.http4s.HttpRoutes
import org.http4s.syntax.kleisli._
import ads.delivery.config.ServerConfig
import scala.concurrent.ExecutionContext.global
import org.http4s.blaze.server.BlazeServerBuilder

object Server {
  def start[F[_]: Async](
      routes: HttpRoutes[F],
      config: ServerConfig
  ): F[ExitCode] =
    BlazeServerBuilder
      .apply[F](global)
      .withHttpApp(routes.orNotFound)
      .bindHttp(port = config.getPort, host = config.getHost)
      .serve
      .compile
      .lastOrError
}
