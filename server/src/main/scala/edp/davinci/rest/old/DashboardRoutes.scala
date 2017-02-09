//package edp.davinci.rest.old
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
//import akka.http.scaladsl.model.headers.RawHeader
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{ConfigurationModule, PersistenceModule}
//import edp.davinci.persistence.entities.Dashboard
//import edp.davinci.rest.SessionClass
//import edp.davinci.util.{AuthorizationProvider, JwtSupport}
//import io.swagger.annotations._
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//
//@Api(value = "/dashboard", consumes = "application/json", produces = "application/json")
//@Path("/dashboard")
//class DashboardRoutes(modules: ConfigurationModule with PersistenceModule) extends Directives {
//
//  val routes: Route = getAllDashboardsRoute ~ getDashboardByIdRoute
//
//  @Path("")
//  @ApiOperation(value = "list all dashboards", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "dashboards", value = "", required = false, dataType = "", paramType = "")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 404, message = "dashboards not found"),
//    new ApiResponse(code = 401, message = "Authentication is possible but has failed or not yet been provided"),
//    new ApiResponse(code = 500, message = "There was an internal server error")
//  ))
//  def getAllDashboardsRoute: Route = path("dashboard") {
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          onComplete(modules.dashboardDal.
//            findByFilter(dashboard => dashboard.domain_id === session.domainId && dashboard.active && session.admin).mapTo[Seq[Dashboard]]) {
//            case Success(dashboardSeq) =>
//              if (dashboardSeq.nonEmpty)
//                respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                  complete(OK, dashboardSeq.map(dashboard => (dashboard.id, dashboard.name)))
//                } else respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                complete(NotFound, "dashboards not found")
//              }
//            case Failure(ex) =>
//              respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//              }
//          }
//      }
//    }
//  }
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
//    new ApiResponse(code = 401, message = "Authentication is possible but has failed or not yet been provided"),
//    new ApiResponse(code = 500, message = "There was an internal server error")
//  ))
//  def getDashboardByIdRoute: Route = path("dashboard" / LongNumber) { dashboardid =>
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          onComplete(modules.dashboardDal.findById(dashboardid).mapTo[Option[Dashboard]]) {
//            case Success(dashboardOpt) => dashboardOpt match {
//              case Some(dashboard) =>
//                respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                  complete(OK, dashboard)
//                }
//              case None =>
//                respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                  complete(NotFound, "dashboard not found")
//                }
//            }
//            case Failure(ex) =>
//              respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//              }
//          }
//      }
//    }
//  }
//
//}
