package edp.davinci

import edp.davinci.module._

object ModuleInstance {
  lazy val modules = new ConfigurationModuleImpl
    with PersistenceModuleImpl
    with ActorModuleImpl
    with CalculationModuleImpl
    with BusinessModuleImpl
    with RoutesModuleImpl

  def getModule: ConfigurationModuleImpl with PersistenceModuleImpl with ActorModuleImpl with CalculationModuleImpl with BusinessModuleImpl with RoutesModuleImpl = {
    modules
  }

}
