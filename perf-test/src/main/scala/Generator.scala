import org.scalacheck.Gen
import ads.delivery.adt.OffsetDateTimeWithMillis
import ads.delivery.adt.Browser
import ads.delivery.adt.OS
import java.net.URL
import ads.delivery.model.Delivery
import java.{util => ju}
import ads.delivery.model.Click
import ads.delivery.model.Install
object Generator {

  private def url: Gen[URL] =
    for {
      protocol <- Gen.oneOf[String](Seq("http", "https"))
      domain <- Gen.listOfN(20, Gen.alphaChar)
      t <- Gen.oneOf[String](Seq("com", "net", "gov", "in"))
    } yield new URL(s"$protocol://${domain.mkString}. $t")

  def delivery: Gen[Delivery] =
    for {
      id <- Gen.posNum[Int]
      deliveryId <- Gen.uuid
      time <- Gen.posNum[Long].map(OffsetDateTimeWithMillis.fromEpochSeconds)
      browser <- Gen.oneOf[Browser](Browser.all)
      os <- Gen.oneOf[OS](OS.all)
      site <- url
    } yield Delivery(id, deliveryId, time, browser, os, site)

  def deliveries: LazyList[Delivery] = {
    val generator = delivery
    def f: LazyList[Option[Delivery]] = generator.sample #:: f
    f.filter(_.isDefined).map(_.get)
  }

  def click(deliveryId: ju.UUID): Gen[Click] =
    for {
      deliveryId <- Gen.const[ju.UUID](deliveryId)
      clickId <- Gen.uuid
      time <- Gen.posNum[Long].map(OffsetDateTimeWithMillis.fromEpochSeconds)
    } yield Click(deliveryId, clickId, time)

  def clicks(deliveryId: ju.UUID): LazyList[Click] = {
    val generator = click(deliveryId)
    def f: LazyList[Option[Click]] = generator.sample #:: f
    f.filter(_.isDefined).map(_.get)
  }

  def install(clickId: ju.UUID): Gen[Install] =
    for {
      clickId <- Gen.const[ju.UUID](clickId)
      installId <- Gen.uuid
      time <- Gen.posNum[Long].map(OffsetDateTimeWithMillis.fromEpochSeconds)
    } yield Install(installId, clickId, time)

  def installs(clickId: ju.UUID): LazyList[Install] = {
    val generator = install(clickId)
    def f: LazyList[Option[Install]] = generator.sample #:: f
    f.filter(_.isDefined).map(_.get)
  }
}
