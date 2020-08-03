package ads.delivery.repository

import scala.concurrent.ExecutionContext.global
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
import ads.delivery.model.Delivery
import ads.delivery.adt._
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import ads.delivery.adt.ZonedDateTimeWithMillis
import ads.delivery.model.Install
import ads.delivery.model.Click

class StatsRepositoryImplTest extends AnyFlatSpec with Matchers {

  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(global)

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
}
