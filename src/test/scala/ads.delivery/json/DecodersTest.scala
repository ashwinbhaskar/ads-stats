package ads.delivery.json

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.Decoder
import io.circe.Json
import java.util.UUID
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import ads.delivery.json.Decoders._
import ads.delivery.adt.Browser
import ads.delivery.adt._
import ads.delivery.model.Interval

class DecodersTest extends AnyFlatSpec with Matchers {
  "UUID String" should "get successfully decoded" in {
    val uuid = UUID.randomUUID
    val uuidString = uuid.toString
    val decoded = implicitly[Decoder[UUID]].apply(Json.fromString(uuidString).hcursor)

    decoded shouldEqual Right(uuid)
  }

  "An invalid UUID String" should "give an error when decoded" in {
      val uuidString = "foo"
      val decoded = implicitly[Decoder[UUID]].apply(Json.fromString(uuidString).hcursor)
    
      decoded should be ('Left)
  }

  "A valid browser string" should "get decoded successfully" in {
      val browserStr = "Firefox"
      val decoded = implicitly[Decoder[Browser]].apply(Json.fromString(browserStr).hcursor)

      decoded shouldEqual Right(FireFox)
  }

  "An invalid browser string" should "give an error when decdoed" in {
      val browserStr = "FooBrowser"
      val decoded = implicitly[Decoder[Browser]].apply(Json.fromString(browserStr).hcursor)

      decoded should be ('Left)
  }

  "A json with valid interval values" should "get decoded successfully" in {
      val start = "2018-01-07T14:30:00+0000"
      val end = "2018-01-07T18:20:00+0000"
      val json = Json.obj(
          ("start", Json.fromString(start)),
          ("end", Json.fromString(end))
      )

      val decoded = implicitly[Decoder[Interval]].apply(json.hcursor)
    
      val f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
      decoded shouldEqual Right(Interval(ZonedDateTime.parse(start, f), ZonedDateTime.parse(end, f)))
  }

  "A json with invalid interval values" should "give an error" in {
      val start = "2018-01-07T14:30:00+0000"
      val end = "2018-01-sdfds"
      val json = Json.obj(
          ("start", Json.fromString(start)),
          ("end", Json.fromString(end))
      )

      val decoded = implicitly[Decoder[Interval]].apply(json.hcursor)
    
      val f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
      decoded should be ('Left)
  }
}
