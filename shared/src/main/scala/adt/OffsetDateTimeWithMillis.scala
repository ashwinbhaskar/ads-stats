package ads.delivery.adt

import java.time.{OffsetDateTime, Instant, ZoneId}
import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.util.chaining._

case class OffsetDateTimeWithMillis(val z: OffsetDateTime) extends AnyVal

object OffsetDateTimeWithMillis {
  private val formatterWithMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME
  def fromString(s: String): Try[OffsetDateTimeWithMillis] =
    Try(OffsetDateTime.parse(s, formatterWithMillis))
      .map(o => OffsetDateTimeWithMillis(o))
  
  def fromEpochSeconds(epoch: Long): OffsetDateTimeWithMillis = 
    OffsetDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault)
      .pipe(OffsetDateTimeWithMillis.apply)
}
