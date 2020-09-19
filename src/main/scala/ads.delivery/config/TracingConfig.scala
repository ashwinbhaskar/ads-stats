package ads.delivery.config

trait TracingConfig {
  def getServiceName: String
  def getAgentHost: String
  def getAgentPort: Int
  def getSamplingManagerHost: String
  def getSamplingManagerPort: Int
}
