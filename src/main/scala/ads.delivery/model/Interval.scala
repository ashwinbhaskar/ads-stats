package ads.delivery.model

import ads.delivery.adt.ZonedDateTimeWithoutMillis

case class Interval(start: ZonedDateTimeWithoutMillis, end: ZonedDateTimeWithoutMillis)