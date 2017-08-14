package edp.davinci

import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import edp.davinci.module._
import edp.davinci.rest._

object Boot extends App {
  // configuring modules for application, cake pattern for DI
  val modules = ModuleInstance.getModule

  implicit lazy val system = modules.system
  implicit lazy val materializer = ActorMaterializer()
  implicit lazy val ec = modules.system.dispatcher

  lazy val host = modules.config.getString("httpServer.host")
  lazy val port = modules.config.getInt("httpServer.port")
  Http().bindAndHandle(new RoutesApi(modules).routes, host, port)
}
