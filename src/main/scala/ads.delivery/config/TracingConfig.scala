package ads.delivery.config

trait TracingConfig {
  def getServiceName: String
  def getAgentHost: String
  def getAgentPort: Int
}
