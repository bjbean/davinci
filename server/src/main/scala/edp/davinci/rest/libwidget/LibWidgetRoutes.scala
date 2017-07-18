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
import edp.davinci.util.ResponseUtils._
import org.slf4j.LoggerFactory

@Api(value = "/libwidgets", consumes = "application/json", produces = "application/json")
@Path("/libwidgets")
class LibWidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getLibWidgetByAllRoute
  private lazy val libWidgetService = new LibWidgetService(modules)
  private val logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(value = "get all libWidget with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 402, message = "internal service error")
  ))
  def getLibWidgetByAllRoute: Route = path("libwidgets") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          onComplete(libWidgetService.getAll) {
            case Success(libWidgetSeq) => complete(OK, ResponseSeqJson[QueryLibWidget](getHeader(200, session), libWidgetSeq))
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }

      }
    }
  }

}
