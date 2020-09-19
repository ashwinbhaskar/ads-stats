package ads.delivery.respository

import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.implicits._
import cats.effect.IO
import cats.implicits._
import ads.delivery.adt._
import ads.delivery.Types._
import ads.delivery.model.{Stats, Install, Delivery, Click, CategorizedStats}
import ads.delivery.implicits.DbConverters._
import java.time.OffsetDateTime
import com.colisweb.tracing.core.TracingContext

class StatsRepositoryImpl(transactor: Transactor[IO]) extends StatsRepository {

  private val ifInsertSuccess: Int => Either[Error, Unit] = { _ =>
    ().asRight[Error]
  }

  private val ifException: Throwable => Either[Error, Unit] = {
    case e: java.sql.SQLException if e.getSQLState == "23505" =>
      AlreadyRecorded.asLeft[Unit]
    case e: Exception =>
      println(s"Unknown exception: $e")
      UnhandledError.asLeft[Unit]
  }

  override def recordDelivery(
      d: Delivery
  )(implicit tracingContext: TracingContext[IO]): IOResult[Unit] =
    tracingContext.span("Record delivery repository") use { _ =>
      sql"INSERT INTO delivery(delivery_id, advertisement_id, t, browser, os, site) VALUES (${d.deliveryId}, ${d.advertisementId},${d.time},${d.browser},${d.os},${d.site})".update.run
        .transact(transactor)
        .redeem(ifException, ifInsertSuccess)
    }

  override def recordInstall(i: Install)(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[Unit] =
    tracingContext.span("Record install repository") use { _ =>
      sql"INSERT INTO install(install_id, click_id, t) VALUES(${i.installId}, ${i.clickId}, ${i.time})".update.run
        .transact(transactor)
        .redeem(ifException, ifInsertSuccess)
    }

  override def recordClick(c: Click)(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[Unit] =
    tracingContext.span("Record click repository") use { _ =>
      sql"INSERT INTO click(delivery_id, click_id, t) VALUES(${c.deliveryId}, ${c.clickId}, ${c.time})".update.run
        .transact(transactor)
        .redeem(ifException, ifInsertSuccess)
    }

  override def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis
  )(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[Stats] =
    tracingContext.span("Get stats repository") use { _ =>
      val result: IO[Stats] =
        for {
          deliveries <-
            sql"SELECT count(*) FROM delivery WHERE t BETWEEN $start AND $end"
              .query[Int]
              .to[List]
              .transact(transactor)
          installs <-
            sql"SELECT count(*) FROM install WHERE t BETWEEN $start AND $end"
              .query[Int]
              .to[List]
              .transact(transactor)
          clicks <-
            sql"SELECT count(*) FROM click WHERE t BETWEEN $start AND $end"
              .query[Int]
              .to[List]
              .transact(transactor)
        } yield Stats(deliveries.head, clicks.head, installs.head)
      result.redeem(
        e => UnhandledError.asLeft[Stats],
        stats => stats.asRight[Error]
      )
    }

  override def getStats(
      start: OffsetDateTimeWithoutMillis,
      end: OffsetDateTimeWithoutMillis,
      categories: List[Category]
  )(implicit
      tracingContext: TracingContext[IO]
  ): IOResult[List[CategorizedStats]] =
    tracingContext.span("Get categorized stats repository") use { _ =>
      val result: IO[List[CategorizedStats]] =
        for {
          deliveries <-
            sql"SELECT advertisement_id, delivery_id, t, browser, os, site FROM delivery WHERE t BETWEEN $start AND $end"
              .query[Delivery]
              .to[List]
              .transact(transactor)
          clicks <-
            sql"SELECT delivery_id, click_id, t FROM click WHERE t BETWEEN $start AND $end"
              .query[Click]
              .to[List]
              .transact(transactor)
          installs <-
            sql"SELECT install_id, click_id, t FROM install WHERE t BETWEEN $start AND $end"
              .query[Install]
              .to[List]
              .transact(transactor)
        } yield {
          val grouping: Map[List[String], List[Delivery]] =
            if (
              categories
                .contains(OSCategory) && categories.contains(BrowserCategory)
            )
              deliveries.groupBy(d => List(d.os.stringRep, d.browser.stringRep))
            else if (categories.contains(OSCategory))
              deliveries.groupBy(o => List(o.os.stringRep))
            else
              deliveries.groupBy(b => List(b.browser.stringRep))
          grouping.map {
            case (key, values) =>
              var deliveryCount, clickCount, installCount = 0
              for (v <- values) {
                deliveryCount += 1
                val cs = clicks.filter(c => c.deliveryId == v.deliveryId)
                clickCount += cs.length
                for (c <- cs) {
                  installCount += installs.count(i => i.clickId == c.clickId)
                }
              }
              val stats = Stats(deliveryCount, clickCount, installCount)
              key match {
                case List(f1, f2) =>
                  CategorizedStats(
                    Map(categories(0) -> f1, categories(1) -> f2),
                    stats
                  )
                case List(f1) =>
                  CategorizedStats(Map(categories.head -> f1), stats)
              }
          }.toList
        }
      result.redeem(
        _ => UnhandledError.asLeft[List[CategorizedStats]],
        cs => cs.asRight[Error]
      )
    }
}
