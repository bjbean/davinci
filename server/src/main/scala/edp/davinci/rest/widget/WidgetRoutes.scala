package edp.davinci.rest.widget

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.{PostWidgetInfo, PutWidgetInfo, Widget}
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import edp.endurance.db.DbConnection
import io.swagger.annotations._

import scala.util.{Failure, Success}

@Api(value = "/widgets", consumes = "application/json", produces = "application/json")
@Path("/widgets")
class WidgetRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {
  val routes: Route = getAllWidgetsRoute ~ postWidgetRoute ~ deleteWidgetByIdRoute ~ putWidgetRoute ~ getWholeSqlByWidgetIdRoute
  private lazy val widgetService = new WidgetService(modules)

  @ApiOperation(value = "list all widgets", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widgets", value = "", required = false, dataType = "", paramType = "")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "widgets not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getAllWidgetsRoute: Route = path("widgets") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllWidgetsComplete(session)
      }
    }
  }


  @ApiOperation(value = "Add a new widget to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget", value = "Widget object to be added", required = true, dataType = "edp.davinci.rest.PostWidgetInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postWidgetRoute: Route = path("widgets") {
    post {
      entity(as[PostWidgetInfoSeq]) {
        widgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postWidget(session, widgetSeq.payload)
          }
      }
    }
  }


  @ApiOperation(value = "update widgets in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget", value = "Widget object to be updated", required = true, dataType = "edp.davinci.rest.PutWidgetInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putWidgetRoute: Route = path("widgets") {
    put {
      entity(as[PutWidgetInfoSeq]) {
        widgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putWidgetComplete(session, widgetSeq.payload)
          }
      }
    }
  }

  @Path("/{widgetId}")
  @ApiOperation(value = "delete widget by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widgetId", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteWidgetByIdRoute: Route = modules.widgetRoutes.deleteByIdRoute("widgets")

  @Path("/{widgetId}/sqls")
  @ApiOperation(value = "get whole sql by widget id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widgetId", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getWholeSqlByWidgetIdRoute: Route = path("widgets" / LongNumber / "sqls") { widgetId =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getWholeSql(session, widgetId)
      }
    }

  }

  private def getAllWidgetsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(widgetService.getAll(session)) {
        case Success(widgetSeq) =>
          val responseSeq: Seq[PutWidgetInfo] = widgetSeq.map(r => PutWidgetInfo(r._1, r._2, r._3, r._4, r._5.getOrElse(""), r._6, r._7, r._8, r._9))
          println("response " + responseSeq.size)
          if (widgetSeq.nonEmpty) {
            println("in if")
            complete(OK, ResponseJson[Seq[PutWidgetInfo]](getHeader(200, session), responseSeq))
          }
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  private def putWidgetComplete(session: SessionClass, putWidgetSeq: Seq[PutWidgetInfo]): Route = {
    if (session.admin) {
      val future = widgetService.update(putWidgetSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  private def postWidget(session: SessionClass, postWidgetSeq: Seq[PostWidgetInfo]): Route = {
    if (session.admin) {
      val widgetSeq = postWidgetSeq.map(post => Widget(0, post.widgetlib_id, post.bizlogic_id, post.name, Some(post.olap_sql), post.desc, post.trigger_type, post.trigger_params, post.publish, active = true, null, session.userId, null, session.userId))
      onComplete(modules.widgetDal.insert(widgetSeq)) {
        case Success(widgetWithIdSeq) =>
          val responseWidget: Seq[PutWidgetInfo] = widgetWithIdSeq.map(widget => PutWidgetInfo(widget.id, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.olap_sql.orNull, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish))
          complete(OK, ResponseSeqJson[PutWidgetInfo](getHeader(200, session), responseWidget))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  private def getWholeSql(session: SessionClass, widgetId: Long): Route = {
    onComplete(widgetService.getSql(widgetId)) {
      case Success(sqlSeq) =>
        val (olap_sql, sql_tmpl) = sqlSeq.head
        val sqlList = SqlInfo((sql_tmpl + s";$olap_sql").split(";").toList)
        complete(OK, ResponseJson[SqlInfo](getHeader(200, session), sqlList))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  private def getResult(bizlogicId: Long, session: SessionClass) = {
    onComplete(widgetService.getSourceInfo(bizlogicId)) {
      case Success(sourceInfo) =>
        val (connectionUrl, _) = sourceInfo.head
        if (connectionUrl != null) {
          val connectionInfo = connectionUrl.split("""<:>""")
          if (connectionInfo.size != 3)
            null
          else {
            val dbConnection = DbConnection.getConnection(connectionInfo(0), connectionInfo(1), connectionInfo(2))
            val statement = dbConnection.createStatement()
            val resultSet = statement.executeQuery("")
            null
          }
        } else {
          null
        }
      case Failure(_) =>
        null
    }
  }

}
