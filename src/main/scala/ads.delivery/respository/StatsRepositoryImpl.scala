package ads.delivery.respository

import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.implicits.javatime._
import doobie.implicits.legacy.instant._
import cats.effect.IO
import cats.implicits._
import ads.delivery.Types
import ads.delivery.model.Click
import ads.delivery.Types
import ads.delivery.adt._
import ads.delivery.model.CategorizedStats
import ads.delivery.Types._
import ads.delivery.adt.ZonedDateTimeWithoutMillis
import ads.delivery.model.{Stats, Install, Delivery}
import ads.delivery.implicits.DbConverters._
import java.time.OffsetDateTime

class StatsRepositoryImpl(transactor: Transactor[IO]) extends StatsRepository {

    private val ifInsertSuccess: Int => Either[Error, Unit] = { _ => ().asRight[Error] }

    private val ifException: Throwable => Either[Error, Unit] = {
        case e: java.sql.SQLException if e.getSQLState == "23505" => AlreadyRecorded.asLeft[Unit]
        case e: Exception => 
            println(s"Unknown exception: $e")
            UnhandledError.asLeft[Unit]
    }

    override def recordDelivery(d: Delivery): RepoResult[Unit] =
        sql"INSERT INTO delivery(delivery_id, advertisement_id, t, browser, os, site) VALUES (${d.deliveryId}, ${d.advertisementId},${d.time.z},${d.browser},${d.os},${d.site})"
            .update
            .run
            .transact(transactor)
            .redeem(ifException, ifInsertSuccess)
    
    override def recordInstall(i: Install): RepoResult[Unit] = 
        sql"INSERT INTO install(install_id, click_id, t) VALUES(${i.installId}, ${i.clickId}, ${i.time})"
            .update
            .run
            .transact(transactor)
            .redeem(ifException, ifInsertSuccess)


    override def recordClick(c: Click): RepoResult[Unit] = 
        sql"INSERT INTO click(delivery_id, click_id, t) VALUES(${c.deliveryId}, ${c.clickId}, ${c.time})"
            .update
            .run
            .transact(transactor)
            .redeem(ifException, ifInsertSuccess)
        
    override def getStats(start: ZonedDateTimeWithoutMillis, end: ZonedDateTimeWithoutMillis)
        : RepoResult[Stats] = ???
    
    override def getStats(start: ZonedDateTimeWithoutMillis, end: ZonedDateTimeWithoutMillis,
        categories: Map[String,String]): RepoResult[List[CategorizedStats]] = ???
    

}
