package ads.delivery.adt

import java.time.format.DateTimeFormatter

sealed trait Error {
  def message: String
}

object AlreadyRecorded extends Error {
  override def message: String = "Delivery already recorded"
}

object InvalidDateTimeWithoutMillisFormat extends Error {
  override def message: String =
    "Invalid date time format. Correct format: yyyy-MM-dd'T'HH:mm:ssZ"
}

object InvalidDateTimeWithMillisFormat extends Error {
  private val format = DateTimeFormatter.ISO_OFFSET_DATE_TIME.toString
  override def message: String =
    s"Invalid date time format. Correct format: $format"
}

class JsonDecodingError(msg: String) extends Error {
  override def message: String = msg
}

object InvalidCategory extends Error {
  override def message: String =
    "Invalid category. Allowed categories are OS, Browser "
}

object UnhandledError extends Error {
  override def message: String = "Unhandled Error"
}
