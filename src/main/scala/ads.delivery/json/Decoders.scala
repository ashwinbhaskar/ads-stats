package ads.delivery.json

import scala.util.Try
import scala.util.chaining._
import io.circe.{Decoder, Json, HCursor, DecodingFailure}
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.net.URL
import java.util.UUID
import ads.delivery.model.Interval
import ads.delivery.adt.Browser

object Decoders {
    private val formatterWithoutMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")

    private def tryToDecodeResult[T](z: Try[T]): Decoder.Result[T] = 
        z.toEither.left.map(e => DecodingFailure(e.getMessage, List.empty))

    implicit val decodeInterval: Decoder[Interval] = 
        new Decoder[Interval] {
            def apply(c: HCursor): Decoder.Result[Interval] = 
                for {
                    start <- c.downField("start").as[String]
                    end <- c.downField("end").as[String]
                    startZonedDateTime <- Try(ZonedDateTime.parse(start, formatterWithoutMillis))
                        .pipe(tryToDecodeResult)
                    endZonedDateTime <- Try(ZonedDateTime.parse(end, formatterWithoutMillis))
                        .pipe(tryToDecodeResult)
                    } yield
                        Interval(startZonedDateTime, endZonedDateTime)
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
            def apply(c: HCursor): Decoder.Result[UUID] =
                for {
                    uuidString <- c.value.as[String]
                    uuid <- Try(UUID.fromString(uuidString))
                        .pipe(tryToDecodeResult)
                } yield
                    uuid
        }
}
