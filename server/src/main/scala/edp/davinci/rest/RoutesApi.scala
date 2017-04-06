package edp.davinci.rest

import akka.http.scaladsl.server._
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.rest.bizlogic.BizlogicRoutes
import edp.davinci.rest.dashboard.DashboardRoutes
import edp.davinci.rest.group.GroupRoutes
import edp.davinci.rest.source.SourceRoutes
import edp.davinci.rest.sqllog.SqlLogRoutes
import edp.davinci.rest.user.UserRoutes
import edp.davinci.rest.widget.WidgetRoutes
import edp.davinci.util.CorsSupport

class RoutesApi(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with CorsSupport {
  val swagger = new SwaggerRoutes
  val login = new LoginRoutes(modules)
  val changePwd = new ChangePwdRoutes(modules)
  val users = new UserRoutes(modules)
  val source = new SourceRoutes(modules)
  val bizlogic = new BizlogicRoutes(modules)
  val dashboard = new DashboardRoutes(modules)
  val widget = new WidgetRoutes(modules)
  val libWidget = new LibWidgetRoutes(modules)
  val group = new GroupRoutes(modules)
  val sqlLog = new SqlLogRoutes(modules)

  val routes: Route =
    corsHandler(swagger.routes) ~ corsHandler(swagger.indexRoute) ~
      pathPrefix("api" / "v1") {
        corsHandler(login.routes) ~
          corsHandler(users.routes) ~
          corsHandler(changePwd.routes) ~
          corsHandler(source.routes) ~
          corsHandler(bizlogic.routes) ~
          corsHandler(widget.routes) ~
          corsHandler(dashboard.routes) ~
          corsHandler(widget.routes) ~
          corsHandler(libWidget.routes) ~
          corsHandler(group.routes) ~
          corsHandler(sqlLog.routes)
      }
}
