package ads.delivery.server

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import org.http4s.dsl.io._
import ads.delivery.config.ServerConfig

object Server {
  def start[F[_]: ConcurrentEffect: Timer](
      routes: HttpRoutes[F],
      config: ServerConfig
  ): F[ExitCode] =
    BlazeServerBuilder[F]
      .withHttpApp(routes.orNotFound)
      .bindHttp(port = config.getPort, host = config.getHost)
      .serve
      .compile
      .lastOrError
}
