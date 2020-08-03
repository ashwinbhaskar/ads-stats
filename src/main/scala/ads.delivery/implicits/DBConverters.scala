package ads.delivery.implicits

import ads.delivery.adt.{Browser, OS}
import doobie.util.meta.Meta
import java.net.URL
import java.sql.PreparedStatement

object DbConverters {

    implicit val browserConverter: Meta[Browser] = 
        Meta[String].timap(Browser.fromStringUnsafe)(_.stringRep)
    
    implicit val osConverter: Meta[OS] = 
        Meta[String].timap(OS.fromStringUnsafe)(_.stringRep)
  
    implicit val urlConverter: Meta[URL] = 
        Meta[String].timap(new URL(_))(_.toExternalForm)
}
