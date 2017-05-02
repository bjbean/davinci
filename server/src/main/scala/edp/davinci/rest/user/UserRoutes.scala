package edp.davinci.rest.user

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._

import scala.util.{Failure, Success}

@Api(value = "/users", consumes = "application/json", produces = "application/json")
@Path("/users")
class UserRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = postUserRoute ~ putUserRoute ~ putLoginUserRoute ~ getUserByAllRoute ~ deleteUserByIdRoute ~ getGroupsByUserIdRoute ~ deleteUserFromGroupRoute
  private lazy val userService = new UserService(modules)

  @ApiOperation(value = "get all users with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getUserByAllRoute: Route = path("users") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllUsersComplete(session)
      }
    }
  }

  @ApiOperation(value = "Add new users to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "users", value = "User objects to be added", required = true, dataType = "edp.davinci.rest.PostUserInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postUserRoute: Route = path("users") {
    post {
      entity(as[PostUserInfoSeq]) {
        userSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postUserComplete(session, userSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update users in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "user", value = "User objects to be updated", required = true, dataType = "edp.davinci.rest.PutUserInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "users not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putUserRoute: Route = path("users") {
    put {
      entity(as[PutUserInfoSeq]) {
        userSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putUserComplete(session, userSeq.payload)
          }
      }
    }
  }

  @Path("/profile")
  @ApiOperation(value = "update login users profile", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "user", value = "login user objects to be updated", required = true, dataType = "edp.davinci.rest.LoginUserInfo", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "users not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putLoginUserRoute: Route = path("users" / "profile") {
    put {
      entity(as[LoginUserInfo]) {
        user =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putLoginUserComplete(session, user)
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
  def deleteUserByIdRoute: Route = modules.userRoutes.deleteByIdRoute("users")

  //  @Path("{page=\\d+&size=\\d+}")
  //  @ApiOperation(value = "get users with paginate", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "paginate", value = "paginate information", required = true, dataType = "String", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 403, message = "user is not admin"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getUserByPageRoute = modules.userRoutes.paginateRoute("users", "domain_id")

  @Path("/{user_id}/groups")
  @ApiOperation(value = "get groups by user id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "user_id", value = "user id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getGroupsByUserIdRoute: Route = path("users" / LongNumber / "groups") { userId =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getGroupsByUserIdComplete(session, userId)
      }

    }
  }

  @Path("/groups")
  @ApiOperation(value = "Add user to a group", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "relUserGroupRequest", value = "relUserGroupRequest object to be added", required = true, dataType = "edp.davinci.rest.PostRelUserGroupSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postUser2GroupRoute: Route = pathPrefix("users" / "groups") {
    post {
      entity(as[PostRelUserGroupSeq]) {
        postRelUserGroupSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.relUserGroupRoutes.postComplete(session, postRelUserGroupSeq.payload)
          }
      }
    }
  }


  @Path("/groups/{rel_id}")
  @ApiOperation(value = "remove user from group by rel id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "rel_id", value = "rel id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteUserFromGroupRoute: Route = path("users" / "groups" / LongNumber) { relId =>
    delete {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => modules.relUserGroupRoutes.deleteByIdComplete(relId, session)
      }
    }
  }

  private def getAllUsersComplete(session: SessionClass): Route = {
    onComplete(userService.getAll(session)) {
      case Success(userSeq) =>
        if (userSeq.nonEmpty) complete(OK, ResponseSeqJson[QueryUserInfo](getHeader(200, session), userSeq))
        else complete(NotFound, getHeader(404, session))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  private def putUserComplete(session: SessionClass, userSeq: Seq[PutUserInfo]): Route = {
    if (session.admin) {
      val future = userService.update(userSeq, session)
      onComplete(future) {
        case Success(_) =>
          onComplete(userService.deleteAllByUserId(userSeq)) {
            case Success(_) =>
              val relSeq = for {rel <- userSeq.head.relUG
              } yield RelUserGroup(0, userSeq.head.id, rel.group_id, active = true, null, session.userId, null, session.userId)
              onComplete(modules.relUserGroupDal.insert(relSeq)) {
                case Success(_) => complete(OK, getHeader(200, session))
                case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
              }
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  private def putLoginUserComplete(session: SessionClass, user: LoginUserInfo): Route = {
    val future = userService.updateLoginUser(user, session)
    onComplete(future) {
      case Success(_) => complete(OK, getHeader(200, session))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  private def getGroupsByUserIdComplete(session: SessionClass, userId: Long): Route = {
    val future = userService.getAllGroups(userId)
    onComplete(future) {
      case Success(relSeq) =>
        if (relSeq.nonEmpty) complete(OK, ResponseSeqJson[PutRelUserGroup](getHeader(200, session), relSeq))
        else complete(NotFound, getHeader(404, session))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  private def postUserComplete(session: SessionClass, userSeq: Seq[PostUserInfo]): Route = {
    if (session.admin) {
      val userEntity = userSeq.map(postUser => User(0, postUser.email, postUser.password, postUser.title, postUser.name, postUser.admin, active = true, null, session.userId, null, session.userId))
      onComplete(modules.userDal.insert(userEntity)) {
        case Success(users) =>
          val relEntity = userSeq.head.relUG.map(rel => RelUserGroup(0, users.head.id, rel.group_id, active = true, null, session.userId, null, session.userId))
          onComplete(modules.relUserGroupDal.insert(relEntity)) {
            case Success(_) =>
              val queryUsers = users.map(user => QueryUserInfo(user.id, user.email, user.title, user.name, user.admin))
              complete(OK, ResponseSeqJson[QueryUserInfo](getHeader(200, session), queryUsers))
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

}
