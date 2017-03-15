package edp.davinci.rest

import javax.ws.rs.Path
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._

@Api(value = "/sqls", consumes = "application/json", produces = "application/json")
@Path("/sqls")
class SqlRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getSqlByIdRoute ~ getSqlByAllRoute ~ postSqlRoute ~ putSqlRoute ~ deleteSqlByIdRoute

  @Path("/{id}")
  @ApiOperation(value = "get one sql from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "sql id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "sql not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSqlByIdRoute: Route = modules.sqlRoutes.getByIdRoute("sqls")

  @Path("/{name}")
  @ApiOperation(value = "get one sql from system by name", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name", value = "sql name", required = true, dataType = "string", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "sql not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSqlByNameRoute: Route = modules.sqlRoutes.getByNameRoute("sqls")

  @ApiOperation(value = "get all sql with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSqlByAllRoute: Route = modules.sqlRoutes.getByAllRoute("sqls")

  //  @Path("{page=\\d+&size=\\d+}")
  //  @ApiOperation(value = "get sqls with pagenifation", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 403, message = "user is not admin"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getSqlByPageRoute = modules.sqlRoutes.pagenifationRoute("sqls")


  @ApiOperation(value = "Add new sqls to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sqls", value = "Sql object to be added", required = true, dataType = "edp.davinci.rest.SimpleSqlSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postSqlRoute: Route = path("sqls") {
    post {
      entity(as[SimpleSqlSeq]) {
        sqlSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.sqlRoutes.postComplete(session, sqlSeq.payload)
          }
      }
    }
  }

  @ApiOperation(value = "update sqls in the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sqls", value = "Sql object to be updated", required = true, dataType = "edp.davinci.rest.SqlSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putSqlRoute: Route = path("sqls") {
    put {
      entity(as[SqlSeq]) {
        sqlSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.sqlRoutes.putComplete(session, sqlSeq.payload)
          }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete sql by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "sql id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteSqlByIdRoute: Route = modules.sqlRoutes.deleteByIdRoute("sqls")

}
