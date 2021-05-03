package ads.delivery.repository

import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.implicits._
import doobie.util.fragment.Fragment
import cats.effect.kernel.Resource
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import natchez.EntryPoint
import com.dimafeng.testcontainers.{PostgreSQLContainer, ForAllTestContainer}
import org.testcontainers.utility.DockerImageName
import java.net.URL
import java.util.UUID
import ads.delivery.respository.{Migration, StatsRepositoryImpl}
import ads.delivery.config.DBConfig
import ads.delivery.adt._
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import ads.delivery.adt.OffsetDateTimeWithMillis
import ads.delivery.model.{Install, Click, Delivery, Stats}
import ads.delivery.model.CategorizedStats
import ads.delivery.util.Tracing

class StatsRepositoryImplTest extends AnyFlatSpec with Matchers with ForAllTestContainer {

  private val formatterWithoutMillis =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  private val formatterWithMillis = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  val entryPoint: Resource[IO, EntryPoint[IO]] =
    Tracing.noOpTracingContext[IO]

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

  private def withTransactor(testCode: Transactor[IO] => Any) = {
    // Thread.sleep(20000)

    val dbConfig = new DBConfig {
      override def getUrl: String = container.jdbcUrl
      
      override def getUser: String = container.username
      
      override def getPass: String = container.password
      
      override def getDriverClassName: String = container.driverClassName
      
      override def getMaxThreads: Int = 10
      
    }
    Migration.migrate(dbConfig)
    val transactor: Aux[IO, Unit] =
      Transactor.fromDriverManager[IO](
        dbConfig.getDriverClassName,
        dbConfig.getUrl,
        dbConfig.getUser,
        dbConfig.getPass
      )
    deleteQueries.map(_.update.run.transact(transactor).unsafeRunSync())
    testCode(transactor)
  }

  override val container: PostgreSQLContainer =  
    PostgreSQLContainer(DockerImageName.parse("postgres:12"), "ads_stats", "postgres", "postgres")

  "Recording a delivery" should "be successfull" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val site = new URL("https://www.foo.com")
    val time = OffsetDateTimeWithMillis(OffsetDateTime.now)
    val delivery = Delivery(1, deliveryId, time, Chrome, Android, site)

    entryPoint
      .flatMap(_.root("Record Delivery"))
      .use(tr => repository.recordDelivery(delivery)(tr))
      .unsafeRunSync shouldBe ('Right)
    entryPoint
      .flatMap(_.root("Record Delivery"))
      .use(tr => repository.recordDelivery(delivery)(tr))
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

     entryPoint
      .flatMap(_.root("Record Delivery"))
      .use(tr => repository.recordInstall(install)(tr))
      .unsafeRunSync should be('Right)
  }

  "Recording a click" should "be successfull" in withTransactor { t =>
    val repository = new StatsRepositoryImpl(t)
    val deliveryId = UUID.fromString("4b7beead-32d1-4207-a687-c173f8b00d2b")
    val clickId = UUID.fromString("4b7beeae-39d1-4207-a687-c173f9b09d2a")
    val time = OffsetDateTimeWithMillis(OffsetDateTime.now)
    val click = Click(deliveryId, clickId, time)

    entryPoint
      .flatMap(_.root("Record Click"))
      .use(tr => repository.recordClick(click)(tr))
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

    deliveries.foreach(d =>
      entryPoint
        .flatMap(_.root("Record Delivery"))
        .use(tr => repository.recordDelivery(d)(tr))
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
       entryPoint
        .flatMap(_.root("Record Click"))
        .use(tr => repository.recordClick(c)(tr))
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
       entryPoint
        .flatMap(_.root("Record Install"))
        .use(tr => repository.recordInstall(i)(tr))
        .unsafeRunSync
    }

    val start = timeWithoutMS("2018-01-07T14:30:00+0000")
    val end = timeWithoutMS("2019-05-07T14:30:00+0000")

     entryPoint
      .flatMap(_.root("Get Stats"))
      .use(tr => repository.getStats(start, end)(tr))
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
    deliveries.foreach(d =>
       entryPoint
        .flatMap(_.root("Record Delivery"))
        .use(tr => repository.recordDelivery(d)(tr))
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
       entryPoint
        .flatMap(_.root("Record Click"))
        .use(tr => repository.recordClick(c)(tr))
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
       entryPoint
        .flatMap(_.root("Record Install"))
        .use(tr => repository.recordInstall(i)(tr))
        .unsafeRunSync
    }

    val start = timeWithoutMS("2017-01-07T14:30:00+0000")
    val end = timeWithoutMS("2021-05-07T14:30:00+0000")

    val expected = Set(
      CategorizedStats(Map(OSCategory -> "IOS"), Stats(1, 1, 1)),
      CategorizedStats(Map(OSCategory -> "Android"), Stats(1, 1, 1))
    )
     entryPoint
      .flatMap(_.root("Get Stats"))
      .use(tr =>
        repository
          .getStats(start, end, List(OSCategory))(tr)
      )
      .unsafeRunSync
      .map(_.toSet) shouldEqual Right(expected)
  }
}
