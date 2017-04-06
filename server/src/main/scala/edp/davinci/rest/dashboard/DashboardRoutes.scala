package edp.davinci.rest.dashboard

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import slick.jdbc.MySQLProfile.api._

import scala.util.{Failure, Success}

@Api(value = "/dashboards", consumes = "application/json", produces = "application/json")
@Path("/dashboards")
class DashboardRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with DashboardService {

  val routes: Route = getWidgetByDashboardIdRoute ~ postDashboardRoute ~ putDashboardRoute ~ postWidget2DashboardRoute ~ getDashboardByAllRoute ~ deleteDashboardByIdRoute ~ deleteWidgetFromDashboardRoute ~ postWidget2DashboardRoute ~ putWidgetInDashboardRoute

  @Path("/{dashboard_id}")
  @ApiOperation(value = "get one dashboard from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard_id", value = "dashboard id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getWidgetByDashboardIdRoute: Route = path("dashboards" / LongNumber) { dashboard_id =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getDashboardById(dashboard_id, session)
      }
    }
  }


  //  @Path("/{name}")
  //  @ApiOperation(value = "get one dashboard from system by name", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "name", value = "dashboard name", required = true, dataType = "String", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 404, message = "dashboard not found"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getDashboardByNameRoute: Route = modules.dashboardRoutes.getByNameRoute("dashboards")

  @ApiOperation(value = "get all dashboards with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDashboardByAllRoute: Route = modules.dashboardRoutes.getByAllRoute("dashboards")

  @ApiOperation(value = "Add new dashboards to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboards", value = "Dashboard objects to be added", required = true, dataType = "edp.davinci.rest.PostDashboardInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postDashboardRoute: Route = path("dashboards") {
    post {
      entity(as[PostDashboardInfoSeq]) {
        dashboardSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postDashBoard(session, dashboardSeq.payload)
          }
      }
    }
  }

  @ApiOperation(value = "update dashboards in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard", value = "Dashboard objects to be updated", required = true, dataType = "edp.davinci.rest.PutDashboardSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 404, message = "dashboards not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putDashboardRoute: Route = path("dashboards") {
    put {
      entity(as[PutDashboardSeq]) {
        dashboardSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putDashboardComplete(session, dashboardSeq.payload)
          }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete dashboard by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "dashboard id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteDashboardByIdRoute: Route = modules.dashboardRoutes.deleteByIdRoute("dashboards")

  @Path("/{dashboard_id}/widgets")
  @ApiOperation(value = "add widgets to a dashboard", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard_id", value = "dashboard id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "relDashboardWidget", value = "RelDashboardWidget objects to be added", required = true, dataType = "edp.davinci.rest.PostRelDashboardWidgetSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postWidget2DashboardRoute: Route = path("dashboards" / LongNumber / "widgets") { _ =>
    post {
      entity(as[PostRelDashboardWidgetSeq]) {
        relDashboardWidgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postWidget2Dashboard(session, relDashboardWidgetSeq.payload)
          }
      }
    }
  }

  @Path("/{dashboard_id}/widgets")
  @ApiOperation(value = "update widgets in the dashboard", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard_id", value = "dashboard id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "relDashboardWidget", value = "RelDashboardWidget objects to be added", required = true, dataType = "edp.davinci.rest.PutRelDashboardWidgetSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putWidgetInDashboardRoute: Route = path("dashboards" / LongNumber / "widgets") { _ =>
    put {
      entity(as[PutRelDashboardWidgetSeq]) {
        relDashboardWidgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putWidgetInDashboard(session, relDashboardWidgetSeq.payload)
          }
      }
    }
  }


  @Path("/widgets/{rel_id}")
  @ApiOperation(value = "delete widget from dashboard by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "rel_id", value = "relation id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteWidgetFromDashboardRoute: Route = path("dashboards" / "widgets" / LongNumber) { relId =>
    delete {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => modules.relDashboardWidgetRoutes.deleteByIdComplete(relId, session)
      }
    }
  }

  //  private def deleteWidgetFromDashboardByIdComplete(dashboardId: Long, widgetId: Long, session: SessionClass): Route = {
  //    if (session.admin)
  //      onComplete(modules.relDashboardWidgetDal.deleteByFilter(obj => obj.dashboard_id === dashboardId && obj.widget_id === widgetId).mapTo[Int]) {
  //        case Success(_) => complete(OK, getHeader(200, session))
  //        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
  //      } else complete(Forbidden, getHeader(403, session))
  //  }

}