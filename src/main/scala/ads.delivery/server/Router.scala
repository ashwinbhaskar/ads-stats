package ads.delivery.server

import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import ads.delivery.model.Delivery
import ads.delivery.implicits.Decoders._
import ads.delivery.respository.StatsRepository
import ads.delivery.model.Click
import ads.delivery.model.Install

class Router(repository: StatsRepository) extends Http4sDsl[IO] {
  val routes = HttpRoutes.of[IO] {
    case req @ POST -> Root / "delivery" =>
      for {
        delivery <- req.decodeJson[Delivery]
        _ <- repository.recordDelivery(delivery)
        response <- Ok()
      } yield response

    case req @ POST -> Root / "install" =>
      for {
        install <- req.decodeJson[Install]
        _ <- repository.recordInstall(install)
        response <- Ok()
      } yield response

    case req @ POST -> Root / "click" =>
      for {
        click <- req.decodeJson[Click]
        _ <- repository.recordClick(click)
        response <- Ok()
      } yield response
  }
}
