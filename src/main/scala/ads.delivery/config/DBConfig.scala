package ads.delivery.config

trait DBConfig {
  def getUrl: String
  def getUser: String
  def getPass: String
  def getDriverClassName: String
  def getMaxThreads: Int
}
