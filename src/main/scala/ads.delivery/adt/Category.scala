package ads.delivery.adt

sealed trait Category {
  def stringRep: String
}

object Category {
  def fromString(s: String): Option[Category] =
    s.toUpperCase match {
      case "OS"      => Some(OSCategory)
      case "BROWSER" => Some(BrowserCategory)
      case _         => None
    }
}

object OSCategory extends Category {
  override def stringRep: String = "os"
}

object BrowserCategory extends Category {
  override def stringRep: String = "browser"
}
