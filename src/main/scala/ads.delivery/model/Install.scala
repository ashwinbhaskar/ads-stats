package ads.delivery.model

import java.util.UUID
import ads.delivery.adt.OffsetDateTimeWithMillis

case class Install(
    installId: UUID,
    clickId: UUID,
    time: OffsetDateTimeWithMillis
)
