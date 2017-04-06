package edp.davinci

import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import edp.davinci.module._
import edp.davinci.persistence.entities._
import edp.davinci.rest._

object Boot extends App {
  // configuring modules for application, cake pattern for DI
  val modules = new ConfigurationModuleImpl
    with PersistenceModuleImpl
    with ActorModuleImpl
    with CalculationModuleImpl
    with BusinessModuleImpl
    with RoutesModuleImpl

  implicit val system = modules.system
  implicit val materializer = ActorMaterializer()
  implicit val ec = modules.system.dispatcher

  val host = modules.config.getString("httpServer.host")
  val port = modules.config.getInt("httpServer.port")

  // create table for suppliers if the table didn't exist (should be removed, when the database wasn't h2)
  modules.userDal.insert(User(0, "test@creditease.cn", "123456", "test", "haha", true, true, "2017-03-14 11:53:49", 1L, "2017-03-14 11:53:49", 1L))
  Http().bindAndHandle(new RoutesApi(modules).routes, host, port)
}
