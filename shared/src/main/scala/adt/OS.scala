package ads.delivery.adt

sealed trait OS {
  def stringRep: String
}

object OS {
  def fromString(s: String): Option[OS] =
    s.toUpperCase match {
      case "IOS"     => Some(IOS)
      case "ANDROID" => Some(Android)
      case "WINDOWS" => Some(Windows)
      case _         => None
    }

  def fromStringUnsafe(s: String): OS =
    fromString(s).get
  
  def all: Set[OS] =
    Set(IOS, Android, Windows)
}

object IOS extends OS {
  override def stringRep: String = "IOS"
}
object Android extends OS {
  override def stringRep: String = "Android"
}
object Windows extends OS {
  override def stringRep: String = "Windows"
}
