package model

import java.time.LocalDateTime
import adt.{Browser, OS}
import java.net.URL

case class Delivery(advertisementId: Int, deliveryId: Int, time: LocalDateTime,
    browser: Browser, os: OS, site: URL)