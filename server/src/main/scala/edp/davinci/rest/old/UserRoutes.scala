//package edp.davinci.rest.old
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.Directives
//import edp.davinci.module.{ConfigurationModule, PersistenceModule}
//import edp.davinci.persistence.entities.User
//import edp.davinci.rest.{SessionClass, UserClass}
//import edp.davinci.util.Utils._
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations.{ApiOperation, ApiResponses, _}
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//@Api(value = "/users", consumes = "application/json", produces = "application/json")
//@Path("/users")
//class UserRoutes(modules: ConfigurationModule with PersistenceModule) extends Directives {
//
//  val routes = getUserByAllRoute ~ getUserByIdRoute ~
////    deleteUserByIdRoute ~ deleteUserByAllRoute ~
//    postUserRoute ~
//    putUserRoute
//
//  @Path("/{userId}")
//  @ApiOperation(value = "get one user from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "userId", value = "user id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 404, message = "user not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getUserByIdRoute = path("users" / LongNumber) { userId =>
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            onComplete(modules.userDal.findById(userId).mapTo[Option[User]]) {
//              case Success(userOpt) => userOpt match {
//                case Some(user) => complete(OK, user)
//                case None => complete(NotFound, "user not found")
//              }
//              case Failure(ex) => complete(InternalServerError, "internal server error")
//            }
//          }
//      }
//    }
//  }
//
//  @ApiOperation(value = "get all user with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "users not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getUserByAllRoute = path("users") {
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            if (session.admin) {
//              onComplete(modules.userDal.findByFilter(
//                user => user.domain_id === session.domainId && user.active === true).mapTo[Seq[User]]) {
//                case Success(userSeq) =>
//                  if (userSeq.nonEmpty) complete(OK, userSeq)
//                  else complete(NotFound, "users not found")
//                case Failure(ex) => complete(InternalServerError, "internal server error")
//              }
//            } else complete(Forbidden, "user is not admin")
//          }
//      }
//    }
//  }
//
////
////  @Path("/{userId}")
////  @ApiOperation(value = "delete user by id", notes = "", nickname = "", httpMethod = "DELETE")
////  @ApiImplicitParams(Array(
////    new ApiImplicitParam(name = "userId", value = "user id", required = true, dataType = "integer", paramType = "path")
////  ))
////  @ApiResponses(Array(
////    new ApiResponse(code = 200, message = "delete success"),
////    new ApiResponse(code = 403, message = "user is not admin"),
////    new ApiResponse(code = 401, message = "authorization error"),
////    new ApiResponse(code = 500, message = "internal server error")
////  ))
////  def deleteUserByIdRoute = path("users" / LongNumber) {
////    userId =>
////      delete {
////        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
////          session =>
////            responseHeaderWithToken(session) {
////              if (session.admin)
////                onComplete(modules.userDal.softDeleteById(userId, session.userId).mapTo[Int]) {
////                  case Success(num) => complete(HttpResponse(OK, entity = "delete success"))
////                  case Failure(ex) => complete(InternalServerError, "internal server error")
////                } else complete(Forbidden, "user is not admin")
////            }
////        }
////      }
////  }
////
////  @ApiOperation(value = "delete all users with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
////  @ApiResponses(Array(
////    new ApiResponse(code = 200, message = "delete success"),
////    new ApiResponse(code = 403, message = "user is not admin"),
////    new ApiResponse(code = 401, message = "authorization error"),
////    new ApiResponse(code = 500, message = "internal server error")
////  ))
////  def deleteUserByAllRoute = path("users") {
////    delete {
////      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
////        session =>
////          responseHeaderWithToken(session) {
////            if (session.admin)
////              onComplete(modules.userDal.softDeleteByAll(
////                user => user.domain_id === session.domainId && user.active === true)(session.userId).mapTo[Unit]) {
////                case Success(num) => complete(HttpResponse(OK, entity = "delete success"))
////                case Failure(ex) => complete(InternalServerError, "internal server error")
////              } else complete(Forbidden, "user is not admin")
////          }
////      }
////    }
////  }
//
//
//  @ApiOperation(value = "Add a new user to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "user", value = "User object to be added", required = true, dataType = "edp.davinci.rest.PostUserClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def postUserRoute = path("users") {
//    post{
//      entity(as[UserClass]) {
//        postUser =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                if (session.admin)
//                  onComplete(modules.userDal.insert(generateUser(postUser, session)).mapTo[Long]) {
//                    case Success(num) => complete(HttpResponse(OK, entity = "post success"))
//                    case Failure(ex) => complete(InternalServerError, "internal server error")
//                  } else complete(Forbidden, "user is not admin")
//              }
//          }
//      }
//    }
//  }
//
//
//  @ApiOperation(value = "update a user in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "user", value = "User object to be updated", required = true, dataType = "edp.davinci.rest.PostUserClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def putUserRoute = path("users") {
//    put{
//      entity(as[UserClass]) {
//        postUser =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                if (session.admin)
//                  onComplete(modules.userDal.insert(generateUser(postUser, session)).mapTo[Long]) {
//                    case Success(num) => complete(HttpResponse(OK, entity = "post success"))
//                    case Failure(ex) => complete(InternalServerError, "internal server error")
//                  } else complete(Forbidden, "user is not admin")
//              }
//          }
//      }
//    }
//  }
//
//
//  private def generateUser(postUser: UserClass, session: SessionClass): User =
//    User(0,
//      session.domainId,
//      postUser.email,
//      defaultPass,
//      postUser.title,
//      postUser.name,
//      false,
//      true,
//      currentTime,
//      session.userId,
//      currentTime,
//      session.userId)
//
//  def defaultPass = "123456"
//
//}
