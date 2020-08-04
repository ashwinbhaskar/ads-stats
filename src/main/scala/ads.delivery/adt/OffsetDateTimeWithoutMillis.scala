package ads.delivery.adt

import java.time.OffsetDateTime

case class OffsetDateTimeWithoutMillis(val z: OffsetDateTime) extends AnyVal
