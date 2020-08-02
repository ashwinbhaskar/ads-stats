package ads.delivery.json

import io.circe.{Encoder, Json}
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime

object Encoders {
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  implicit val encodeZonedDateTime: Encoder[ZonedDateTime] = 
    new Encoder[ZonedDateTime] {
        def apply(ldt: ZonedDateTime): Json = 
            Json.fromString(formatter.format(ldt))
    }
}