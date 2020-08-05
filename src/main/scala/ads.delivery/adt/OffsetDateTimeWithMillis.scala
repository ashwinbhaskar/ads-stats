package ads.delivery.adt

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

case class OffsetDateTimeWithMillis(val z: OffsetDateTime) extends AnyVal

object OffsetDateTimeWithMillis {
  private val formatterWithMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  def fromString(s: String): Try[OffsetDateTimeWithMillis] =
    Try(OffsetDateTime.parse(s, formatterWithMillis))
      .map(o => OffsetDateTimeWithMillis(o))
}
