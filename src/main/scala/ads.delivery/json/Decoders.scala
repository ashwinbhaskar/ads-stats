package ads.delivery.json

import scala.util.Try
import scala.util.chaining._
import io.circe.{Decoder, Json, HCursor, DecodingFailure}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.net.URL
import java.util.UUID
import ads.delivery.model.{Interval, Click}
import ads.delivery.adt.{Browser, ZonedDateTimeWithoutMillis, ZonedDateTimeWithMillis}
import ads.delivery.adt.OS

object Decoders {
    private val formatterWithoutMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    private val formatterWithMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    private def tryToDecodeResult[T](z: Try[T]): Decoder.Result[T] = 
        z.toEither.left.map(e => DecodingFailure(e.getMessage, List.empty))

    implicit val decodeZonedDateTimeWithoutMillis: Decoder[ZonedDateTimeWithoutMillis] = 
        new Decoder[ZonedDateTimeWithoutMillis]{
            def apply(c: HCursor): Decoder.Result[ZonedDateTimeWithoutMillis] = 
                for {
                    zonedDateTimeStr <- c.value.as[String]
                    zonedDateTime <- Try(ZonedDateTime.parse(zonedDateTimeStr, formatterWithoutMillis))
                        .pipe(tryToDecodeResult)
                        .map(new ZonedDateTimeWithoutMillis(_))
                } yield
                    zonedDateTime
        }

    implicit val decodeZonedDateTimeWithMillis: Decoder[ZonedDateTimeWithMillis] = 
        new Decoder[ZonedDateTimeWithMillis]{
            def apply(c: HCursor): Decoder.Result[ZonedDateTimeWithMillis] = 
                for {
                    zonedDateTimeStr <- c.value.as[String]
                    zonedDateTime <- Try(ZonedDateTime.parse(zonedDateTimeStr, formatterWithMillis))
                        .pipe(tryToDecodeResult)
                        .map(new ZonedDateTimeWithMillis(_))
                } yield
                    zonedDateTime
        }

    implicit val decoderBrowser: Decoder[Browser] =
        new Decoder[Browser] {
            def apply(c: HCursor): Decoder.Result[Browser] = 
                for {
                    browserString <- c.value.as[String]
                    browser <- Browser.fromString(browserString).toRight(DecodingFailure("Invalid browser", List.empty))
                } yield
                    browser
        }
    
    implicit val decodeURL: Decoder[URL] = 
        new Decoder[URL] {
            def apply(c: HCursor): Decoder.Result[URL] = 
                for {
                    urlString <- c.value.as[String]
                    url <- Try(new URL(urlString))
                        .pipe(tryToDecodeResult)
                } yield
                    url
        }
    
    implicit val decodeUUID: Decoder[UUID] = 
        new Decoder[UUID] {
            def apply(c: HCursor): Decoder.Result[UUID] = {
                for {
                    uuidString <- c.value.as[String]
                    uuid <- Try(UUID.fromString(uuidString))
                        .pipe(tryToDecodeResult)
                } yield
                    uuid
        }
    }

    implicit val decodeOS: Decoder[OS] = 
        new Decoder[OS] {
            def apply(c: HCursor): Decoder.Result[OS] = 
                for {
                    osString <- c.value.as[String]
                    os <- OS.fromString(osString).toRight(DecodingFailure("Invalid OS", List.empty))
                } yield
                    os
        }
}
