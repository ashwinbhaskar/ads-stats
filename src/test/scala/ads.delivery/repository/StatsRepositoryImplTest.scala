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
import com.typesafe.config.ConfigFactory
import java.net.URL
import java.util.UUID
import ads.delivery.respository.{Migration, StatsRepositoryImpl}
import ads.delivery.config.AllConfigsImpl
import ads.delivery.adt._
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import ads.delivery.adt.ZonedDateTimeWithMillis
import ads.delivery.model.{Install, Click, Delivery, Stats}

class StatsRepositoryImplTest extends AnyFlatSpec with Matchers {

    private val formatterWithoutMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    private val formatterWithMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(global)

  private def timeWithoutMS(t: String): ZonedDateTimeWithoutMillis = 
    OffsetDateTime.parse(t, formatterWithoutMillis).pipe(new ZonedDateTimeWithoutMillis(_))

    private def timeWithMS(t: String): ZonedDateTimeWithMillis = 
        OffsetDateTime.parse(t, formatterWithMillis).pipe(new ZonedDateTimeWithMillis(_))

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
            allConfigs.getPass)
    deleteQueries.map(_.update.run.transact(transactor).unsafeRunSync())
    testCode(transactor)
  }

  "Recording a delivery" should "be successfull" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val site = new URL("https://www.foo.com")
    val time = ZonedDateTimeWithMillis(OffsetDateTime.now)
    val delivery = Delivery(1, deliveryId, time, Chrome, Android, site)

    repository.recordDelivery(delivery).unsafeRunSync shouldBe ('Right)

    repository.recordDelivery(delivery).unsafeRunSync shouldEqual Left(AlreadyRecorded)
  }

  "Recording an install" should "be successfull" in withTransactor {t =>
    val repository = new StatsRepositoryImpl(t)
    val installId = UUID.fromString("4b7beeae-39d1-4207-a687-c173f8b00d2b")
    val clickId = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d2a")
    val time = ZonedDateTimeWithMillis(OffsetDateTime.now)
    val install = Install(installId, clickId, time)

    repository.recordInstall(install).unsafeRunSync should be ('Right)
  }

  "Recording a click" should "be successfull" in withTransactor {t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val clickId = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d2a")
    val time = ZonedDateTimeWithMillis(OffsetDateTime.now)
    val click = Click(deliveryId,clickId, time)

    repository.recordClick(click).unsafeRunSync should be ('Right)
  }

  "Recorded stats" should "be given out successfully" in withTransactor {t => 
    val repository = new StatsRepositoryImpl(t)
    val deliveryId1 = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val deliveryId2 = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2a")
    val deliveries = Seq(
        Delivery(1, deliveryId1, timeWithMS("2019-01-07T18:32:34.201100+00:00"), Chrome, Android, new URL("http://foo.com")),
        Delivery(2, deliveryId2, timeWithMS("2017-01-07T18:32:34.201100+00:00"), Chrome, IOS, new URL("http://goo.com"))
    )
    deliveries.foreach { d => repository.recordDelivery(d).unsafeRunSync }

    val clickId1 = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d3a")
    val clickId2 = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d4a")
    val clicks = Seq(
        Click(deliveryId1, clickId1, timeWithMS("2020-01-07T18:32:34.201100+00:00")),
        Click(deliveryId2, clickId2, timeWithMS("2019-01-07T18:32:34.201100+00:00"))
    )
    clicks.foreach{ c => repository.recordClick(c).unsafeRunSync }

    val installId1 = UUID.fromString("4b7beeae-39d1-4207-a687-c174f8b00d2b")
    val installId2 = UUID.fromString("4b7beeae-39d1-5207-a687-c174f8b00d2b")
    val installs = Seq(
        Install(installId1, clickId1, timeWithMS("2019-01-07T18:32:34.201100+00:00")),
        Install(installId2, clickId2, timeWithMS("2020-08-07T18:32:34.201100+00:00"))
    )
    installs.foreach{ i => repository.recordInstall(i).unsafeRunSync }

    val start = timeWithoutMS("2018-01-07T14:30:00+0000")
    val end = timeWithoutMS("2019-05-07T14:30:00+0000")

    repository.getStats(start, end).unsafeRunSync shouldEqual Right(Stats(1,1,1))

  }
}
