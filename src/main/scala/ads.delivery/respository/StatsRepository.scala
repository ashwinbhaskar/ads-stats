package ads.delivery.respository

import ads.delivery.model._
import ads.delivery.adt.Error
import ads.delivery.Types._
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import cats.effect.IO
import ads.delivery.adt.Category

trait StatsRepository {

  def recordDelivery(d: Delivery): IOResult[Unit]

  def recordInstall(i: Install): IOResult[Unit]

  def recordClick(c: Click): IOResult[Unit]

  def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis
  ): IOResult[Stats]

  def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis,
      categories: List[Category]
  ): IOResult[List[CategorizedStats]]

}
