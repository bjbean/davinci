package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule}
import edp.davinci.persistence.entities.QueryUserInfo
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

@Api(value = "login", consumes = "application/json", produces = "application/json")
@Path("/login")
class LoginRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule) extends Directives {

  val routes: Route = accessTokenRoute
  private val logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(value = "Login into the server and return token", notes = "", nickname = "login", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "username", value = "Login information", required = true, dataType = "edp.davinci.rest.LoginClass", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "unspecified error"),
    new ApiResponse(code = 400, message = "pwd is wrong"),
    new ApiResponse(code = 404, message = "user not found")
  ))
  def accessTokenRoute: Route = path("login") {
    post {
      entity(as[LoginClass]) { login =>
        onComplete(AuthorizationProvider.createSessionClass(login)) {
          case Success(sessionEither) =>
            sessionEither.fold(authorizationError => complete(Unauthorized, ResponseJson[String](getHeader(authorizationError.statusCode, authorizationError.desc, null), "")),
              info => complete(OK, ResponseJson[QueryUserInfo](getHeader(200, info._1), info._2))
            )
          case Failure(ex) => complete(Unauthorized, ResponseJson[String](getHeader(401, ex.getMessage, null), ""))
        }
      }
    }
  }

}
