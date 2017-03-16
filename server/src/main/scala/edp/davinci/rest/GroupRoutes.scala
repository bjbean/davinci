package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
import scala.util.{Failure, Success}

@Api(value = "/groups", consumes = "application/json", produces = "application/json")
@Path("/groups")
class GroupRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getGroupByIdRoute ~ getGroupByNameRoute ~ getGroupByAllRoute ~ getBizlogicsByGroupIdRoute ~ getUsersByGroupIdRoute ~ getSourcesByGroupIdRoute ~ postGroupRoute ~ postUser2GroupRoute ~ postBizlogic2GroupRoute ~ putGroupRoute ~ deleteGroupByIdRoute ~ deleteBizlogicFromGroupRoute ~ deleteUserFromGroupRoute

  @Path("/{id}")
  @ApiOperation(value = "get one group from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "group id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "group not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getGroupByIdRoute: Route = modules.groupRoutes.getByIdRoute("groups")


  @Path("/{name}")
  @ApiOperation(value = "get group from system by name", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name", value = "group name", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "group not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getGroupByNameRoute: Route = modules.groupRoutes.getByNameRoute("groups")

  @ApiOperation(value = "get all group with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getGroupByAllRoute: Route = modules.groupRoutes.getByAllRoute("groups")

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
    new ApiImplicitParam(name = "group", value = "Group object to be added", required = true, dataType = "edp.davinci.rest.SimpleGroupSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postGroupRoute: Route = path("groups") {
    post {
      entity(as[SimpleGroupSeq]) {
        groupSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.groupRoutes.postComplete(session, groupSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update a group in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group", value = "Group object to be updated", required = true, dataType = "edp.davinci.rest.GroupSeq", paramType = "body")
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
      entity(as[GroupSeq]) {
        groupSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.groupRoutes.putComplete(session, groupSeq.payload)
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

  @Path("/{group_id}/users")
  @ApiOperation(value = "get users in a group", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getUsersByGroupIdRoute: Route = path("groups" / LongNumber / "users") { groupId =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          val query = (modules.relUserGroupQuery.filter(obj => obj.group_id === groupId && obj.active === true) join
            modules.userQuery.filter(_.active === true) on (_.user_id === _.id)).map(r => (r._2.id, r._2.name)).result
          val future = modules.db.run(query).mapTo[Seq[BaseInfo]]
          getByGroupIdComplete(future, session)
      }
    }
  }


  @Path("/{group_id}/users")
  @ApiOperation(value = "Add user to a group", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "relUserGroup", value = "relUserGroup object to be added", required = true, dataType = "edp.davinci.rest.SimpleRelUserGroupSeq", paramType = "body")

  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postUser2GroupRoute: Route = pathPrefix("groups" / LongNumber) { _ =>
    path("users") {
      post {
        entity(as[SimpleRelUserGroupSeq]) {
          simpleRelUserGroupSeq =>
            authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
              session => modules.relUserGroupRoutes.postComplete(session, simpleRelUserGroupSeq.payload)
            }
        }
      }
    }
  }


  @Path("/{group_id}/users/{user_id}")
  @ApiOperation(value = "remove user from group by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "user_id", value = "user id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteUserFromGroupRoute: Route = path("groups" / LongNumber / "users" / LongNumber) { (groupId, userId) =>
    delete {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          val future: Future[Int] = modules.relUserGroupDal.deleteByFilter(obj => obj.user_id === userId && obj.group_id === groupId).mapTo[Int]
          deleteFromGroupComplete(future, session)
      }
    }
  }


  @Path("/{group_id}/bizlogics")
  @ApiOperation(value = "get bizlogics in a group", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getBizlogicsByGroupIdRoute: Route = path("groups" / LongNumber / "bizlogics") {
    groupId =>
      get {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session =>
            val query = (modules.relGroupBizlogicQuery.filter(obj => obj.group_id === groupId && obj.active === true) join
              modules.bizlogicQuery.filter(_.active === true) on (_.bizlogic_id === _.id)).map(r => (r._2.id, r._2.name)).result
            val future = modules.db.run(query).mapTo[Seq[BaseInfo]]
            getByGroupIdComplete(future, session)
        }
      }
  }

  @Path("/{group_id}/bizlogics")
  @ApiOperation(value = "Add bizlogics to a group", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "relGroupBizlogic", value = "relGroupBizlogic object to be added", required = true, dataType = "edp.davinci.rest.SimpleRelGroupBizlogicSeq", paramType = "body")

  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postBizlogic2GroupRoute: Route = path("groups" / LongNumber / "bizlogics") {
    _ =>
      post {
        entity(as[SimpleRelGroupBizlogicSeq]) {
          simpleRelGroupBizlogicSeq =>
            authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
              session => modules.relGroupBizlogicRoutes.postComplete(session, simpleRelGroupBizlogicSeq.payload)
            }
        }
      }
  }

  @Path("/{group_id}/bizlogics/{bizlogic_id}")
  @ApiOperation(value = "remove bizlogic from group by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "bizlogic_id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteBizlogicFromGroupRoute: Route = path("groups" / LongNumber / "bizlogics" / LongNumber) {
    (groupId, bizlogicId) =>
      delete {
        authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
          session =>
            val future = modules.relGroupBizlogicDal.deleteByFilter(obj => obj.bizlogic_id === bizlogicId && obj.group_id === groupId)
            deleteFromGroupComplete(future, session)
        }
      }
  }


  @Path("/{group_id}/sources")
  @ApiOperation(value = "get sources in a group", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group_id", value = "group id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSourcesByGroupIdRoute: Route = path("groups" / LongNumber / "sources") { groupId =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          val future = modules.sourceDal.findAll(obj => obj.group_id === groupId && obj.active === true)
          getByGroupIdComplete(future, session)
      }
    }
  }

  private def getByGroupIdComplete(future: Future[Seq[BaseInfo]], session: SessionClass): Route = {
    onComplete(future) {
      case Success(baseSeq) =>
        if (baseSeq.nonEmpty) complete(OK, ResponseSeqJson[BaseInfo](getHeader(200, session), baseSeq))
        else complete(NotFound, getHeader(404, session))

      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  private def deleteFromGroupComplete(future: Future[Int], session: SessionClass): Route = {
    if (session.admin)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      } else complete(Forbidden, getHeader(403, session))
  }

}
