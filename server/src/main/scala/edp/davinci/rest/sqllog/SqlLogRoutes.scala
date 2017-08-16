package edp.davinci.rest.sqllog

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.SqlLog
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.ResponseUtils.getHeader
import io.swagger.annotations._
import edp.davinci.util.JsonProtocol._
import org.apache.log4j.Logger
import scala.util.{Failure, Success}

@Api(value = "/sqllogs", consumes = "application/json", produces = "application/json")
@Path("/sqllogs")
class SqlLogRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getSqlByAllRoute ~ postSqlLogRoute ~ putSqlLogRoute
  private lazy val sqlLogService = new SqlLogService(modules)
  private lazy val logger = Logger.getLogger(this.getClass)
  private lazy val routeName = "sqllogs"


  @ApiOperation(value = "get all sqlLogs", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "sources not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getSqlByAllRoute: Route = path(routeName) {
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session => getAllLogsComplete(session)
      }
    }
  }

  private def getAllLogsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(sqlLogService.getAll(session)) {
        case Success(logSeq) => complete(OK, ResponseSeqJson[SqlLog](getHeader(200, session), logSeq))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }


  @ApiOperation(value = "Add sqlLog to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sqlLog", value = "sqlLog objects to be added", required = true, dataType = "edp.davinci.rest.SimpleSqlLogSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "sources not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def postSqlLogRoute: Route = path(routeName) {
    post {
      entity(as[SimpleSqlLogSeq]) {
        sqlLogSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
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
    new ApiResponse(code = 400, message = "bad request")
  ))
  def putSqlLogRoute: Route = path(routeName) {
    put {
      entity(as[SqlLogSeq]) {
        sqlLogSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
            session => putSqlLogComplete(session, sqlLogSeq.payload)
          }
      }
    }
  }


  private def putSqlLogComplete(session: SessionClass, sqlLogSeq: Seq[SqlLog]): Route = {
    if (session.admin) {
      val future = sqlLogService.update(sqlLogSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }

}
