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

object Executor {
  def execute(
      concurrency: Int,
      timeToRunInSeconds: Int,
      badRequestPercentage: Float,
      fetchToInsertRatio: Float,
      preRequisite: TimeTravelData
  ): Unit = {
    val now = LocalTime.now
    val endTime = now.plusSeconds(timeToRunInSeconds)
    while (!LocalTime.now.isAfter(endTime)) {
      val host = "http://127.0.0.1:8000"
      val recordDeliveryUrl = s"$host/ads/delivery"
      val recordInstallUrl = s"$host/ads/install"
      val recordClickUrl = s"$host/ads/click"
      val getStats = s"$host/ads/statistics/time/%s/%s/overall"
      val getCategorizedStats = s"$host/ads/statistics/time/%s/%s/%s"
    }
  }
}
