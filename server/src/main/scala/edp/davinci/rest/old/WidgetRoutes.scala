//package edp.davinci.rest.old
//
//import javax.ws.rs.Path
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule}
//import edp.davinci.persistence.entities.{Widget, WidgetTable}
//import edp.davinci.rest.BaseRoutesImpl
//import io.swagger.annotations._
//
//@Api(value = "/widget", consumes = "application/json", produces = "application/json")
//@Path("/widget")
//class WidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule) extends Directives {
//
//  private lazy val handler = new BaseRoutesImpl[WidgetTable, Widget](modules, modules.widgetDal)
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
//  private lazy val getAllWidgetsRoute: Route = handler.getByAllRoute("widgets")
//
//
//  @Path("/{widgetid}")
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
//  private lazy val getWidgetByIdRoute: Route = handler.getByIdRoute("widgets")
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
//  private lazy val createWidgetRoute = handler.postRoute("widgets")
//
//  @Path("/{widgetid}")
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
//  private lazy val deleteWidgetByIdRoute = handler.deleteByIdRoute("widgets")
//
//  @ApiOperation(value = "delete all widgets with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  private lazy val deleteWidgetByAllRoute = handler.deleteByAllRoute("widgets")
//
//  lazy val routes: Route = getAllWidgetsRoute ~ getWidgetByIdRoute ~ createWidgetRoute ~ deleteWidgetByAllRoute ~ deleteWidgetByIdRoute
//
//
//}
