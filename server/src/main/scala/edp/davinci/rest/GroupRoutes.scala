package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._


@Api(value = "/groups", consumes = "application/json", produces = "application/json")
@Path("/groups")
class GroupRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getGroupByIdRoute ~ getGroupByNameRoute ~ getGroupByAllRoute ~ postGroupRoute ~ putGroupRoute ~ deleteGroupByIdRoute

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
  @ApiOperation(value = "get one group from system by name", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name", value = "group name", required = true, dataType = "string", paramType = "path")
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
  //  def getGroupByPageRoute: Route = modules.groupRoutes.paginateRoute("groups","domain_id")


  @ApiOperation(value = "Add new groups to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "groups", value = "Group objects to be added", required = true, dataType = "edp.davinci.rest.SimpleGroupSeq", paramType = "body")
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


  @ApiOperation(value = "update groups in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "groups", value = "Group objects to be updated", required = true, dataType = "edp.davinci.rest.GroupSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "groups not found"),
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


}
