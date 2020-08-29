package ads.delivery.adt

sealed trait Browser {
  def stringRep: String
}

object Browser {
  def fromString(str: String): Option[Browser] =
    str.toUpperCase match {
      case "CHROME"  => Some(Chrome)
      case "FIREFOX" => Some(FireFox)
      case "EDGE"    => Some(Edge)
      case "SAFARI"  => Some(Safari)
      case _         => None
    }
  def fromStringUnsafe(str: String): Browser =
    fromString(str).get

  def all: Set[Browser] = 
    Set(Chrome, FireFox, Edge, Safari)
}

object Chrome extends Browser {
  override def stringRep: String = "Chrome"
}
object FireFox extends Browser {
  override def stringRep: String = "FireFox"
}
object Edge extends Browser {
  override def stringRep: String = "Edge"
}
object Safari extends Browser {
  override def stringRep: String = "Safari"
}
