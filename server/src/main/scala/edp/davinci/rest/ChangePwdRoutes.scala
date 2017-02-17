package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule}
import edp.davinci.persistence.entities.JsonProtocol._
import edp.davinci.persistence.entities.User
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.Utils._
import io.swagger.annotations._

import scala.util.{Failure, Success}

@Api(value = "/changepwd", consumes = "application/json")
@Path("/changepwd")
class ChangePwdRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule) extends Directives {

  val routes = changePwdRoute

  @ApiOperation(value = "change user's password", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "changePwd", value = "change password information", required = true, dataType = "edp.davinci.rest.ChangePwdClass", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 400, message = "old password is wrong"),
    new ApiResponse(code = 404, message = "user not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def changePwdRoute = path("changepwd") {
    post {
      entity(as[ChangePwdClass]) { changePwd =>
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session =>
            onComplete(modules.userDal.findById(session.userId).mapTo[Option[User]]) {
              case Success(userOpt) => userOpt match {
                case Some(user) =>
                  if (user.password == changePwd.oldPass) {
                    onComplete(modules.userDal.update(updatePass(user, changePwd.newPass)).mapTo[Int]) {
                      case Success(result) => complete(OK, getHeader(200, session))
                      case Failure(ex) => complete(InternalServerError, getHeader(500, session))
                    }
                  } else complete(Unauthorized, getHeader(401, "old password is wrong", session))
                case None => complete(NotFound, getHeader(404, session))
              }
              case Failure(ex) => complete(InternalServerError, getHeader(500, session))
            }
        }
      }
    }
  }

  private def updatePass(user: User, password: String): User = {
    User(user.id, user.domain_id, user.email, password, user.title, user.name, user.admin,
      user.active, user.create_time, user.create_by, user.update_time, user.update_by)
  }

}