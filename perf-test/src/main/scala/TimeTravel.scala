import model.PreRequisite
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

object TimeTravel {

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

  private def timeTravel(
      deliveries: List[Delivery],
      clicks: List[Click],
      installs: List[Install]
  ): IO[Unit] = {
    val host = "http://127.0.0.1:8000"
    val recordDeliveryUrl = s"$host/ads/delivery"
    val recordClickUrl = s"$host/ads/click"
    val recordInstallUrl = s"$host/ads/install"

    IO.apply {
      deliveries.foreach(d =>
        requests.post(recordDeliveryUrl, data = jsonize(d))
      )
      clicks.foreach(c => requests.post(recordClickUrl, data = jsonize(c)))
      installs.foreach(i => requests.post(recordInstallUrl, data = jsonize(i)))
    }
  }

  def apply(preRequisite: PreRequisite): IO[Unit] = {
    val noDeliveries = preRequisite.deliveries
    val deliveryParameters = Parameters.default.withSize(noDeliveries)
    val deliveries: List[Delivery] = Gen
      .listOf[Delivery](Generator.delivery)
      .pureApply(deliveryParameters, Seed.random)

    val c = (1 / preRequisite.deliveriesToClicksRatio)
      .pipe(Math.ceil(_))
      .pipe(_.toInt)
    val clickParameters = Parameters.default.withSize(c)
    val clicks: List[Click] = deliveries
      .map(d =>
        Gen
          .listOf[Click](Generator.click(d.deliveryId))
          .pureApply(clickParameters, Seed.random)
      )
      .flatten

    val i = (1 / preRequisite.clicksToInstallsRatio)
      .pipe(Math.ceil(_))
      .pipe(_.toInt)
    val installParameters = Parameters.default.withSize(i)
    val installs: List[Install] =
      clicks
        .map(c =>
          Gen
            .listOf[Install](Generator.install(c.clickId))
            .pureApply(installParameters, Seed.random)
        )
        .flatten

    timeTravel(deliveries, clicks, installs)
  }
}
