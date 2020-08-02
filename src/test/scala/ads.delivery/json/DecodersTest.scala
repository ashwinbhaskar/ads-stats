package ads.delivery.json

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.Decoder
import io.circe.Json
import io.circe.parser.decode
import io.circe.generic.auto._
import scala.util.chaining._
import java.util.UUID
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.net.URL
import ads.delivery.json.Decoders._
import ads.delivery.adt.Browser
import ads.delivery.adt._
import ads.delivery.model.Interval
import ads.delivery.model.Click
import ads.delivery.model.Install
import ads.delivery.model.Delivery
import ads.delivery.model.Stats

class DecodersTest extends AnyFlatSpec with Matchers {
  "Click" should "get successfully decoded" in {
    val f = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val uuid1 = UUID.randomUUID
    val uuid2 = UUID.randomUUID
    val jsonStr = s"""
    {
        "deliveryId": "${uuid1.toString}",
        "clickId": "${uuid2.toString}",
        "time": "2018-01-07T18:32:34.201100+00:00"
    }
    """
    val decoded = decode[Click](jsonStr)

    decoded shouldEqual 
        Right(Click(uuid1, uuid2, ZonedDateTime.parse("2018-01-07T18:32:34.201100+00:00", f)
            .pipe(new ZonedDateTimeWithMillis(_))))
  }

  "Interval" should "get successfully decoded" in {
      val f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
      val start = "2018-01-07T14:30:00+0000"
      val end = "2018-01-07T18:20:00+0000"
      val jsonStr = s"""
      {
          "start": "$start",
          "end": "$end"
      }
      """
      val decoded = decode[Interval](jsonStr)

      decoded shouldEqual
        Right(Interval(ZonedDateTime.parse(start, f).pipe(new ZonedDateTimeWithoutMillis(_)),
            ZonedDateTime.parse(end, f).pipe(new ZonedDateTimeWithoutMillis(_))))
  }

  "Install" should "get successfully decoded" in {
    val f = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    val uuid1 = UUID.randomUUID
    val uuid2 = UUID.randomUUID
    val jsonStr = s"""
    {
        "installId": "${uuid1.toString}",
        "clickId": "${uuid2.toString}",
        "time": "2018-01-07T18:32:34.201100+00:00"
    }
    """
    val decoded = decode[Install](jsonStr)

    decoded shouldEqual 
        Right(Install(uuid1, uuid2, ZonedDateTime.parse("2018-01-07T18:32:34.201100+00:00", f)
            .pipe(new ZonedDateTimeWithMillis(_)))) 
  }

  "Delivery" should "get decoded successfully" in {
      val f = DateTimeFormatter.ISO_OFFSET_DATE_TIME
      val jsonStr = s"""
      {
          "advertisementId": 1,
          "deliveryId": 2,
          "time": "2018-01-07T18:32:34.201100+00:00",
          "browser": "Chrome",
          "os": "Android",
          "site": "https://www.foo.com"
      }
      """
      val decoded = decode[Delivery](jsonStr)

      decoded shouldEqual
        Right(Delivery(1, 2, ZonedDateTime.parse("2018-01-07T18:32:34.201100+00:00", f).pipe(new ZonedDateTimeWithMillis(_)), 
            Chrome, Android, new URL("https://www.foo.com")))
  }

  "Stats" should "get decoded successfully" in {
      val jsonStr = """
      {
          "deliveries": 1,
          "clicks": 2,
          "installs": 3
      }
      """
      val decoded = decode[Stats](jsonStr)

      decoded shouldEqual Right(Stats(1,2,3))
  }
}
