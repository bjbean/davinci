package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule}
import edp.davinci.persistence.entities.JsonProtocol._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.Utils._
import io.swagger.annotations._

import scala.util.{Failure, Success}

@Api(value = "login", consumes = "application/json", produces = "application/json")
@Path("/login")
class LoginRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule) extends Directives {

  val routes = accessTokenRoute

  @ApiOperation(value = "Login into the server and return token", notes = "", nickname = "login", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "username", value = "Login information", required = true, dataType = "edp.davinci.rest.LoginClass", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "password is wrong"),
    new ApiResponse(code = 404, message = "user not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def accessTokenRoute = path("login") {
    post {
      entity(as[LoginClass]) { login =>
        onComplete(AuthorizationProvider.createSessionClass(login)) {
          case Success(sessionEither) =>
            sessionEither.fold(authorizationError => complete(Unauthorized, getHeader(authorizationError.statusCode, authorizationError.desc, null)),
              session => complete(OK, getHeader(200, session))
            )
          case Failure(ex) => complete(InternalServerError, getHeader(500, null))
        }
      }
    }
  }

}