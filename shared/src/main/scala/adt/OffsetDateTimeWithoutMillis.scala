package ads.delivery.adt

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.util.chaining._
import java.time.Instant
import java.time.ZoneId

case class OffsetDateTimeWithoutMillis(val z: OffsetDateTime) extends AnyVal

object OffsetDateTimeWithoutMillis {
  private val formatterWithoutMillis =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  def fromString(s: String): Try[OffsetDateTimeWithoutMillis] =
    Try(OffsetDateTime.parse(s, formatterWithoutMillis))
      .map(o => OffsetDateTimeWithoutMillis(o))

  def fromEpochSeconds(epoch: Long): OffsetDateTimeWithoutMillis =
    OffsetDateTime
      .ofInstant(Instant.ofEpochSecond(epoch), ZoneId.systemDefault)
      .pipe(OffsetDateTimeWithoutMillis.apply)
}
