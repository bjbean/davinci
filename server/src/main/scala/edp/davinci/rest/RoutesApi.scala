package edp.davinci.rest

import akka.http.scaladsl.server._
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.util.CorsSupport

class RoutesApi(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with CorsSupport {
  val swagger = new SwaggerRoutes
  val login = new LoginRoutes(modules)
  val changePwd = new ChangePwdRoutes(modules)
  val users = new UserRoutes(modules)
  val source = new SourceRoutes(modules)
  val bizlogic = new BizlogicRoutes(modules)
  //  val dashboard = new DashboardRoutes(modules)
  val widget = new WidgetRoutes(modules)
  //  val sql = new SqlRoutes(modules)
  //  val source = new SourceRoutes(modules)
  //  val libWidget = new LibWidgetRoutes(modules)
  //  val group = new GroupRoutes(modules)

  val routes: Route =
    corsHandler(swagger.routes) ~ corsHandler(swagger.indexRoute) ~
      pathPrefix("api" / "v1") {
        corsHandler(login.routes) ~
          corsHandler(users.routes) ~
          corsHandler(changePwd.routes) ~
          corsHandler(source.routes) ~
          corsHandler(bizlogic.routes) ~
          corsHandler(widget.routes)
        //          corsHandler(bizlogic.routes) ~
        //          corsHandler(dashboard.routes) ~
        //          corsHandler(widget.routes) ~
        //          corsHandler(sql.routes) ~
        //          corsHandler(source.routes) ~
        //          corsHandler(libWidget.routes) ~
        //          corsHandler(group.routes) ~

      }
}
