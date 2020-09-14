import model.TimeTravelData
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll
import ads.delivery.model.Delivery
import java.{util => ju}
import java.time.LocalTime
import org.scalacheck.Gen.Parameters
import org.scalacheck.rng.Seed
import ads.delivery.model.Click
import scala.util.chaining._
import ads.delivery.model.Install
import cats.effect.IO
import cats.implicits._
import config.AdsStatsService
import cats.effect.IOApp
import cats.effect.IO

object AdsStatsRequests {

  private def jsonize(delivery: Delivery): ujson.Obj =
    ujson.Obj(
      "advertisementId" -> delivery.advertisementId,
      "deliveryId" -> delivery.deliveryId.toString,
      "time" -> delivery.time.toString,
      "browser" -> delivery.browser.stringRep,
      "os" -> delivery.os.stringRep,
      "site" -> delivery.site.toExternalForm
    )

  private def jsonize(click: Click): ujson.Obj =
    ujson.Obj(
      "deliveryId" -> click.deliveryId.toString,
      "clickId" -> click.clickId.toString,
      "time" -> click.time.toString
    )

  private def jsonize(install: Install): ujson.Obj =
    ujson.Obj(
      "installId" -> install.installId.toString,
      "clickId" -> install.clickId.toString,
      "time" -> install.time.toString
    )

  private def prepareRequests(
      config: AdsStatsService,
      deliveries: List[Delivery],
      clicks: List[Click],
      installs: List[Install]
  ): List[IO[_]] = {
    println(s"size of deliveries = ${deliveries.size}")
    println(s"size of clicks = ${clicks.size}")
    println(s"size of installs = ${installs.size}")
    
    val host = s"http://${config.host}:${config.port}"
    val recordDeliveryUrl = s"$host/ads/delivery"
    val recordClickUrl = s"$host/ads/click"
    val recordInstallUrl = s"$host/ads/install"

    println(s"recordDeliveryURL = $recordDeliveryUrl")
    println(s"recordClickURL = $recordClickUrl")
    println(s"recordInstallURL = $recordInstallUrl")      
    val d: List[IO[_]] = deliveries.map(d =>
      IO.apply(
        requests.post(recordDeliveryUrl, data = jsonize(d)
      ))
    )
    val c: List[IO[_]] = 
      clicks.map(c => IO.apply(
        requests.post(recordClickUrl, data = jsonize(c))))

    val i: List[IO[_]] = 
      installs.map(i => IO.apply(
        requests.post(recordInstallUrl, data = jsonize(i))))
    
    d ++ c ++ i
  }

  def timeTravel(config: AdsStatsService, ttd: TimeTravelData): List[IO[_]] = {
    val noDeliveries = ttd.deliveries
    val deliveries: List[Delivery] = Generator
        .deliveries
        .take(noDeliveries)
        .toList

    val noOfClicksPerDelivery = (1 / ttd.deliveriesToClicksRatio)
      .pipe(Math.ceil(_))
      .pipe(_.toInt)
    val clicks: List[Click] = deliveries
      .map(d =>
        Generator
          .clicks(d.deliveryId)
          .take(noOfClicksPerDelivery)
          .toList
      )
      .flatten

    val noOfInstallsPerClick = (1 / ttd.clicksToInstallsRatio)
      .pipe(Math.ceil(_))
      .pipe(_.toInt)
    val installs: List[Install] =
      clicks
        .map(c =>
          Generator
            .installs(c.clickId)
            .take(noOfInstallsPerClick)
            .toList
        )
        .flatten

    prepareRequests(config, deliveries, clicks, installs)
  }
}
