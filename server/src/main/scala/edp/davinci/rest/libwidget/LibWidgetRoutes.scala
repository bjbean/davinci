package edp.davinci.rest.libwidget

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import edp.davinci.util.JsonProtocol._

import scala.util.{Failure, Success}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities.QueryLibWidget
import edp.davinci.rest.{ResponseJson, ResponseSeqJson, SessionClass}
import edp.davinci.util.AuthorizationProvider
import io.swagger.annotations._
import edp.davinci.util.CommonUtils._
import org.slf4j.LoggerFactory

@Api(value = "/libWidgets", consumes = "application/json", produces = "application/json")
@Path("/libWidgets")
class LibWidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getLibWidgetByAllRoute
  private lazy val libWidgetService = new LibWidgetService(modules)
  private val logger = LoggerFactory.getLogger(this.getClass)

  //  @Path("/{id}")
  //  @ApiOperation(value = "get one libWidget from system by id", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "id", value = "libWidget id", required = true, dataType = "integer", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 400, message = "bad request"),
  //    new ApiResponse(code = 403, message = "user is not admin")
  //  ))
  //  def getLibWidgetByIdRoute: Route = modules.libWidgetRoutes.getByIdRoute("libWidgets")

  @ApiOperation(value = "get all libWidget with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 402, message = "internal service error")
  ))
  def getLibWidgetByAllRoute: Route = path("libWidgets") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          if (session.admin) {
            onComplete(libWidgetService.getAll) {
              case Success(libWidgetSeq) => complete(OK, ResponseSeqJson[QueryLibWidget](getHeader(200, session), libWidgetSeq))
              case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
            }
          } else complete(Forbidden, ResponseJson[String](getHeader(403, "user is not admin", session), ""))
      }
    }
  }

}
