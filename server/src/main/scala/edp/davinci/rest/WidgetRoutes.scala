//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, OK}
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.entities.{Widget, WidgetTable}
//import edp.davinci.persistence.base.BaseEntity
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations._
//import edp.davinci.persistence.entities.JsonProtocol._
//
//import scala.util.{Failure, Success}
//import slick.jdbc.H2Profile.api._
//
//@Api(value = "/widgets", consumes = "application/json", produces = "application/json")
//@Path("/widgets")
//class WidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//  val routes =
//    getAllWidgetsRoute ~
//      getWidgetByIdRoute ~
//      createWidgetRoute ~
//      deleteWidgetByAllRoute ~
//      deleteWidgetByIdRoute ~
//      updateWidgetRoute
//
//
//  @ApiOperation(value = "list all widgets", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "widgets", value = "", required = false, dataType = "", paramType = "")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "widgets not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getAllWidgetsRoute = modules.widgetRoutes.getByAllRoute("widgets", "domain_id")
//
//
//  @Path("/{id}")
//  @ApiOperation(value = "get one widget from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "widgetid", value = "widget id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 404, message = "widget not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getWidgetByIdRoute = modules.widgetRoutes.getByIdRoute("widgets")
//
//  @ApiOperation(value = "Add a new widget to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "widget", value = "Widget object to be added", required = true, dataType = "edp.davinci.rest.WidgetClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def createWidgetRoute = path("widgets") {
//    post {
//      entity(as[WidgetClass]) {
//        baseclass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                modules.widgetRoutes.postComplete(session, baseclass, modules.userDal.findByFilter(table => table.name === baseclass.name))
//              }
//          }
//      }
//    }
//  }
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a widget in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "widget id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "widget", value = "Widget object to be updated", required = true, dataType = "edp.davinci.rest.WidgetClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "widget not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def updateWidgetRoute = modules.widgetRoutes.putRoute("widgets")
//
//  @Path("/{id}")
//  @ApiOperation(value = "delete widget by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "widgetId", value = "widget id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteWidgetByIdRoute = modules.widgetRoutes.deleteByIdRoute("widgets")
//
//  @ApiOperation(value = "delete all widgets with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteWidgetByAllRoute = modules.widgetRoutes.deleteByAllRoute("widgets")
//}
