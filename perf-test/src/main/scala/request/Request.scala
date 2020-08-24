package request

abstract class Request {
  def path: String
  def method: String
  def host: String = ""
  def port: Int = 1
  def url: String = s"https://$host:$port/$path"
  
}
