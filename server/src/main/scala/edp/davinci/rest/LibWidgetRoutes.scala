//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.base.BaseEntity
//import edp.davinci.persistence.entities.JsonProtocol._
//import edp.davinci.persistence.entities.{LibWidget, LibWidgetTable}
//import edp.davinci.util.Utils._
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations._
//import slick.jdbc.MySQLProfile.api._
//import scala.util.{Failure, Success}
//
//@Api(value = "/libWidgets", consumes = "application/json", produces = "application/json")
//@Path("/libWidgets")
//class LibWidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//
//  val routes = getLibWidgetByIdRoute ~ getLibWidgetByAllRoute ~ getLibWidgetByPageRoute ~ postLibWidgetRoute ~ putLibWidgetRoute ~ deleteLibWidgetByIdRoute ~ deleteLibWidgetByAllRoute
//
//  @Path("/{id}")
//  @ApiOperation(value = "get one libWidget from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "libWidget id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 404, message = "libWidget not found"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getLibWidgetByIdRoute: Route = modules.libWidgetRoutes.getByIdRoute("libWidgets")
//
//  @ApiOperation(value = "get all libWidget with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getLibWidgetByAllRoute = modules.libWidgetRoutes.getByAllRoute("libWidgets", "id")
//
//  @Path("{page=\\d+&size=\\d+}")
//  @ApiOperation(value = "get libWidgets with pagenifation", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getLibWidgetByPageRoute = modules.libWidgetRoutes.pagenifationRoute("libWidgets")
//
//
//  @ApiOperation(value = "Add a new libWidget to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "libWidget", value = "LibWidget object to be added", required = true, dataType = "edp.davinci.rest.LibWidgetClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def postLibWidgetRoute = path("libWidgets") {
//    post {
//      entity(as[LibWidgetClass]) {
//        postClass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                if (session.admin)
//                  onComplete(modules.libWidgetDal.insert(modules.libWidgetRoutes.generateEntity(postClass, session).asInstanceOf[LibWidget]).mapTo[Long]) {
//                    case Success(result) => complete(HttpResponse(OK, entity = "post success"))
//                    case Failure(ex) => complete(InternalServerError, "internal server error")
//                  } else complete(Forbidden, "user is not admin")
//              }
//          }
//      }
//    }
//  }
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a libWidget in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "libWidget id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "libWidget", value = "LibWidget object to be updated", required = true, dataType = "edp.davinci.rest.LibWidgetClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "libWidget not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def putLibWidgetRoute = modules.libWidgetRoutes.putRoute("libWidgets")
//
//  @Path("/{id}")
//  @ApiOperation(value = "delete libWidget by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "libWidget id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteLibWidgetByIdRoute = modules.libWidgetRoutes.deleteByIdRoute("libWidgets")
//
//  @ApiOperation(value = "delete all libWidgets with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteLibWidgetByAllRoute = path("libWidgets") {
//    delete {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            if (session.admin)
//              onComplete(modules.libWidgetDal.findByFilter(_.active === true).mapTo[Seq[LibWidget]]) {
//                case Success(seq) => onComplete(
//                  modules.libWidgetDal.update(seq.map(base => modules.libWidgetRoutes.generateEntity(base, session)).asInstanceOf[Seq[LibWidget]]).mapTo[Unit]) {
//                  case Success(unit) => complete(HttpResponse(OK, entity = "delete success"))
//                  case Failure(ex) => complete(InternalServerError, "internal server error")
//                }
//                case Failure(ex) => complete(InternalServerError, "internal server error")
//              }
//            else complete(Forbidden, "user is not admin")
//          }
//      }
//    }
//  }
//}
