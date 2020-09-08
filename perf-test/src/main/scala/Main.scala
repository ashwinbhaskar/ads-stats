import pureconfig.ConfigSource
import pureconfig.generic.auto._
import config.AdsStatsService
import model.TimeTravelData

object Main extends Object {
  
  def timeTravel(config: AdsStatsService) = {
    val deliveries = System.getenv("deliveries").toInt
    val deliveryToClickRatio = System.getenv("delivery_to_click_ratio").toFloat
    val clickToInstallRatio = System.getenv("click_to_install_ratio").toFloat
    val timeTravelData = TimeTravelData(deliveries, deliveryToClickRatio, clickToInstallRatio)
    AdsStatsRequests
    .timeTravel(config, timeTravelData)
    .unsafeRunSync
  }

  val conf = ConfigSource.default.load[AdsStatsService]
  assert(conf.isRight)
  conf.foreach { config =>
    val isTimeTravelMode = System.getenv("is_time_travel_mode").toBoolean
    if(isTimeTravelMode)
      timeTravel(config)
  }

}
