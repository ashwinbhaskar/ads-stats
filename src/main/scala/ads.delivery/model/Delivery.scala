package ads.delivery.model

import ads.delivery.adt.{Browser, OS, ZonedDateTimeWithMillis}
import java.net.URL

case class Delivery(advertisementId: Int, deliveryId: Int, time: ZonedDateTimeWithMillis,
    browser: Browser, os: OS, site: URL)