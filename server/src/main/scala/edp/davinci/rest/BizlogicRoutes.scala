package edp.davinci.rest

import javax.ws.rs.Path

import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._

@Api(value = "/bizlogics", consumes = "application/json", produces = "application/json")
@Path("/bizlogics")
class BizlogicRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes = getBizlogicByIdRoute ~ getBizlogicByNameRoute ~ postBizlogicRoute ~ putBizlogicRoute ~ getBizlogicByAllRoute ~ deleteBizlogicByIdRoute

  @Path("/{id}")
  @ApiOperation(value = "get one bizlogic from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "bizlogic not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getBizlogicByIdRoute: Route = modules.bizlogicRoutes.getByIdRoute("bizlogics")


  @Path("/{name}")
  @ApiOperation(value = "get one bizlogic from system by name", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name", value = "bizlogic name", required = true, dataType = "String", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "bizlogic not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getBizlogicByNameRoute: Route = modules.bizlogicRoutes.getByNameRoute("bizlogics")

  @ApiOperation(value = "get all bizlogics with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "bizlogic is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getBizlogicByAllRoute = modules.bizlogicRoutes.getByAllRoute("bizlogics")

  @ApiOperation(value = "Add new bizlogics to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "bizlogics", value = "Bizlogic objects to be added", required = true, dataType = "edp.davinci.rest.SimpleBizlogicSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "bizlogic is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postBizlogicRoute = path("bizlogics") {
    post {
      entity(as[SimpleBizlogicSeq]) {
        bizlogicSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.bizlogicRoutes.postComplete(session, bizlogicSeq.payload)
          }
      }
    }
  }

  @ApiOperation(value = "update bizlogics in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "bizlogic", value = "Bizlogic objects to be updated", required = true, dataType = "edp.davinci.rest.BizlogicSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "bizlogic is not admin"),
    new ApiResponse(code = 404, message = "bizlogics not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putBizlogicRoute = path("bizlogics") {
    put {
      entity(as[BizlogicSeq]) {
        bizlogicSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.bizlogicRoutes.putComplete(session, bizlogicSeq.payload)
          }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete bizlogic by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "bizlogic is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteBizlogicByIdRoute = modules.bizlogicRoutes.deleteByIdRoute("bizlogics")
}