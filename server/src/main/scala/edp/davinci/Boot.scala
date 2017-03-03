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
  modules.userDal.createTable()
  modules.groupDal.createTable()
  modules.relUserGroupDal.createTable()
  modules.userDal.insert(User(0, "122@qq.com", "123456", "test", "haha", true, true, "2016-12-02 11:53:49.0", 1L, "2016-12-02 11:53:49.0", 1L))
  modules.relUserGroupDal.insert(RelUserGroup(1, 1, 1, true, "2016-12-02 11:53:49.0", 1, "2016-12-02 11:53:49.0", 1))
  modules.relUserGroupDal.insert(RelUserGroup(1, 1, 2, true, "2016-12-02 11:53:49.0", 1, "2016-12-02 11:53:49.0", 1))
  modules.dashboardDal.createTable()
  modules.widgetDal.createTable()
  modules.relDashboardWidgetDal.createTable()
  modules.dashboardDal.insert(Dashboard(0, "test", "test", true, true, "2017-03-03 12:00:00", 1, "2017-03-03 12:00:00", 1))
  modules.widgetDal.insert(Widget(0, 1, 1, "test", "test", "insert", "i", true, true, "2017-03-03 12:00:00", 1, "2017-03-03 12:00:00", 1))
  modules.relDashboardWidgetDal.insert(RelDashboardWidget(0, 1, 1, 10, 10, 10, 10, true, "2017-03-03 12:00:00", 1, "2017-03-03 12:00:00", 1))
  //
  //  modules.bizlogicDal.createTable()
  //  modules.bizlogicDal.insert(Bizlogic(1, 1, 1, "biz", "***", true, "2016-11-24 11:11:11", 1, "2016-11-24 11:12:12", 1))
  //  modules.bizlogicDal.insert(Bizlogic(2, 1, 1, "bi", "***", true, "2016-11-25 11:11:11", 1, "2016-11-25 11:12:12", 1))

  modules.sqlDal.createTable()
  modules.sourceDal.createTable()
  //  modules.libWidgetDal.createTable()
  //  modules.groupDal.createTable()

  Http().bindAndHandle(new RoutesApi(modules).routes, host, port)
}
