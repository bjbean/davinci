package edp.davinci.module

import akka.actor.ActorSystem

trait ActorModule {
  val system: ActorSystem
}

trait ActorModuleImpl extends ActorModule {
  this: ConfigurationModule =>
  val system = ActorSystem("davinciActorSystem", config)
}
