package ads.delivery.repository

import scala.concurrent.ExecutionContext.global
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import doobie.util.fragment.Fragment
import cats.effect.{IO, ContextShift}
import cats.effect.Timer
import cats.effect.implicits._
import cats.effect.Bracket
import com.typesafe.config.ConfigFactory
import java.net.URL
import java.util.UUID
import ads.delivery.respository.{Migration, StatsRepositoryImpl}
import ads.delivery.config.AllConfigsImpl
import ads.delivery.adt._
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import ads.delivery.adt.OffsetDateTimeWithMillis
import ads.delivery.model.{Install, Click, Delivery, Stats}
import ads.delivery.model.CategorizedStats
import ads.delivery.util.Tracing
import ads.delivery.Types._
import com.colisweb.tracing.core.TracingContext
import com.colisweb.tracing.core.TracingContextBuilder
import com.colisweb.tracing.core.implicits._

class StatsRepositoryImplTest extends AnyFlatSpec with Matchers {

  private val formatterWithoutMillis =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private val formatterWithMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(global)

  implicit val timer: Timer[IO] =
    IO.timer(global)

  val tracingContextBuilder: TracingContextBuilder[IO] =
    Tracing.noOpTracingContext[IO].unsafeRunSync

  private def timeWithoutMS(t: String): OffsetDateTimeWithoutMillis =
    OffsetDateTime
      .parse(t, formatterWithoutMillis)
      .pipe(new OffsetDateTimeWithoutMillis(_))

  private def timeWithMS(t: String): OffsetDateTimeWithMillis =
    OffsetDateTime
      .parse(t, formatterWithMillis)
      .pipe(new OffsetDateTimeWithMillis(_))

  private val deleteQueries: Seq[Fragment] = Seq(
    sql"DELETE FROM delivery",
    sql"DELETE FROM install",
    sql"DELETE FROM click"
  )

  private def withTransactor(testCode: Transactor[IO] => Any) {
    val t = ConfigFactory.load("test.conf")
    val allConfigs = new AllConfigsImpl(t)
    Migration.migrate(allConfigs)
    val transactor: Aux[IO, Unit] =
      Transactor.fromDriverManager[IO](
        allConfigs.getDriverClassName,
        allConfigs.getUrl,
        allConfigs.getUser,
        allConfigs.getPass
      )
    deleteQueries.map(_.update.run.transact(transactor).unsafeRunSync())
    testCode(transactor)
  }

  "Recording a delivery" should "be successfull" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val site = new URL("https://www.foo.com")
    val time = OffsetDateTimeWithMillis(OffsetDateTime.now)
    val delivery = Delivery(1, deliveryId, time, Chrome, Android, site)

    val resource = tracingContextBuilder.build("foo-operation")
    resource
      .use[IO, Result[Unit]](tr => repository.recordDelivery(delivery)(tr))
      .unsafeRunSync shouldBe ('Right)
    resource
      .use[IO, Result[Unit]](tr => repository.recordDelivery(delivery)(tr))
      .unsafeRunSync shouldEqual Left(
      AlreadyRecorded
    )
  }

  "Recording an install" should "be successfull" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val installId = UUID.fromString("4b7beeae-39d1-4207-a687-c173f8b00d2b")
    val clickId = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d2a")
    val time = OffsetDateTimeWithMillis(OffsetDateTime.now)
    val install = Install(installId, clickId, time)

    val resource = tracingContextBuilder.build("foo-operation")
    resource
      .use[IO, Result[Unit]](tr => repository.recordInstall(install)(tr))
      .unsafeRunSync should be('Right)
  }

  "Recording a click" should "be successfull" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val clickId = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d2a")
    val time = OffsetDateTimeWithMillis(OffsetDateTime.now)
    val click = Click(deliveryId, clickId, time)

    val resource = tracingContextBuilder.build("foo-operation")
    resource
      .use[IO, Result[Unit]](tr => repository.recordClick(click)(tr))
      .unsafeRunSync should be('Right)
  }

  "Recorded stats" should "be given out successfully" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId1 = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val deliveryId2 = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2a")
    val deliveries = Seq(
      Delivery(
        1,
        deliveryId1,
        timeWithMS("2019-01-07T18:32:34.201100+00:00"),
        Chrome,
        Android,
        new URL("http://foo.com")
      ),
      Delivery(
        2,
        deliveryId2,
        timeWithMS("2017-01-07T18:32:34.201100+00:00"),
        Chrome,
        IOS,
        new URL("http://goo.com")
      )
    )

