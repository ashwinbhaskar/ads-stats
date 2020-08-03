package ads.delivery.model

import ads.delivery.adt.{Browser, OS, ZonedDateTimeWithMillis}
import java.net.URL
import java.util.UUID

case class Delivery(advertisementId: Int, deliveryId: UUID, time: ZonedDateTimeWithMillis,
    browser: Browser, os: OS, site: URL)