package ads.delivery

import com.typesafe.config.ConfigFactory
import ads.delivery.config.AllConfigsImpl
import ads.delivery.respository.Migration

object Main extends App {
  val tsc = ConfigFactory.load
  val allConfigs = new AllConfigsImpl(tsc)
  Migration.migrate(allConfigs)
}
