import model.TimeTravelData
import ads.delivery.model.Delivery
import ads.delivery.model.Click
import scala.util.chaining._
import ads.delivery.model.Install
import config.AdsStatsService
import cats.effect.IO
import model.{PerfTestData, PostRequest, Request}
import fs2.Stream

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

  private def getRecordingUrls(
      config: AdsStatsService
  ): (String, String, String) = {
    val host = s"http://${config.host}:${config.port}"
    val recordDeliveryUrl = s"$host/ads/delivery"
    val recordClickUrl = s"$host/ads/click"
    val recordInstallUrl = s"$host/ads/install"
    (recordDeliveryUrl, recordClickUrl, recordInstallUrl)
  }

  private def prepareRequests(
      config: AdsStatsService,
      deliveries: List[Delivery],
      clicks: List[Click],
      installs: List[Install]
  ): List[IO[_]] = {
    val (recordDeliveryUrl, recordClickUrl, recordInstallUrl) =
      getRecordingUrls(config)

    val d: List[IO[_]] = deliveries.map(d =>
      IO.apply(requests.post(recordDeliveryUrl, data = jsonize(d)))
    )
    val c: List[IO[_]] =
      clicks.map(c =>
        IO.apply(requests.post(recordClickUrl, data = jsonize(c)))
      )

    val i: List[IO[_]] =
      installs.map(i =>
        IO.apply(requests.post(recordInstallUrl, data = jsonize(i)))
      )

    d ++ c ++ i
  }

  private def prepareRequests(
      config: AdsStatsService,
      deliveriesClicksInstalls: LazyList[(Delivery, List[Click], List[Install])]
  ): Stream[IO, Request[Unit]] = {
    val (recordDeliveryUrl, recordClickUrl, recordInstallUrl) =
      getRecordingUrls(config)

    println(s"recordDeliveryURL = $recordDeliveryUrl")
    println(s"recordClickURL = $recordClickUrl")
    println(s"recordInstallURL = $recordInstallUrl")

    val rs: LazyList[Request[Unit]] = deliveriesClicksInstalls.map {
      case (delivery, clicks, installs) =>
        val clicksRequests = clicks.map(c => new PostRequest(requests.post(recordClickUrl, data = jsonize(c))))
        val installsRequests = installs.map(i => new PostRequest(requests.post(recordInstallUrl, data = jsonize(i))))
        val deliveryRequests = List(new PostRequest(requests.post(recordDeliveryUrl, data = jsonize(delivery))))
        deliveryRequests ++ clicksRequests ++ installsRequests
    }.flatten

    Stream.fromIterator[IO](rs.iterator)
  }

  def timeTravel(config: AdsStatsService, ttd: TimeTravelData): List[IO[_]] = {
    val noDeliveries = ttd.deliveries
    val deliveries: List[Delivery] = Generator.deliveries
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

  def perfTest(
      config: AdsStatsService,
      perfTestData: PerfTestData
  ): Stream[IO, Request[Unit]] = {
    val noOfClicksPerDelivery = (1 / perfTestData.deliveriesToClicksRatio)
      .pipe(Math.ceil(_))
      .pipe(_.toInt)
    val noOfInstallsPerClick = (1 / perfTestData.clicksToInstallsRatio)
      .pipe(Math.ceil(_))
      .pipe(_.toInt)

    val deliveriesClicksInstalls
        : LazyList[(Delivery, List[Click], List[Install])] =
      Generator.deliveries.map { delivery =>
        val clicks: List[Click] = Generator
          .clicks(delivery.deliveryId)
          .take(noOfClicksPerDelivery)
          .toList
        val installs: List[Install] = clicks
          .map(click =>
            Generator.installs(click.clickId).take(noOfInstallsPerClick)
          )
          .flatten
          .toList
        (delivery, clicks, installs)
      }

    prepareRequests(config, deliveriesClicksInstalls)
  }
}
