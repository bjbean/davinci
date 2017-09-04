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

package edp.davinci.rest

import akka.http.scaladsl.server._
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.rest.dashboard.DashboardRoutes
import edp.davinci.rest.download.DownloadRoutes
import edp.davinci.rest.group.GroupRoutes
import edp.davinci.rest.shares.ShareRoutes
import edp.davinci.rest.source.SourceRoutes
import edp.davinci.rest.sqllog.SqlLogRoutes
import edp.davinci.rest.user.UserRoutes
import edp.davinci.rest.view.ViewRoutes
import edp.davinci.rest.widget.WidgetRoutes
import edp.davinci.util.CorsSupport

class RoutesApi(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with CorsSupport {
  val swagger = new SwaggerRoutes
  val login = new LoginRoutes(modules)
  val changePwd = new ChangePwdRoutes(modules)
  val users = new UserRoutes(modules)
  val source = new SourceRoutes(modules)
  val flatTable = new ViewRoutes(modules)
  val dashboard = new DashboardRoutes(modules)
  val widget = new WidgetRoutes(modules)
  val group = new GroupRoutes(modules)
  val sqlLog = new SqlLogRoutes(modules)
  val share = new ShareRoutes(modules)
  val download = new DownloadRoutes(modules)
  val davinci = new DavinciRoutes

  val routes: Route =
    corsHandler(swagger.indexRoute) ~ corsHandler(swagger.routes) ~
      corsHandler(davinci.indexRoute) ~ corsHandler(davinci.shareRoute) ~
      pathPrefix("api" / "v1") {
        corsHandler(login.routes) ~
          corsHandler(users.routes) ~
          corsHandler(changePwd.routes) ~
          corsHandler(source.routes) ~
          corsHandler(flatTable.routes) ~
          corsHandler(dashboard.routes) ~
          corsHandler(widget.routes) ~
          corsHandler(group.routes) ~
          corsHandler(sqlLog.routes) ~
          corsHandler(share.routes) ~
          corsHandler(download.routes)
      }
}
