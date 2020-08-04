package ads.delivery.respository

import ads.delivery.model._
import ads.delivery.adt.Error
import ads.delivery.Types._
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import cats.effect.IO
import ads.delivery.adt.Category

trait StatsRepository {

  def recordDelivery(d: Delivery): RepoResult[Unit]

  def recordInstall(i: Install): RepoResult[Unit]

  def recordClick(c: Click): RepoResult[Unit]

  def getStats(start: OffsetDateTimeWithoutMillis, end: OffsetDateTimeWithoutMillis): RepoResult[Stats]

  def getStats(start: OffsetDateTimeWithoutMillis, end: OffsetDateTimeWithoutMillis
    , categories: List[Category]): RepoResult[List[CategorizedStats]]

}
