package ads.delivery.model

import ads.delivery.adt.{Browser, OS, OffsetDateTimeWithMillis}
import java.net.URL
import java.util.UUID

case class Delivery(advertisementId: Int, deliveryId: UUID, time: OffsetDateTimeWithMillis,
    browser: Browser, os: OS, site: URL)