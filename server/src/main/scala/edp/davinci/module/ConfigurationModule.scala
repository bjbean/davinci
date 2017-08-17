package edp.davinci.module

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.PropertyConfigurator

trait ConfigurationModule {
  def config: Config
}

trait ConfigurationModuleImpl extends ConfigurationModule {
  private lazy val internalConfig: Config = {
    val dir: String = System.getProperty("DAVINCI_HOME")
    PropertyConfigurator.configure(s"$dir/conf/log4j.properties")
    val configDefaults = ConfigFactory.load(this.getClass.getClassLoader, "application.conf")
    if (dir != null) {
      ConfigFactory.parseFile(new File(dir + "/conf/application.conf")).withFallback(configDefaults)
    } else {
      configDefaults
    }
  }

  def config = internalConfig
}
