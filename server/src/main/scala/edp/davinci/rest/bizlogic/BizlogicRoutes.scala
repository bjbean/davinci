package edp.davinci.rest.bizlogic

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities.{Bizlogic, PostBizlogicInfo, PostRelGroupBizlogic}
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import slick.jdbc.MySQLProfile.api._

import scala.util.{Failure, Success, Try}

@Api(value = "/bizlogics", consumes = "application/json", produces = "application/json")
@Path("/bizlogics")
class BizlogicRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with BizlogicService {

  val routes: Route = postBizlogicRoute ~ putBizlogicRoute ~ getBizlogicByAllRoute ~ deleteBizlogicByIdRoute

  //  @Path("/{id}")
  //  @ApiOperation(value = "get one bizlogic from system by id", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 404, message = "bizlogic not found"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getBizlogicByIdRoute: Route = modules.bizlogicRoutes.getByIdRoute("bizlogics")
  //
  //
  //  @Path("/{name}")
  //  @ApiOperation(value = "get one bizlogic from system by name", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "name", value = "bizlogic name", required = true, dataType = "String", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 404, message = "bizlogic not found"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getBizlogicByNameRoute: Route = modules.bizlogicRoutes.getByNameRoute("bizlogics")

  @ApiOperation(value = "get all bizlogics", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getBizlogicByAllRoute: Route = path("bizlogics") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllBizlogicsComplete(session)
      }
    }
  }


  @ApiOperation(value = "Add new bizlogics to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "bizlogics", value = "Bizlogic objects to be added", required = true, dataType = "edp.davinci.rest.PostBizlogicInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postBizlogicRoute: Route = path("bizlogics") {
    post {
      entity(as[PostBizlogicInfoSeq]) {
        bizlogicSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postBizlogic(session, bizlogicSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update bizlogics in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "bizlogic", value = "Bizlogic objects to be updated", required = true, dataType = "edp.davinci.rest.PutBizlogicInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "bizlogics not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putBizlogicRoute: Route = path("bizlogics") {
    put {
      entity(as[PutBizlogicInfoSeq]) {
        bizlogicSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putBizlogicComplete(session, bizlogicSeq.payload)
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
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteBizlogicByIdRoute: Route = modules.bizlogicRoutes.deleteByIdRoute("bizlogics")


}

