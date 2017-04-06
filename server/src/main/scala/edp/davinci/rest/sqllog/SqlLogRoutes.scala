package edp.davinci.rest.sqllog

import javax.ws.rs.Path

import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.rest.{SessionClass, SimpleSqlLogSeq, SqlLogSeq}
import edp.davinci.util.AuthorizationProvider
import io.swagger.annotations._
import edp.davinci.util.JsonProtocol._

@Api(value = "/sqlLogs", consumes = "application/json", produces = "application/json")
@Path("/sqlLogs")
class SqlLogRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with SqlLogService {

  val routes: Route = getSqlByAllRoute ~ postSqlLogRoute ~ putSqlLogRoute

  @ApiOperation(value = "get all sqlLogs", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getSqlByAllRoute: Route = path("sqlLogs") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllLogsComplete(session)
      }
    }
  }


  @ApiOperation(value = "Add sqlLog to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sqlLog", value = "sqlLog objects to be added", required = true, dataType = "edp.davinci.rest.SimpleSqlLogSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postSqlLogRoute: Route = path("sqlLogs") {
    post {
      entity(as[SimpleSqlLogSeq]) {
        sqlLogSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => modules.sqlLogRoutes.postComplete(session, sqlLogSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update sqlLog in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sqlLog", value = "sqlLog objects to be updated", required = true, dataType = "edp.davinci.rest.SqlLogSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "sources not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putSqlLogRoute: Route = path("sqlLogs") {
    put {
      entity(as[SqlLogSeq]) {
        sqlLogSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putSqlLogComplete(session, sqlLogSeq.payload)
          }
      }
    }
  }

}
