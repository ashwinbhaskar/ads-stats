package ads.delivery.respository

import ads.delivery.model._
import ads.delivery.adt.Error
import ads.delivery.Types._
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import ads.delivery.adt.Category
import cats.effect.IO
import com.colisweb.tracing.core.TracingContext

trait StatsRepository {

  def recordDelivery(d: Delivery)(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[Unit]

  def recordInstall(i: Install)(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[Unit]

  def recordClick(c: Click)(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[Unit]

  def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis
  )(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[Stats]

  def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis,
      categories: List[Category]
  )(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[List[CategorizedStats]]

}
