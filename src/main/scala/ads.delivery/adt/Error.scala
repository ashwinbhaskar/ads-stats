package ads.delivery.adt

sealed trait Error {
    def message: String
}

object AlreadyRecorded extends Error {
    override def message: String = "Delivery already recorded"
}

object UnhandledError extends Error {
    override def message: String = "Unhandled Error"
}