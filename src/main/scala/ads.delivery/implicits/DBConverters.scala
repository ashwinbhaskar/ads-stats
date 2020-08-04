package ads.delivery.implicits

import ads.delivery.adt.{Browser, OS}
import ads.delivery.adt.OffsetDateTimeWithMillis
import ads.delivery.adt.OffsetDateTimeWithoutMillis
import doobie.util.meta.Meta
import doobie.implicits.javatime._
import java.net.URL
import java.sql.PreparedStatement
import java.time.OffsetDateTime

object DbConverters {

    implicit val browserConverter: Meta[Browser] = 
        Meta[String].timap(Browser.fromStringUnsafe)(_.stringRep)
    
    implicit val osConverter: Meta[OS] = 
        Meta[String].timap(OS.fromStringUnsafe)(_.stringRep)
  
    implicit val urlConverter: Meta[URL] = 
        Meta[String].timap(new URL(_))(_.toExternalForm)

    implicit val dateTimeWithMillisConverter = 
        Meta[OffsetDateTime].timap(new OffsetDateTimeWithMillis(_))(_.z)
    
    implicit val dateTimeWithoutMillisConverter = 
        Meta[OffsetDateTime].timap(new OffsetDateTimeWithoutMillis(_))(_.z)

    
}
