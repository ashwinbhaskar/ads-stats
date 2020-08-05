package ads.delivery.server

import scala.util.chaining._
import cats.effect.IO
import io.circe.generic.auto._
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri, Response}
import ads.delivery.model.Delivery
import ads.delivery.implicits.Decoders._
import ads.delivery.implicits.Encoders._
import ads.delivery.respository.StatsRepository
import ads.delivery.model.Click
import ads.delivery.model.Install
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import ads.delivery.adt.Error
import ads.delivery.Types._
import ads.delivery.adt.UnhandledError

class Router(repository: StatsRepository) extends Http4sDsl[IO] {

  private val root = Root / "ads"

  def toHttpResponse[T: Encoder](
      r: Result[T],
      successResponse: => IO[Response[IO]]
  ): IO[Response[IO]] =
    r match {
      case Left(error) if error == UnhandledError =>
        InternalServerError(r.asJson)
      case Left(error) => BadRequest(r.asJson)
      case Right(data) => successResponse
    }

  val routes = HttpRoutes.of[IO] {
    case req @ POST -> root / "delivery" =>
      val result = for {
        delivery <- req.decodeJson[Delivery]
        r <- repository.recordDelivery(delivery)
      } yield r

      result.flatMap(toHttpResponse(_, Created()))

    case req @ POST -> root / "install" =>
      val result = for {
        install <- req.decodeJson[Install]
        r <- repository.recordInstall(install)
      } yield r

      result.flatMap(toHttpResponse(_, Created()))

    case req @ POST -> root / "click" =>
      val result = for {
        click <- req.decodeJson[Click]
        r <- repository.recordClick(click)
      } yield r
      
      result.flatMap(toHttpResponse(_, Created()))

    case GET -> root / start / end / "overall" =>
      val result = for {
        startTime <-
          OffsetDateTimeWithoutMillis
            .fromString(start)
            .pipe(IO.fromTry)
        endTime <-
          OffsetDateTimeWithoutMillis
            .fromString(end)
            .pipe(IO.fromTry)
        stats <- repository.getStats(startTime, endTime)
      } yield stats

      result.flatMap(r => toHttpResponse(r, Ok(r.asJson)))
  }
}
