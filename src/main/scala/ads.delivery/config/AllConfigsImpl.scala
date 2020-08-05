package ads.delivery.config

import com.typesafe.config.Config

class AllConfigsImpl(config: Config) extends AllConfigs {
  override def getUrl: String = config.getString("db.url")

  override def getUser: String = config.getString("db.user")

  override def getPass: String = config.getString("db.password")

  override def getDriverClassName: String =
    config.getString("db.driver_class_name")

  override def getMaxThreads: Int = config.getInt("db.thread_pool_size")

}
