package model

import scala.concurrent.duration._

case class PerfTestData(
    deliveriesToClicksRatio: Float,
    clicksToInstallsRatio: Float,
    deliveriesToQueryRatio: Float
)
