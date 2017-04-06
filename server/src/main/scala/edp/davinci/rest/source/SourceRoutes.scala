package edp.davinci.rest.source

import javax.ws.rs.Path
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.rest.{PostSourceInfoSeq, PutSourceInfoSeq, SessionClass}
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._


@Api(value = "/sources", consumes = "application/json", produces = "application/json")
@Path("/sources")
class SourceRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with SourceService {

  val routes: Route = getSourceByAllRoute ~ postSourceRoute ~ putSourceRoute ~ deleteSourceByIdRoute

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
//  @Path("/{name}")
//  @ApiOperation(value = "get one source from system by name", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "name", value = "source name", required = true, dataType = "string", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 404, message = "source not found"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSourceByNameRoute: Route = modules.sourceRoutes.getByNameRoute("sources")


  @ApiOperation(value = "get all source with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSourceByAllRoute: Route = path("sources") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllSourcesComplete(session)
      }
    }
  }

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
  //  def getSourceByPageRoute: Route = modules.sourceRoutes.paginateRoute("sources","domain_id")


  @ApiOperation(value = "Add new sources to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sources", value = "Source objects to be added", required = true, dataType = "edp.davinci.rest.PostSourceInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postSourceRoute: Route = path("sources") {
    post {
      entity(as[PostSourceInfoSeq]) {
        sourceSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postSource(session,sourceSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update sources in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sources", value = "Source objects to be updated", required = true, dataType = "edp.davinci.rest.PutSourceInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "sources not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putSourceRoute: Route = path("sources") {
    put {
      entity(as[PutSourceInfoSeq]) {
        sourceSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putSourceComplete(session,sourceSeq.payload)
          }
      }
    }
  }


  @Path("/{id}")
  @ApiOperation(value = "delete source by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteSourceByIdRoute: Route = modules.sourceRoutes.deleteByIdRoute("sources")


}
