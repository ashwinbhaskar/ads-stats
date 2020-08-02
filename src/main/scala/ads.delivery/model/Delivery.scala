package ads.delivery.model

import java.time.ZonedDateTime
import ads.delivery.adt.{Browser, OS}
import java.net.URL

case class Delivery(advertisementId: Int, deliveryId: Int, time: ZonedDateTime,
    browser: Browser, os: OS, site: URL)