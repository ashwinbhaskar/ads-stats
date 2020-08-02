package ads.delivery.adt

sealed trait Browser

object Browser {
    def fromString(str: String): Option[Browser] = 
            str.toUpperCase match {
                case "CHROME" => Some(Chrome)
                case "FIREFOX" => Some(FireFox)
                case "EDGE" => Some(Edge)
                case "SAFARI" => Some(Safari)
                case _ => None
            }
}

object Chrome extends Browser
object FireFox extends Browser
object Edge extends Browser
object Safari extends Browser
