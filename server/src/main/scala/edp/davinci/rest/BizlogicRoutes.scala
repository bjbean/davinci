//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, OK}
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.entities.{Bizlogic, BizlogicTable}
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations.{ApiOperation, ApiResponses, _}
//import edp.davinci.persistence.entities.JsonProtocol._
//
//import scala.util.{Failure, Success}
//import slick.jdbc.H2Profile.api._
//
//@Api(value = "/bizlogics", consumes = "application/json", produces = "application/json")
//@Path("/bizlogics")
//class BizlogicRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//
//  val routes: Route = getAllBizlogicRoute ~
//    getBizlogicByIdRoute ~
//    createBizlogicRoute ~
//    deleteBizlogicRoute ~
//    deleteAllBizlogics ~
//    updateBizlogicRoute
//
//  @ApiOperation(value = "list all bizlogics", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "bizlogics", value = "", required = false, dataType = "", paramType = "")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "bizlogics not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getAllBizlogicRoute: Route = modules.bizlogicRoutes.getByAllRoute("bizlogics", "domain_id")
//
//  @Path("/{id}")
//  @ApiOperation(value = "get one bizlogic from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "bizlogicid", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 404, message = "bizlogic not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getBizlogicByIdRoute: Route = modules.bizlogicRoutes.getByIdRoute("bizlogics")
//
//
//  @ApiOperation(value = "create a new bizlogic", nickname = "createBizlogic", httpMethod = "POST", consumes = "application/json")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "body", value = "Bizlogic object that needs to be created", required = true, dataType = "edp.davinci.rest.BizlogicClass", paramType = "body")))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def createBizlogicRoute: Route = path("bizlogics") {
//    post {
//      entity(as[BizlogicClass]) {
//        baseclass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                modules.bizlogicRoutes.postComplete(session, baseclass, modules.userDal.findByFilter(table => table.name === baseclass.name))
//              }
//          }
//      }
//    }
//  }
//
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a bizlogic in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "bizlogic", value = "Bizlogic object to be updated", required = true, dataType = "edp.davinci.rest.BizlogicClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "bizlogic not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def updateBizlogicRoute = modules.bizlogicRoutes.putRoute("bizlogics")
//
//
//  @Path("/{id}")
//  @ApiOperation(value = "Delete a bizlogic by id", nickname = "deleteBizlogic", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "bizlogicId", value = "Bizlogic id to delete", required = true, dataType = "integer", paramType = "path")))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteBizlogicRoute: Route = modules.bizlogicRoutes.deleteByIdRoute("bizlogics")
//
//  @ApiOperation(value = "delete all bizlogics with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteAllBizlogics: Route = modules.bizlogicRoutes.deleteByAllRoute("bizlogics")
//
//}
