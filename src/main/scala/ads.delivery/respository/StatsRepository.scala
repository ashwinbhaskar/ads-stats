package ads.delivery.respository

import ads.delivery.model._
import ads.delivery.adt.Error
import ads.delivery.Types._
import ads.delivery.adt.ZonedDateTimeWithoutMillis
import cats.effect.IO

trait StatsRepository {

  def recordDelivery(d: Delivery): RepoResult[Unit]

  def recordInstall(i: Install): RepoResult[Unit]

  def recordClick(c: Click): RepoResult[Unit]

  def getStats(start: ZonedDateTimeWithoutMillis, end: ZonedDateTimeWithoutMillis): RepoResult[Stats]

  def getStats(start: ZonedDateTimeWithoutMillis, end: ZonedDateTimeWithoutMillis
    , categories: Map[String, String]): RepoResult[List[CategorizedStats]]

}
