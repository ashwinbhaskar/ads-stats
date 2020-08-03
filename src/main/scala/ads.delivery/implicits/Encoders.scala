package ads.delivery.implicits

import io.circe.{Encoder, Json}
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.net.URL
import ads.delivery.adt._

object Encoders {
  private val formatterWithoutMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private val formatterWithMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  implicit val encodeZonedDateTimeWithoutMillis: Encoder[ZonedDateTimeWithoutMillis] = 
    new Encoder[ZonedDateTimeWithoutMillis] {
        def apply(zdt: ZonedDateTimeWithoutMillis): Json = 
            Json.fromString(formatterWithoutMillis.format(zdt.z))
    }
  
  implicit val encodeZonedDateTImeWithMillis: Encoder[ZonedDateTimeWithMillis] = 
    new Encoder[ZonedDateTimeWithMillis] {
      def apply(zdt: ZonedDateTimeWithMillis): Json =
        Json.fromString(formatterWithMillis.format(zdt.z))
    }

  implicit val encodeBrowser: Encoder[Browser] = 
    new Encoder[Browser] {
      def apply(b: Browser): Json = 
        Json.fromString(b.stringRep)
    }

  implicit val encodeOS: Encoder[OS] = 
    new Encoder[OS] {
      def apply(a: OS): Json = 
        Json.fromString(a.stringRep)
    }
  
  implicit val encodeUUID: Encoder[UUID] = 
    new Encoder[UUID] {
      def apply(a: UUID): Json = 
        Json.fromString(a.toString)
    }
  
  implicit val encodeURL: Encoder[URL] = 
    new Encoder[URL] {
      def apply(a: URL): Json = 
        Json.fromString(a.toExternalForm)
    }
}