package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.util.JsonProtocol._
import edp.davinci.persistence.entities.Source
import edp.davinci.util.AuthorizationProvider
import io.swagger.annotations._


@Api(value = "/sources", consumes = "application/json", produces = "application/json")
@Path("/sources")
class SourceRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getSourceByIdRoute  ~ getSourceByPageRoute ~ postSourceRoute ~ putSourceRoute ~ deleteSourceByIdRoute

  @Path("/{id}")
  @ApiOperation(value = "get one source from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "source not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSourceByIdRoute: Route = modules.sourceRoutes.getByIdRoute("sources")

//  @ApiOperation(value = "get all source with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSourceByAllRoute = modules.sourceRoutes.getByAllRoute("sources", "domain_id")

  @Path("{page=\\d+&size=\\d+}")
  @ApiOperation(value = "get sources with pagenifation", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSourceByPageRoute: Route = modules.sourceRoutes.paginateRoute("sources","domain_id")


  @ApiOperation(value = "Add a new source to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "source", value = "Source object to be added", required = true, dataType = "edp.davinci.rest.SourceClass", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postSourceRoute: Route = path("sources") {
    post {
      entity(as[SourceClass]) {
        source =>authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize){
          session => modules.sourceRoutes.postComplete(session,source)
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "update a source in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "source", value = "Source object to be updated", required = true, dataType = "edp.davinci.persistence.entities.Source", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "source not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putSourceRoute: Route = path("sources" / LongNumber) {
    id =>
    put {
      entity(as[Source]) {
        source => authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize){
          session =>modules.sourceRoutes.putComplete(session,source,id)
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
