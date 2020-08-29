import java.time.LocalTime
import model.PreRequisite
import org.scalacheck.Gen

object Executor {
  def execute(
      concurrency: Int,
      timeToRunInSeconds: Int,
      badRequestPercentage: Float,
      fetchToInsertRatio: Float,
      preRequisite: PreRequisite
): Unit = {
    prepare(preRequisite)
    val now = LocalTime.now
    val endTime = now.plusSeconds(timeToRunInSeconds)
    while(!LocalTime.now.isAfter(endTime)) {
      val host = "http://127.0.0.1:8000"
      val recordDeliveryUrl = s"$host/ads/delivery"
      val recordInstallUrl = s"$host/ads/install"
      val recordClickUrl = s"$host/ads/click"
      val getStats = s"$host/ads/statistics/time/%s/%s/overall"
      val getCategorizedStats = s"$host/ads/statistics/time/%s/%s/%s"
    }
  }

  private def prepare(preRequisite: PreRequisite): Unit = {
    Gen.frequency((1 -> Generator.delivery))
    
  }
}
