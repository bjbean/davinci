package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities.JsonProtocol._
import edp.davinci.persistence.entities.User
import edp.davinci.util.AuthorizationProvider
import io.swagger.annotations._

@Api(value = "/users", consumes = "application/json", produces = "application/json")
@Path("/users")
class UserRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes = getUserByIdRoute ~ getUserByAllRoute ~ postUserRoute ~ putUserRoute ~ getUserByPageRoute ~ deleteUserByIdRoute

  @Path("/{id}")
  @ApiOperation(value = "get one user from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "user id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "user not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getUserByIdRoute: Route = modules.userRoutes.getByIdRoute("users")

  @ApiOperation(value = "get all user with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getUserByAllRoute = modules.userRoutes.getByAllRoute("users", "domain_id")


  @Path("{page=\\d+&size=\\d+}")
  @ApiOperation(value = "get users with paginate", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "paginate", value = "paginate information", required = true, dataType = "String", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getUserByPageRoute = modules.userRoutes.paginateRoute("users", "domain_id")


  @ApiOperation(value = "Add a new user to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "user", value = "User object to be added", required = true, dataType = "edp.davinci.rest.UserClassSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postUserRoute = path("users") {
    post {
      entity(as[RequestJson[Seq[UserClass]]]) {
        seq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.userRoutes.postComplete(session, seq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update a user in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "user", value = "User object to be updated", required = true, dataType = "edp.davinci.rest.UserSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "user not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putUserRoute = path("users") {
    put {
      entity(as[RequestJson[Seq[User]]]) {
        seq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.userRoutes.putComplete(session, seq.payload)
          }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete user by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "user id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteUserByIdRoute = modules.userRoutes.deleteByIdRoute("users")
}