    val resource = tracingContextBuilder.build("foo-operation")
    deliveries.foreach(d =>
      resource
        .use[IO, Result[Unit]](tr => repository.recordDelivery(d)(tr))
        .unsafeRunSync
    )

    val clickId1 = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d3a")
    val clickId2 = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d4a")
    val clicks = Seq(
      Click(
        deliveryId1,
        clickId1,
        timeWithMS("2020-01-07T18:32:34.201100+00:00")
      ),
      Click(
        deliveryId2,
        clickId2,
        timeWithMS("2019-01-07T18:32:34.201100+00:00")
      )
    )
    clicks.foreach { c =>
      resource
        .use[IO, Result[Unit]](tr => repository.recordClick(c)(tr))
        .unsafeRunSync
    }

    val installId1 = UUID.fromString("4b7beeae-39d1-4207-a687-c174f8b00d2b")
    val installId2 = UUID.fromString("4b7beeae-39d1-5207-a687-c174f8b00d2b")
    val installs = Seq(
      Install(
        installId1,
        clickId1,
        timeWithMS("2019-01-07T18:32:34.201100+00:00")
      ),
      Install(
        installId2,
        clickId2,
        timeWithMS("2020-08-07T18:32:34.201100+00:00")
      )
    )
    installs.foreach { i =>
      resource
        .use[IO, Result[Unit]](tr => repository.recordInstall(i)(tr))
        .unsafeRunSync
    }

    val start = timeWithoutMS("2018-01-07T14:30:00+0000")
    val end = timeWithoutMS("2019-05-07T14:30:00+0000")

    resource
      .use[IO, Result[Stats]](tr => repository.getStats(start, end)(tr))
      .unsafeRunSync shouldEqual Right(
      Stats(1, 1, 1)
    )
  }

  "Stats" should "be successfully be categorized" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId1 = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val deliveryId2 = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2a")
    val deliveries = Seq(
      Delivery(
        1,
        deliveryId1,
        timeWithMS("2019-01-07T18:32:34.201100+00:00"),
        Chrome,
        Android,
        new URL("http://foo.com")
      ),
      Delivery(
        2,
        deliveryId2,
        timeWithMS("2017-01-07T18:32:34.201100+00:00"),
        Safari,
        IOS,
        new URL("http://goo.com")
      )
    )
    val resource = tracingContextBuilder.build("foo-operation")
    deliveries.foreach(d =>
      resource
        .use[IO, Result[Unit]](tr => repository.recordDelivery(d)(tr))
        .unsafeRunSync
    )

    val clickId1 = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d3a")
    val clickId2 = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d4a")
    val clicks = Seq(
      Click(
        deliveryId1,
        clickId1,
        timeWithMS("2020-01-07T18:32:34.201100+00:00")
      ),
      Click(
        deliveryId2,
        clickId2,
        timeWithMS("2019-01-07T18:32:34.201100+00:00")
      )
    )
    clicks.foreach { c =>
      resource
        .use[IO, Result[Unit]](tr => repository.recordClick(c)(tr))
        .unsafeRunSync
    }

    val installId1 = UUID.fromString("4b7beeae-39d1-4207-a687-c174f8b00d2b")
    val installId2 = UUID.fromString("4b7beeae-39d1-5207-a687-c174f8b00d2b")
    val installs = Seq(
      Install(
        installId1,
        clickId1,
        timeWithMS("2019-01-07T18:32:34.201100+00:00")
      ),
      Install(
        installId2,
        clickId2,
        timeWithMS("2020-08-07T18:32:34.201100+00:00")
      )
    )
    installs.foreach { i =>
      resource
        .use[IO, Result[Unit]](tr => repository.recordInstall(i)(tr))
        .unsafeRunSync
    }

    val start = timeWithoutMS("2017-01-07T14:30:00+0000")
    val end = timeWithoutMS("2021-05-07T14:30:00+0000")

    val expected = Set(
      CategorizedStats(Map(OSCategory -> "IOS"), Stats(1, 1, 1)),
      CategorizedStats(Map(OSCategory -> "Android"), Stats(1, 1, 1))
    )
    resource
      .use[IO, Result[List[CategorizedStats]]](tr =>
        repository
          .getStats(start, end, List(OSCategory))(tr)
      )
      .unsafeRunSync
      .map(_.toSet) shouldEqual Right(expected)
  }
}
