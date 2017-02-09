//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.entities.JsonProtocol._
//import edp.davinci.persistence.entities.{Source, SourceTable}
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations._
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//@Api(value = "/sources", consumes = "application/json", produces = "application/json")
//@Path("/sources")
//class SourceRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//
//  val routes = getSourceByIdRoute ~ getSourceByAllRoute ~ getSourceByPageRoute ~ postSourceRoute ~ putSourceRoute ~ deleteSourceByIdRoute ~ deleteSourceByAllRoute
//
//  @Path("/{id}")
//  @ApiOperation(value = "get one source from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 404, message = "source not found"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSourceByIdRoute: Route = modules.sourceRoutes.getByIdRoute("sources")
//
//  @ApiOperation(value = "get all source with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSourceByAllRoute = modules.sourceRoutes.getByAllRoute("sources", "domain_id")
//
//  @Path("{page=\\d+&size=\\d+}")
//  @ApiOperation(value = "get sources with pagenifation", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSourceByPageRoute = modules.sourceRoutes.pagenifationRoute("sources")
//
//
//  @ApiOperation(value = "Add a new source to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "source", value = "Source object to be added", required = true, dataType = "edp.davinci.rest.SourceClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def postSourceRoute = path("sources") {
//    post {
//      entity(as[SourceClass]) {
//        baseclass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                modules.sourceRoutes.postComplete(session, baseclass, modules.userDal.findByFilter(table => table.name === baseclass.name))
//              }
//          }
//      }
//    }
//  }
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a source in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "source", value = "Source object to be updated", required = true, dataType = "edp.davinci.rest.SourceClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "source not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def putSourceRoute = modules.sourceRoutes.putRoute("sources")
//
//  @Path("/{id}")
//  @ApiOperation(value = "delete source by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteSourceByIdRoute = modules.sourceRoutes.deleteByIdRoute("sources")
//
//  @ApiOperation(value = "delete all sources with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteSourceByAllRoute = modules.sourceRoutes.deleteByAllRoute("sources")
//}
