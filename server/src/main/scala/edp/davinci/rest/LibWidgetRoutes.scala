package edp.davinci.rest

import javax.ws.rs.Path
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import io.swagger.annotations._

@Api(value = "/libWidgets", consumes = "application/json", produces = "application/json")
@Path("/libWidgets")
class LibWidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getLibWidgetByIdRoute ~ getLibWidgetByAllRoute

  @Path("/{id}")
  @ApiOperation(value = "get one libWidget from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "libWidget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 403, message = "user is not admin")
  ))
  def getLibWidgetByIdRoute: Route = modules.libWidgetRoutes.getByIdRoute("libWidgets")

  @ApiOperation(value = "get all libWidget with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 402, message = "internal service error")
  ))
  def getLibWidgetByAllRoute: Route = modules.libWidgetRoutes.getByAllRoute("libWidgets")
}
