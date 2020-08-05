package ads.delivery.config

trait ServerConfig {
  def getPort: Int
  def getHost: String
}
