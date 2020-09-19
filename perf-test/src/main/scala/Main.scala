import pureconfig.ConfigSource
import pureconfig.generic.auto._
import config.AdsStatsService
import model.{TimeTravelData, PerfTestData}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import scala.concurrent.duration._
import cats.effect.concurrent.Semaphore

object Main extends IOApp {

  def timeTravel(config: AdsStatsService): IO[ExitCode] = {
    val deliveries = System.getProperty("deliveries").toInt
    val deliveryToClickRatio =
      System.getProperty("delivery_to_click_ratio").toFloat
    val clickToInstallRatio =
      System.getProperty("click_to_install_ratio").toFloat
    val timeTravelData =
      TimeTravelData(deliveries, deliveryToClickRatio, clickToInstallRatio)
    AdsStatsRequests
      .timeTravel(config, timeTravelData)
      .parSequence
      .as(ExitCode.Success)
  }

  def execute(tasks: LazyList[IO[_]], semaphore: Semaphore[IO]): IO[Unit] = {
    for {
      _ <- semaphore.acquire
      _ <- IO.apply{println("aquired semaphore")}
      _ <- tasks.parSequence
    } yield () 
  }

  def perfTest(config: AdsStatsService): IO[ExitCode] = {
    val deliveryToClickRatio =
      System.getProperty("delivery_to_click_ratio").toFloat
    val clickToInstallRatio =
      System.getProperty("click_to_install_ratio").toFloat
    val deliveryToQueryRatio =
      System.getProperty("delivery_to_query_ratio").toFloat
    
    val timeToRunInSeconds = 
      System.getProperty("running_time_in_seconds").toInt
    
    val perfTestData = PerfTestData(deliveryToClickRatio, clickToInstallRatio, deliveryToQueryRatio)

    val deadLine = timeToRunInSeconds.seconds.fromNow
    
    val m = 
      for {
        s <- Semaphore[IO](3)
        _ <- AdsStatsRequests
              .perfTest(config, perfTestData)
              .grouped(50)
              .map(l => execute(l, s))
              .takeWhile(_ => deadLine.hasTimeLeft)
              .toList
              .parSequence
              .void
      } yield ()

    m.as(ExitCode.Success)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val isPerfTestMode = System.getProperty("is_perf_test_mode").toBoolean
    if(!isPerfTestMode)
      IO.apply(ExitCode.Success)
    else {
    val isTimeTravelMode = System.getProperty("is_time_travel_mode").toBoolean
    println(s"Starting perf test in time travel mode? = $isTimeTravelMode")
    val conf = ConfigSource.default.load[AdsStatsService]
    conf match {
      case Right(c) =>
        if (isTimeTravelMode)
          timeTravel(c)
        else
          perfTest(c)
      case Left(_) => IO.apply(ExitCode.Error)
    }
  }
  }

}
