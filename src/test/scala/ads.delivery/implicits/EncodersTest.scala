package ads.delivery.implicits

import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.Encoder
import io.circe.syntax._
import io.circe.literal._
import io.circe.generic.auto._
import ads.delivery.adt._
import ads.delivery.model._
import ads.delivery.implicits.Encoders._
import java.util.UUID
import java.time.format.DateTimeFormatter
import java.net.URL
import java.time.OffsetTime
import java.time.OffsetDateTime

class EncodersTest extends AnyFlatSpec with Matchers {

  "Click" should "get successfully encoded" in {
    val f = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val uuid1 = UUID.fromString("7cdbd969-4511-4a64-bff1-752786c76f82")
    val uuid2 = UUID.fromString("59e0c9dd-d2e8-424d-b26e-b13d717c5b73")
    val zdt = OffsetDateTime.parse("2018-01-07T18:32:34.201100+00:00", f)
    val click = Click(uuid1, uuid2, new OffsetDateTimeWithMillis(zdt))

    val encoded = click.asJson
    val expectedJson = json"""
        {
            "deliveryId": "7cdbd969-4511-4a64-bff1-752786c76f82",
            "clickId": "59e0c9dd-d2e8-424d-b26e-b13d717c5b73",
            "time": "2018-01-07T18:32:34.2011Z"
        }
        """

    encoded shouldEqual expectedJson
  }

  "Interval" should "get successfully encoded" in {
    val f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    val start = OffsetDateTime
      .parse("2018-01-07T14:30:00+0000", f)
      .pipe(new OffsetDateTimeWithoutMillis(_))
    val end = OffsetDateTime
      .parse("2018-01-07T18:20:00+0000", f)
      .pipe(new OffsetDateTimeWithoutMillis(_))
    val interval = Interval(start, end)

    val encoded = interval.asJson
    val expectedJson = json"""
        {
            "start": "2018-01-07T14:30:00+0000",
            "end": "2018-01-07T18:20:00+0000"
        }
        """

    encoded shouldEqual expectedJson
  }

  "Install" should "get successfully encoded" in {
    val f = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val uuid1 = UUID.fromString("7cdbd969-4511-4a64-bff1-752786c76f82")
    val uuid2 = UUID.fromString("59e0c9dd-d2e8-424d-b26e-b13d717c5b73")
    val zdt = OffsetDateTime.parse("2018-01-07T18:32:34.201100+00:00", f)
    val click = Install(uuid1, uuid2, new OffsetDateTimeWithMillis(zdt))

    val encoded = click.asJson
    val expectedJson = json"""
        {
            "installId": "7cdbd969-4511-4a64-bff1-752786c76f82",
            "clickId": "59e0c9dd-d2e8-424d-b26e-b13d717c5b73",
            "time": "2018-01-07T18:32:34.2011Z"
        }
        """

    encoded shouldEqual expectedJson
  }

  "Delivery" should "get successfully encoded" in {
    val f = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val time = OffsetDateTime
      .parse("2018-01-07T18:32:34.201100+00:00", f)
      .pipe(new OffsetDateTimeWithMillis(_))
    val advertiseMentId =
      UUID.fromString("7cdbd969-4511-4a64-bff1-752786c76f82")
    val delivery = Delivery(
      1,
      advertiseMentId,
      time,
      Chrome,
      Android,
      new URL("https://www.foo.com")
    )

    val encoded = delivery.asJson
    val expectedJson = json"""
        {
          "advertisementId": 1,
          "deliveryId": "7cdbd969-4511-4a64-bff1-752786c76f82",
          "time": "2018-01-07T18:32:34.2011Z",
          "browser": "Chrome",
          "os": "Android",
          "site": "https://www.foo.com"
        }
        """

    encoded shouldEqual expectedJson
  }

  "Stats" should "get successfully encoded" in {
    val stats = Stats(1, 2, 3)

    val encoded = stats.asJson
    val expectedJson = json"""
        {
            "deliveries": 1,
            "clicks": 2,
            "installs": 3
        }
        """

    encoded shouldEqual expectedJson
  }
}
