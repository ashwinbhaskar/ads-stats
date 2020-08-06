package ads.delivery.server

import scala.util.chaining._
import scala.util.Try
import cats.effect.IO
import cats.implicits._
import cats.data._
import io.circe.generic.auto._
import io.circe.Encoder
import io.circe.Decoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri, Response, Request}
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

class Router(repository: StatsRepository) extends Http4sDsl[IO] {

  private def collectCategories(p: Path): Either[Error, List[Category]] =
    p match {
      case parent / child =>
        Category.fromString(child) match {
          case Some(category) =>
            collectCategories(parent).map(list => category :: list)
          case None => Left(InvalidCategory)
          case None => Left(InvalidCategory)
          case None => Left(InvalidCategory)
        }
      case Root => Right(List.empty)
    }

  private def decodeBody[T: Decoder](
      req: Request[IO]
  ): IO[Either[JsonDecodingError, T]] =
    req
      .decodeJson[T]
      .redeem(
        t => new JsonDecodingError(t.getCause.getMessage).asLeft[T],
        _.asRight[JsonDecodingError]
      )
  private def decodeTime(t: String): EitherT[
    IO,
    InvalidDateTimeWithoutMillisFormat.type,
    OffsetDateTimeWithoutMillis
  ] =
    OffsetDateTimeWithoutMillis
      .fromString(t)
      .pipe(IO.fromTry)
      .redeem(
        _ =>
          InvalidDateTimeWithoutMillisFormat
            .asLeft[OffsetDateTimeWithoutMillis],
        _.asRight[InvalidDateTimeWithoutMillisFormat.type]
      )
      .pipe(EitherT.apply)

  private def timeDecoder(t: IO[OffsetDateTimeWithMillis]): IO[
    Either[InvalidDateTimeWithMillisFormat.type, OffsetDateTimeWithMillis]
  ] =
    t.redeem(
      _ => InvalidDateTimeWithMillisFormat.asLeft[OffsetDateTimeWithMillis],
      _.asRight[InvalidDateTimeWithMillisFormat.type]
    )

  private def toHttpResponse[T: Encoder](
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
    case req @ POST -> Root / "ads" / "delivery" =>
      val result = for {
        delivery <- EitherT(decodeBody[Delivery](req))
        r <- EitherT(repository.recordDelivery(delivery))
      } yield r

      result.value.flatMap(toHttpResponse(_, Created()))

    case req @ POST -> Root / "ads" / "install" =>
      val result = for {
        install <- EitherT(decodeBody[Install](req))
        r <- EitherT(repository.recordInstall(install))
      } yield r

      result.value.flatMap(toHttpResponse(_, Created()))

    case req @ POST -> Root / "ads" / "click" =>
      val result = for {
        click <- EitherT(decodeBody[Click](req))
        r <- EitherT(repository.recordClick(click))
      } yield r

      result.value.flatMap(toHttpResponse(_, Created()))

    case GET -> Root / "ads" / "statistics" / time / start / end / "overall" =>
      val result = for {
        startTime <- decodeTime(start)
        endTime <- decodeTime(end)
        stats <- EitherT(repository.getStats(startTime, endTime))
      } yield stats

      result.value.flatMap(r => toHttpResponse(r, Ok(r.asJson)))

    case GET -> "ads" /: "statistics" /: "time" /: start /: end /: categories =>
      val result = for {
        categ <- EitherT(IO(collectCategories(categories)))
        startTime <- decodeTime(start)
        endTime <- decodeTime(end)
        stats <- EitherT(repository.getStats(startTime, endTime, categ))
      } yield stats

      result.value.flatMap(r => toHttpResponse(r, Ok(r.asJson)))
  }
}
