package ads.delivery.model

import ads.delivery.adt.OffsetDateTimeWithoutMillis

case class Interval(start: OffsetDateTimeWithoutMillis, end: OffsetDateTimeWithoutMillis)