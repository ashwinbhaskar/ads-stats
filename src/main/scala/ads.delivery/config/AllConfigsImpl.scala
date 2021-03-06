package ads.delivery.config

import com.typesafe.config.Config

class AllConfigsImpl(config: Config) extends AllConfigs {
  override def getUrl: String = config.getString("db.url")

  override def getUser: String = config.getString("db.user")

  override def getPass: String = config.getString("db.password")

  override def getDriverClassName: String =
    config.getString("db.driver_class_name")

  override def getMaxThreads: Int = config.getInt("db.max_thread_pool")

  override def getPort: Int = config.getInt("server.port")

  override def getHost: String = config.getString("server.host")

  override def getServiceName: String = config.getString("tracer.service")

  override def getAgentHost: String = config.getString("tracer.agent.host")

  override def getAgentPort: Int = config.getInt("tracer.agent.port")

  override def getSamplingManagerHost: String =
    config.getString("tracer.agent.sampling_manager_host")

  override def getSamplingManagerPort: Int =
    config.getInt("tracer.agent.sampling_manager_port")
}
