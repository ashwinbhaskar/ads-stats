package ads.delivery.model

import java.util.UUID
import java.time.ZonedDateTime

case class Install(installId: UUID, clickId: UUID, time: ZonedDateTime)