package ads.delivery.adt

sealed trait Error {
    def message: String
}