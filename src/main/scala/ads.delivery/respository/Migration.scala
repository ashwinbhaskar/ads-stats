package ads.delivery.respository

import ads.delivery.config.DBConfig
import org.flywaydb.core.Flyway

object Migration {
  
    def migrate(config: DBConfig): Unit = {
        val flyway = Flyway.configure()
            .dataSource(config.getUrl,config.getUser, config.getPass)
            .load()
        flyway.migrate()
    }

}
