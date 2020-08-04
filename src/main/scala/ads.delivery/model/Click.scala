package ads.delivery.model

import java.util.UUID
import ads.delivery.adt.OffsetDateTimeWithMillis

case class Click(deliveryId: UUID, clickId: UUID, time: OffsetDateTimeWithMillis)