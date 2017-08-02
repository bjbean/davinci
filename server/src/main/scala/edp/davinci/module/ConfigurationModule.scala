package edp.davinci.module

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigurationModule {
  def config: Config
}

trait ConfigurationModuleImpl extends ConfigurationModule {
  private lazy val internalConfig: Config = {
    val configDefaults = ConfigFactory.load(this.getClass.getClassLoader,"application.conf")
    val dir: String = System.getenv("DAVINCI_HOME")
    //println(s"========= dir: ${dir}")
    if (dir != null) {
      //println(s"========= file path  ${dir}/application.conf")
      ConfigFactory.parseFile(new File(dir + "/conf/application.conf")).withFallback(configDefaults)
    } else {
      //println("========= dir is empty")
      configDefaults
    }
  }

  def config = internalConfig
}
