//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, OK}
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.entities.{Dashboard, DashboardTable}
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations._
//import edp.davinci.persistence.entities.JsonProtocol._
//
//import scala.util.{Failure, Success}
//import slick.jdbc.H2Profile.api._
//
//
//@Api(value = "/dashboards", consumes = "application/json", produces = "application/json")
//@Path("/dashboards")
//class DashboardRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//
//  val routes: Route =
//    getAllDashboardsRoute ~
//    getDashboardByIdRoute ~
//    createDashboardRoute ~
//    deleteDashboardByAllRoute ~
//    deleteDashboardByIdRoute ~
//    updateDashboardRoute
//
//
//
//  @ApiOperation(value = "list all dashboards", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "dashboards", value = "", required = false, dataType = "", paramType = "")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "dashboards not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getAllDashboardsRoute: Route = modules.dashboardRoutes.getByAllRoute("dashboards", "domain_id")
//
//
//  @Path("/{dashboardid}")
//  @ApiOperation(value = "get one dashboard from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "dashboardid", value = "dashboard id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 404, message = "dashboard not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getDashboardByIdRoute: Route = modules.dashboardRoutes.getByIdRoute("dashboards")
//
//  @ApiOperation(value = "Add a new dashboard to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "dashboard", value = "dashboard object to be added", required = true, dataType = "edp.davinci.rest.DashboardClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def createDashboardRoute = path("dashboards") {
//    post {
//      entity(as[DashboardClass]) {
//        baseclass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                modules.dashboardRoutes.postComplete(session, baseclass, modules.userDal.findByFilter(table => table.name === baseclass.name))
//              }
//          }
//      }
//    }
//  }
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a dashboard in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "dashboard id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "dashboard", value = "Dashboard object to be updated", required = true, dataType = "edp.davinci.rest.DashboardClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "dashboard not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def updateDashboardRoute = modules.dashboardRoutes.putRoute("dashboards")
//
//
//  @Path("/{dashboardid}")
//  @ApiOperation(value = "delete dashboard by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "dashboardId", value = "dashboard id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteDashboardByIdRoute = modules.dashboardRoutes.deleteByIdRoute("dashboards")
//
//  @ApiOperation(value = "delete all dashboards with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteDashboardByAllRoute = modules.dashboardRoutes.deleteByAllRoute("dashboards")
//
//}