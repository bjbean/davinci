//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.entities.JsonProtocol._
//import edp.davinci.persistence.entities.{Group, GroupTable}
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations._
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//@Api(value = "/groups", consumes = "application/json", produces = "application/json")
//@Path("/groups")
//class GroupRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//
//  val routes = getGroupByIdRoute ~ getGroupByAllRoute ~ getGroupByPageRoute ~ postGroupRoute ~ putGroupRoute ~ deleteGroupByIdRoute ~ deleteGroupByAllRoute
//
//  @Path("/{id}")
//  @ApiOperation(value = "get one group from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "group id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 404, message = "group not found"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getGroupByIdRoute: Route = modules.groupRoutes.getByIdRoute("groups")
//
//  @ApiOperation(value = "get all group with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getGroupByAllRoute = modules.groupRoutes.getByAllRoute("groups", "domain_id")
//
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
//
//
//  @ApiOperation(value = "Add a new group to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "group", value = "Group object to be added", required = true, dataType = "edp.davinci.rest.GroupClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def postGroupRoute = path("groups") {
//    post {
//      entity(as[GroupClass]) {
//        baseclass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                modules.groupRoutes.postComplete(session, baseclass, modules.userDal.findByFilter(table => table.name === baseclass.name))
//              }
//          }
//      }
//    }
//  }
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a group in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "group id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "group", value = "Group object to be updated", required = true, dataType = "edp.davinci.rest.GroupClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "group not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def putGroupRoute = modules.groupRoutes.putRoute("groups")
//
//  @Path("/{id}")
//  @ApiOperation(value = "delete group by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "group id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteGroupByIdRoute = modules.groupRoutes.deleteByIdRoute("groups")
//
//  @ApiOperation(value = "delete all groups with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteGroupByAllRoute = modules.groupRoutes.deleteByAllRoute("groups")
//}
