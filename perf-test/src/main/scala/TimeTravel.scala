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

object TimeTravel {
  def apply(preRequisite: PreRequisite): Unit = {
    val deliveryIdGen: Gen[ju.UUID] = Gen.uuid
    val deliveryGen: Gen[Delivery] = Generator.delivery
    val host = "http://127.0.0.1:8000"
    val recordDeliveryUrl = s"$host/ads/delivery"

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

  }
}
