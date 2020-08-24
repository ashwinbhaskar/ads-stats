package request

object RecordDelivery extends Request {
  override def path: String = "ads/delivery"
  
  override def method: String = "post"
  
}
