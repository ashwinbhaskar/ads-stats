import pureconfig.ConfigSource
import pureconfig.generic.auto._
import config.AdsStatsService
import model.TimeTravelData
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {

  def timeTravel(config: AdsStatsService): IO[ExitCode] = {
    val deliveries = System.getProperty("deliveries").toInt
    val deliveryToClickRatio = System.getProperty("delivery_to_click_ratio").toFloat
    val clickToInstallRatio = System.getProperty("click_to_install_ratio").toFloat
    val timeTravelData =
      TimeTravelData(deliveries, deliveryToClickRatio, clickToInstallRatio)
    AdsStatsRequests
      .timeTravel(config, timeTravelData)
      .parSequence
      .as(ExitCode.Success)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val isTimeTravelMode = System.getProperty("is_time_travel_mode").toBoolean
    println(s"Starting perf test in time travel mode? = $isTimeTravelMode")
    val conf = ConfigSource.default.load[AdsStatsService]
    conf match {
      case Right(c) => 
        if (isTimeTravelMode)
          timeTravel(c)
        else
          ???
      case Left(_) => IO.apply(ExitCode.Error)
    }
  }

}
