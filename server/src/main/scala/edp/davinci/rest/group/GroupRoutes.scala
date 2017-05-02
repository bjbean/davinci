package edp.davinci.rest.group

import javax.ws.rs.Path
import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities.{PostGroupInfo, PutGroupInfo, UserGroup}
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import scala.util.{Failure, Success}

@Api(value = "/groups", consumes = "application/json", produces = "application/json")
@Path("/groups")
class GroupRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getGroupByAllRoute ~ postGroupRoute ~ putGroupRoute ~ deleteGroupByIdRoute
  private lazy val groupService = new GroupService(modules)

  @ApiOperation(value = "get all group with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getGroupByAllRoute: Route = path("groups") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllGroupsComplete(session)
      }
    }
  }

  //  @Path("{page=\\d+&size=\\d+}")
  //  @ApiOperation(value = "get groups with pagenifation", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 403, message = "user is not admin"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getGroupByPageRoute = modules.groupRoutes.pagenifationRoute("groups")


  @ApiOperation(value = "Add a new group to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group", value = "Group object to be added", required = true, dataType = "edp.davinci.rest.PostGroupInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postGroupRoute: Route = path("groups") {
    post {
      entity(as[PostGroupInfoSeq]) {
        groupSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postGroup(session, groupSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update a group in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group", value = "Group object to be updated", required = true, dataType = "edp.davinci.rest.PutGroupInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "group not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putGroupRoute: Route = path("groups") {
    put {
      entity(as[PutGroupInfoSeq]) {
        groupSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putUserComplete(session, groupSeq.payload)
          }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete group by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "group id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteGroupByIdRoute: Route = modules.groupRoutes.deleteByIdRoute("groups")


  private def getAllGroupsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(groupService.getAll) {
        case Success(groupSeq) =>
          if (groupSeq.nonEmpty) complete(OK, ResponseSeqJson[PutGroupInfo](getHeader(200, session), groupSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  private def putUserComplete(session: SessionClass, groupSeq: Seq[PutGroupInfo]): Route = {
    if (session.admin) {
      val future = groupService.update(groupSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  private def postGroup(session: SessionClass, postGroupSeq: Seq[PostGroupInfo]) = {
    if (session.admin) {
      val groupSeq = postGroupSeq.map(post => UserGroup(0, post.name, post.desc, active = true, null, session.userId, null, session.userId))
      onComplete(modules.groupDal.insert(groupSeq)) {
        case Success(groupWithIdSeq) =>
          val responseGroup = groupWithIdSeq.map(group => PutGroupInfo(group.id, group.name, group.desc))
          complete(OK, ResponseSeqJson[PutGroupInfo](getHeader(200, session), responseGroup))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))

  }

}
