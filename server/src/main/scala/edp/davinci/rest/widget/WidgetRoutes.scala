package edp.davinci.rest.widget

import javax.ws.rs.Path
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.rest.{PostWidgetInfoSeq, PutWidgetInfoSeq, SessionClass}
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._

@Api(value = "/widgets", consumes = "application/json", produces = "application/json")
@Path("/widgets")
class WidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with WidgetService {
  val routes = getAllWidgetsRoute ~ postWidgetRoute ~ deleteWidgetByIdRoute ~ putWidgetRoute ~ getWholeSqlByWidgetIdRoute


  @ApiOperation(value = "list all widgets", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widgets", value = "", required = false, dataType = "", paramType = "")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "widgets not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getAllWidgetsRoute: Route = path("widgets") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllWidgetsComplete(session)
      }
    }
  }


  //  @Path("/{id}")
  //  @ApiOperation(value = "get one widget from system by id", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "id", value = "widget id", required = true, dataType = "integer", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 404, message = "widget not found"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getWidgetByIdRoute: Route = modules.widgetRoutes.getByIdRoute("widgets")
  //
  //
  //  @Path("/{name}")
  //  @ApiOperation(value = "get one widget from system by name", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "name", value = "widget name", required = true, dataType = "string", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 404, message = "widget not found"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getWidgetByNameRoute: Route = modules.widgetRoutes.getByNameRoute("widgets")


  @ApiOperation(value = "Add a new widget to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget", value = "Widget object to be added", required = true, dataType = "edp.davinci.rest.PostWidgetInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postWidgetRoute: Route = path("widgets") {
    post {
      entity(as[PostWidgetInfoSeq]) {
        widgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postWidget(session, widgetSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update widgets in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget", value = "Widget object to be updated", required = true, dataType = "edp.davinci.rest.PutWidgetInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putWidgetRoute: Route = path("widgets") {
    put {
      entity(as[PutWidgetInfoSeq]) {
        widgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putWidgetComplete(session, widgetSeq.payload)
          }
      }
    }
  }

  @Path("/{widgetId}")
  @ApiOperation(value = "delete widget by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widgetId", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteWidgetByIdRoute: Route = modules.widgetRoutes.deleteByIdRoute("widgets")

  @Path("/{widgetId}/sqls")
  @ApiOperation(value = "get whole sql by widget id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widgetId", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getWholeSqlByWidgetIdRoute: Route = path("widgets" / LongNumber / "sqls") { widgetId =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getWholeSql(session, widgetId)
      }
    }

  }
}
