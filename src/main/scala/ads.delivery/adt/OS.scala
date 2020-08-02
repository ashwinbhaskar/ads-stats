package ads.delivery.adt

sealed trait OS

object OS {
    def fromString(s: String): Option[OS] = 
        s.toUpperCase match {
            case "IOS" => Some(IOS)
            case "ANDROID" => Some(Android)
            case "WINDOWS" => Some(Windows)
            case _ => None
        }
}

object IOS extends OS
object Android extends OS
object Windows extends OS
