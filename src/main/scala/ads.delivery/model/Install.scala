package ads.delivery.model

import java.util.UUID
import ads.delivery.adt.ZonedDateTimeWithMillis

case class Install(installId: UUID, clickId: UUID, time: ZonedDateTimeWithMillis)