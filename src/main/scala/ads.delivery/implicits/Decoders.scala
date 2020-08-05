package ads.delivery.implicits

import scala.util.Try
import scala.util.chaining._
import io.circe.{Decoder, Json, HCursor, DecodingFailure}
import java.time.format.DateTimeFormatter
import java.net.URL
import java.util.UUID
import ads.delivery.model.{Interval, Click}
import ads.delivery.adt.Browser
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import ads.delivery.adt.OffsetDateTimeWithMillis
import ads.delivery.adt.OS
import java.time.OffsetDateTime
import ads.delivery.adt.Category

object Decoders {

  private def tryToDecodeResult[T](z: Try[T]): Decoder.Result[T] =
    z.toEither.left.map(e => DecodingFailure(e.getMessage, List.empty))

  implicit val decodeZonedDateTimeWithoutMillis
      : Decoder[OffsetDateTimeWithoutMillis] =
    new Decoder[OffsetDateTimeWithoutMillis] {
      def apply(c: HCursor): Decoder.Result[OffsetDateTimeWithoutMillis] =
        for {
          zonedDateTimeStr <- c.value.as[String]
          zonedDateTime <-
            OffsetDateTimeWithoutMillis
              .fromString(zonedDateTimeStr)
              .toEither
              .left
              .map(e => DecodingFailure(e.getMessage, List.empty))
        } yield zonedDateTime
    }

  implicit val decodeZonedDateTimeWithMillis
      : Decoder[OffsetDateTimeWithMillis] =
    new Decoder[OffsetDateTimeWithMillis] {
      def apply(c: HCursor): Decoder.Result[OffsetDateTimeWithMillis] =
        for {
          zonedDateTimeStr <- c.value.as[String]
          zonedDateTime <-
            OffsetDateTimeWithMillis
              .fromString(zonedDateTimeStr)
              .toEither
              .left
              .map(e => DecodingFailure(e.getMessage, List.empty))
        } yield zonedDateTime
    }

  implicit val decoderBrowser: Decoder[Browser] =
    new Decoder[Browser] {
      def apply(c: HCursor): Decoder.Result[Browser] =
        for {
          browserString <- c.value.as[String]
          browser <-
            Browser
              .fromString(browserString)
              .toRight(DecodingFailure("Invalid browser", List.empty))
        } yield browser
    }

  implicit val decodeURL: Decoder[URL] =
    new Decoder[URL] {
      def apply(c: HCursor): Decoder.Result[URL] =
        for {
          urlString <- c.value.as[String]
          url <- Try(new URL(urlString))
            .pipe(tryToDecodeResult)
        } yield url
    }

  implicit val decodeUUID: Decoder[UUID] =
    new Decoder[UUID] {
      def apply(c: HCursor): Decoder.Result[UUID] = {
        for {
          uuidString <- c.value.as[String]
          uuid <- Try(UUID.fromString(uuidString))
            .pipe(tryToDecodeResult)
        } yield uuid
      }
    }

  implicit val decodeOS: Decoder[OS] =
    new Decoder[OS] {
      def apply(c: HCursor): Decoder.Result[OS] =
        for {
          osString <- c.value.as[String]
          os <-
            OS.fromString(osString)
              .toRight(DecodingFailure("Invalid OS", List.empty))
        } yield os
    }

  implicit val decodeCategory: Decoder[Category] =
    new Decoder[Category] {
      def apply(c: HCursor): Decoder.Result[Category] =
        for {
          categoryString <- c.value.as[String]
          category <-
            Category
              .fromString(categoryString)
              .toRight(DecodingFailure("Invalid category", List.empty))
        } yield category
    }
}
