package edp.davinci.module

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.PropertyConfigurator

trait ConfigurationModule {
  def config: Config
}

trait ConfigurationModuleImpl extends ConfigurationModule {
  val userDir: String = System.getProperty("user.dir")
  println("user dir " + userDir + "~~~~~~~~~~~~~~")
  PropertyConfigurator.configure(s"$userDir/conf/log4j.properties")

  private lazy val internalConfig: Config = {
//    val configDefaults = ConfigFactory.load(this.getClass.getClassLoader, "application.conf")
    ConfigFactory.parseFile(new File(userDir + "/conf/application.conf"))
  }

  def config = internalConfig
}
