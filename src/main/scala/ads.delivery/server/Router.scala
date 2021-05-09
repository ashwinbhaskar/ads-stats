package ads.delivery.server

import scala.util.chaining._
import cats.implicits._
import cats.data._
import cats.effect.kernel.{Async, MonadCancel}
import io.circe.generic.auto._
import io.circe.Encoder
import io.circe.Decoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{Response, Request}
import ads.delivery.model.Delivery
import ads.delivery.implicits.Decoders._
import ads.delivery.implicits.Encoders._
import ads.delivery.respository.StatsRepository
import ads.delivery.model.Click
import ads.delivery.model.Install
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import ads.delivery.adt.Error
import ads.delivery.adt._
import ads.delivery.Types._
import ads.delivery.adt.{UnhandledError, JsonDecodingError}
import ads.delivery.adt.InvalidDateTimeWithoutMillisFormat
import org.http4s.HttpRoutes
import natchez.EntryPoint

class Router[F[_]: Async](
    repository: StatsRepository[F],
    entryPoint: EntryPoint[F]
) extends Http4sDsl[F] {

  private def collectCategories(p: Path): Either[Error, List[Category]] =
    p match {
      case parent / child =>
        Category.fromString(child) match {
          case Some(category) =>
            collectCategories(parent).map(list => category :: list)
          case None => Left(InvalidCategory)
        }
      case Root => Right(List.empty)
    }

  private def decodeBody[T: Decoder](
      req: Request[F]
  ): F[Either[JsonDecodingError, T]] =
    req
      .decodeJson[T]
      .redeem(
        t => new JsonDecodingError(t.getCause.getMessage).asLeft[T],
        _.asRight[JsonDecodingError]
      )

  private def decodeTime(t: String): EitherT[
    F,
    InvalidDateTimeWithoutMillisFormat.type,
    OffsetDateTimeWithoutMillis
  ] =
    OffsetDateTimeWithoutMillis
      .fromString(t)
      .pipe(MonadCancel[F].fromTry)
      .redeem(
        _ =>
          InvalidDateTimeWithoutMillisFormat
            .asLeft[OffsetDateTimeWithoutMillis],
        _.asRight[InvalidDateTimeWithoutMillisFormat.type]
      )
      .pipe(EitherT.apply)

  private def toHttpResponse[T: Encoder](
      r: Result[T],
      successResponse: => F[Response[F]]
  ): F[Response[F]] =
    r match {
      case Left(error) if error == UnhandledError =>
        InternalServerError(r.asJson)
      case Left(_)  => BadRequest(r.asJson)
      case Right(_) => successResponse
    }

  val routes = HttpRoutes.of[F] {
    case (GET -> Root / "ping") => Ok("pong")
    case (req @ POST -> Root / "ads" / "delivery") =>
      entryPoint.root("Delivery - Router").use { tcx =>
        val result = for {
          delivery <- EitherT(decodeBody[Delivery](req))
          r <- EitherT(repository.recordDelivery(delivery)(tcx))
        } yield r

        result.value.flatMap(toHttpResponse(_, Created()))
      }

    case (req @ POST -> Root / "ads" / "install") =>
      entryPoint.root("Install - Router").use { trx =>
        val result = for {
          install <- EitherT(decodeBody[Install](req))
          r <- EitherT(repository.recordInstall(install)(trx))
        } yield r

        result.value.flatMap(toHttpResponse(_, Created()))
      }

    case (req @ POST -> Root / "ads" / "click") =>
      entryPoint.root("Click - Router").use { tcx =>
        val result = for {
          click <- EitherT(decodeBody[Click](req))
          r <- EitherT(repository.recordClick(click)(tcx))
        } yield r

        result.value.flatMap(toHttpResponse(_, Created()))
      }

    case GET -> Root / "ads" / "statistics" / "time" / start / end / "overall" =>
      entryPoint.root("Overall stats - Router").use { tcx =>
        val result = for {
          startTime <- decodeTime(start)
          endTime <- decodeTime(end)
          stats <- EitherT(repository.getStats(startTime, endTime)(tcx))
        } yield stats

        result.value.flatMap(r => toHttpResponse(r, Ok(r.asJson)))
      }
    case GET -> "ads" /: "statistics" /: "time" /: start /: end /: categories =>
      entryPoint.root("Categorised stats - Router").use { tcx =>
        val result = for {
          categ <- EitherT(Async[F].delay(collectCategories(categories)))
          startTime <- decodeTime(start)
          endTime <- decodeTime(end)
          stats <- EitherT(
            repository.getStats(startTime, endTime, categ)(tcx)
          )
        } yield stats

        result.value.flatMap(r => toHttpResponse(r, Ok(r.asJson)))
      }
  }
}
