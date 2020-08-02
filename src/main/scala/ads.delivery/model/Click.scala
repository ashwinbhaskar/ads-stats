package ads.delivery.model

import java.util.UUID
import ads.delivery.adt.ZonedDateTimeWithMillis

case class Click(deliveryId: UUID, clickId: UUID, time: ZonedDateTimeWithMillis)