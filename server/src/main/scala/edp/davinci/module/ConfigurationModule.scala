/*-
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2017 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

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
    println("dir: " + dir)
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
