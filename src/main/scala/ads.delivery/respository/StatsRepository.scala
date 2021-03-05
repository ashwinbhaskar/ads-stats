package ads.delivery.respository

import ads.delivery.model._
import ads.delivery.Types._
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import ads.delivery.adt.Category
import natchez.Span

trait StatsRepository[F[_]] {

  def recordDelivery(d: Delivery)(implicit
      tracingContext: Span[F]
  ): FResult[F, Unit]

  def recordInstall(i: Install)(implicit
      tracingContext: Span[F]
  ): FResult[F, Unit]

  def recordClick(c: Click)(implicit
      tracingContext: Span[F]
  ): FResult[F, Unit]

  def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis
  )(implicit
      tracingContext: Span[F]
  ): FResult[F, Stats]

  def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis,
      categories: List[Category]
  )(implicit
      tracingContext: Span[F]
  ): FResult[F, List[CategorizedStats]]

}
