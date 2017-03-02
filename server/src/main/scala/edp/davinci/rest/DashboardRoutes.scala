package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities.RelDashboardWidget
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import slick.jdbc.MySQLProfile.api._

import scala.util.{Failure, Success}

@Api(value = "/dashboards", consumes = "application/json", produces = "application/json")
@Path("/dashboards")
class DashboardRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes = getDashboardByIdRoute ~ getDashboardByNameRoute ~ postDashboardRoute ~ putDashboardRoute ~ getDashboardByAllRoute ~ deleteDashboardByIdRoute

  @Path("/{id}")
  @ApiOperation(value = "get one dashboard from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "dashboard id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDashboardByIdRoute: Route = modules.dashboardRoutes.getByIdRoute("dashboards")

//  def getDashboardById(route: String, id: Long, session: SessionClass): Route = {
//    val future = if (session.admin) modules.dashboardDal.findById(id) else modules.dashboardDal.findByFilter(obj => obj.id === id && obj.publish === true && obj.active === true).map(seq => seq.headOption)
//    onComplete(future) {
//      case Success(dashboardOpt) => dashboardOpt match {
//        case Some(dashboard) => {
//          onComplete(modules.relDashboardWidgetDal.findByFilter(obj => obj.dashboard_id === id && obj.active === true).mapTo[Seq[RelDashboardWidget]]){
//            case Success(relSeq) =>
//              relSeq.isEmpty
//            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
//          }
//        }
//        case None => complete(NotFound, getHeader(404, session))
//      }
//      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
//    }
//
//
//  }


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