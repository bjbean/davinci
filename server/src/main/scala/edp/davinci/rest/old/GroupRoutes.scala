//package edp.davinci.rest.old
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.Directives
//import edp.davinci.module.{ConfigurationModule, PersistenceModule}
//import edp.davinci.persistence.entities.Group
//import edp.davinci.rest.SessionClass
//import edp.davinci.util.Utils._
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations.{ApiOperation, ApiResponses, _}
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//@Api(value = "/groups", consumes = "application/json", produces = "application/json")
//@Path("/groups")
//class GroupRoutes(modules: ConfigurationModule with PersistenceModule) extends Directives {
//
//  val routes = getGroupByAllRoute ~ getGroupByIdRoute ~
////    deleteGroupByIdRoute ~ deleteGroupByAllRoute ~
//    postGroupRoute ~
//    putGroupRoute
//
//  @Path("/{groupId}")
//  @ApiOperation(value = "get one group from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "groupId", value = "group id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 404, message = "group not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getGroupByIdRoute = path("groups" / LongNumber) { groupId =>
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            onComplete(modules.groupDal.findById(groupId).mapTo[Option[Group]]) {
//              case Success(groupOpt) => groupOpt match {
//                case Some(group) => complete(OK, group)
//                case None => complete(NotFound, "group not found")
//              }
//              case Failure(ex) => complete(InternalServerError, "internal server error")
//            }
//          }
//      }
//    }
//  }
//
//  @ApiOperation(value = "get all group with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "group is not admin"),
//    new ApiResponse(code = 404, message = "groups not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getGroupByAllRoute = path("groups") {
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            if (session.admin) {
//              onComplete(modules.groupDal.findByFilter(
//                group => group.domain_id === session.domainId && group.active === true).mapTo[Seq[Group]]) {
//                case Success(groupSeq) =>
//                  if (groupSeq.nonEmpty) complete(OK, groupSeq)
//                  else complete(NotFound, "groups not found")
//                case Failure(ex) => complete(InternalServerError, "internal server error")
//              }
//            } else complete(Forbidden, "group is not admin")
//          }
//      }
//    }
//  }
//
//
////  @Path("/{groupId}")
////  @ApiOperation(value = "delete group by id", notes = "", nickname = "", httpMethod = "DELETE")
////  @ApiImplicitParams(Array(
////    new ApiImplicitParam(name = "groupId", value = "group id", required = true, dataType = "integer", paramType = "path")
////  ))
////  @ApiResponses(Array(
////    new ApiResponse(code = 200, message = "delete success"),
////    new ApiResponse(code = 403, message = "group is not admin"),
////    new ApiResponse(code = 401, message = "authorization error"),
////    new ApiResponse(code = 500, message = "internal server error")
////  ))
////  def deleteGroupByIdRoute = path("groups" / LongNumber) {
////    groupId =>
////      delete {
////        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
////          session =>
////            responseHeaderWithToken(session) {
////              if (session.admin)
////                onComplete(modules.groupDal.softDeleteById(groupId, session.userId).mapTo[Int]) {
////                  case Success(num) => complete(HttpResponse(OK, entity = "delete success"))
////                  case Failure(ex) => complete(InternalServerError, "internal server error")
////                } else complete(Forbidden, "group is not admin")
////            }
////        }
////      }
////  }
////
////  @ApiOperation(value = "delete all groups with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
////  @ApiResponses(Array(
////    new ApiResponse(code = 200, message = "delete success"),
////    new ApiResponse(code = 403, message = "group is not admin"),
////    new ApiResponse(code = 401, message = "authorization error"),
////    new ApiResponse(code = 500, message = "internal server error")
////  ))
////  def deleteGroupByAllRoute = path("groups") {
////    delete {
////      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
////        session =>
////          responseHeaderWithToken(session) {
////            if (session.admin)
////              onComplete(modules.groupDal.softDeleteByAll(
////                group => group.domain_id === session.domainId && group.active === true)(session.userId).mapTo[Unit]) {
////                case Success(num) => complete(HttpResponse(OK, entity = "delete success"))
////                case Failure(ex) => complete(InternalServerError, "internal server error")
////              } else complete(Forbidden, "group is not admin")
////          }
////      }
////    }
////  }
//
//
//  @ApiOperation(value = "Add a new group to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "group", value = "Group object to be added", required = true, dataType = "edp.davinci.rest.PostGroupClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "group is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def postGroupRoute = path("groups") {
//    post {
//      entity(as[PostGroupClass]) {
//        postGroup =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                if (session.admin)
//                  onComplete(modules.groupDal.insert(generateGroup(postGroup, session)).mapTo[Long]) {
//                    case Success(num) => complete(HttpResponse(OK, entity = "post success"))
//                    case Failure(ex) => complete(InternalServerError, "internal server error")
//                  } else complete(Forbidden, "group is not admin")
//              }
//          }
//      }
//    }
//  }
//
//
//  @ApiOperation(value = "update a group in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "group", value = "Group object to be updated", required = true, dataType = "edp.davinci.rest.PostGroupClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "group is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def putGroupRoute = path("groups") {
//    put {
//      entity(as[PostGroupClass]) {
//        postGroup =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                if (session.admin)
//                  onComplete(modules.groupDal.insert(generateGroup(postGroup, session)).mapTo[Long]) {
//                    case Success(num) => complete(HttpResponse(OK, entity = "post success"))
//                    case Failure(ex) => complete(InternalServerError, "internal server error")
//                  } else complete(Forbidden, "group is not admin")
//              }
//          }
//      }
//    }
//  }
//
//
//  private def generateGroup(postGroup: PostGroupClass, session: SessionClass): Group =
//    Group(0, session.domainId, postGroup.name, postGroup.desc, true, currentTime, session.userId, currentTime, session.userId)
//
//}
