package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._

@Api(value = "/domains", consumes = "application/json", produces = "application/json")
@Path("/domains")
class DomainRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getDomainByIdRoute ~ getDomainByPageRoute ~ postDomainRoute ~ putDomainRoute ~ putDomainBatchRoute ~ deleteDomainByIdRoute ~ deleteDomainByBatchRoute

  @Path("/{id}")
  @ApiOperation(value = "get one domain from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "domain id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "domain not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDomainByIdRoute: Route = modules.domainRoutes.getByIdRoute("domains")


  @Path("{page=\\d+&size=\\d+}")
  @ApiOperation(value = "get all domain with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDomainByPageRoute: Route = modules.domainRoutes.paginateRoute("domains", "id")


  @ApiOperation(value = "Add a new domain to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "domain", value = "Domain object to be added", required = true, dataType = "edp.davinci.rest.DomainClassSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postDomainRoute: Route = path("domains") {
    post {
      entity(as[RequestSeqJson[DomainClass]]) {
        request =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.domainRoutes.postComplete(session, request.payload)
          }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "update a domain in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "domain id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "domain", value = "Domain object to be updated", required = true, dataType = "edp.davinci.rest.DomainClass", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "domain not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putDomainRoute: Route = path("domains" / LongNumber) { id => {
    put {
      entity(as[DomainClass]) {
        domain =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.domainRoutes.putComplete(session, domain, id)
          }
      }
    }
  }
  }

  @Path("batch_update")
  @ApiOperation(value = "update n domains in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "domain", value = "Domain object to be updated", required = true, dataType = "edp.davinci.rest.DomainClassSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "domain not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putDomainBatchRoute: Route = path("domains" / "batch_update") {
    post {
      entity(as[RequestSeqJson[DomainClass]]) {
        request => {
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.domainRoutes.postComplete(session, request.payload)
          }
        }
      }
    }
  }


  @Path("/{id}")
  @ApiOperation(value = "delete domain by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "domain id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteDomainByIdRoute: Route = modules.domainRoutes.deleteByIdRoute("domains")



  @ApiOperation(value = "delete domain by batch", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "domain id", required = true, dataType = "String", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteDomainByBatchRoute: Route = modules.domainRoutes.deleteByBatchRoute("domains")

}
