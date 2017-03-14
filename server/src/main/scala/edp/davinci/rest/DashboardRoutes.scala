package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities.Dashboard
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Api(value = "/dashboards", consumes = "application/json", produces = "application/json")
@Path("/dashboards")
class DashboardRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes = getDashboardByIdRoute ~ getDashboardByNameRoute ~ postDashboardRoute ~ putDashboardRoute ~ getDashboardByAllRoute ~ deleteDashboardByIdRoute

  @Path("/{dashboard_id}/groups/{group_id}")
  @ApiOperation(value = "get one dashboard from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard_id", value = "dashboard id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDashboardByIdRoute: Route = path("dashboards" / LongNumber / "groups" / LongNumber) {
    (dashboard_id, group_id) =>
      get {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session => getDashboardById(dashboard_id, group_id, session)
        }
      }

  }


  private def getDashboardById(dashboard_id: Long, group_id: Long, session: SessionClass): Route = {
    val future =
      if (session.admin) modules.dashboardDal.findById(dashboard_id)
      else
        modules.dashboardDal.findByFilter(obj => obj.id === dashboard_id && obj.active === true && obj.publish === true).map[Option[Dashboard]](_.headOption)
    onComplete(future) {
      case Success(dashboardOpt) => dashboardOpt match {
        case Some(dashboard) => {
          val query = if (session.admin)
            (modules.relDashboardWidgetQuery.filter(obj => obj.dashboard_id === dashboard_id && obj.active === true) join modules.widgetQuery.filter(_.active === true) on (_.widget_id === _.id)).
              map {
                case (r, w) => (r.id, r.dashboard_id, w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.desc, false, w.trigger_type, w.trigger_params, w.publish, w.active, r.position_x, r.position_y, r.length, r.width, r.create_time, r.create_by, r.update_time, r.update_by)
              }.result
          else {
            val relGB_ids = (modules.bizlogicQuery.filter(_.active === true) join modules.relGroupBizlogicQuery.filter(_.active === true) on (_.id === _.bizlogic_id)).map(_._1.id)

            (modules.relDashboardWidgetQuery.filter(obj => obj.dashboard_id === dashboard_id && obj.active === true) join
              modules.widgetQuery.filter(obj => obj.bizlogic_id in relGB_ids).filter(obj => obj.active === true && obj.publish === true) on (_.widget_id === _.id)).
              map {
                case (r, w) => (r.id, r.dashboard_id, w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.desc, false, w.trigger_type, w.trigger_params, w.publish, w.active, r.position_x, r.position_y, r.length, r.width, r.create_time, r.create_by, r.update_time, r.update_by)
              }.union {
              (modules.relDashboardWidgetQuery.filter(obj => obj.dashboard_id === dashboard_id && obj.active === true) join
                modules.widgetQuery.filterNot(obj => obj.bizlogic_id in relGB_ids).filter(obj => obj.active === true && obj.publish === true) on (_.widget_id === _.id)).
                map {
                  case (r, w) => (r.id, r.dashboard_id, w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.desc, true, w.trigger_type, w.trigger_params, w.publish, w.active, r.position_x, r.position_y, r.length, r.width, r.create_time, r.create_by, r.update_time, r.update_by)
                }
            }
          }.result

          onComplete(modules.db.run(query).mapTo[Seq[WidgetInfo]]) {
            case Success(widgetInfoSeq) => complete(OK, ResponseJson[DashboardInfo](getHeader(200, session), DashboardInfo(dashboard, widgetInfoSeq)))
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
        }
        case None => complete(NotFound, getHeader(404, session))
      }
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }


  @Path("/{name}")
  @ApiOperation(value = "get one dashboard from system by name", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name", value = "dashboard name", required = true, dataType = "String", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDashboardByNameRoute: Route = modules.dashboardRoutes.getByNameRoute("dashboards")

  @ApiOperation(value = "get all dashboards with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDashboardByAllRoute = modules.dashboardRoutes.getByAllRoute("dashboards")

  @ApiOperation(value = "Add new dashboards to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboards", value = "Dashboard objects to be added", required = true, dataType = "edp.davinci.rest.SimpleDashboardSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postDashboardRoute = path("dashboards") {
    post {
      entity(as[SimpleDashboardSeq]) {
        dashboardSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.dashboardRoutes.postComplete(session, dashboardSeq.payload)
          }
      }
    }
  }

  @ApiOperation(value = "update dashboards in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard", value = "Dashboard objects to be updated", required = true, dataType = "edp.davinci.rest.DashboardSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 404, message = "dashboards not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putDashboardRoute = path("dashboards") {
    put {
      entity(as[DashboardSeq]) {
        dashboardSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.dashboardRoutes.putComplete(session, dashboardSeq.payload)
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
  def deleteDashboardByIdRoute = modules.dashboardRoutes.deleteByIdRoute("dashboards")
}