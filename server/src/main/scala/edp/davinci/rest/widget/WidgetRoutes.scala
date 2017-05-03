package edp.davinci.rest.widget

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.{PostWidgetInfo, PutWidgetInfo, Widget}
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import edp.endurance.db.DbConnection
import io.swagger.annotations._
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
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
            session => postWidgetComplete(session, widgetSeq.payload)
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
        session => getWholeSqlComplete(session, widgetId)
      }
    }

  }

  private def getAllWidgetsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(widgetService.getAll(session)) {
        case Success(widgetSeq) =>
          val responseSeq: Seq[PutWidgetInfo] = widgetSeq.map(r => PutWidgetInfo(r._1, r._2, r._3, r._4, r._5.getOrElse(""), r._6, r._7, r._8, r._9))
          if (widgetSeq.nonEmpty)
            complete(OK, ResponseJson[Seq[PutWidgetInfo]](getHeader(200, session), responseSeq))
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

  //  private def postWidget(session: SessionClass, postWidgetSeq: Seq[PostWidgetInfo]): Route = {
  //    if (session.admin) {
  //      val widgetSeq = postWidgetSeq.map(post => Widget(0, post.widgetlib_id, post.bizlogic_id, post.name, Some(post.olap_sql), post.desc, post.trigger_type, post.trigger_params, post.publish, active = true, null, session.userId, null, session.userId))
  //      onComplete(modules.widgetDal.insert(widgetSeq)) {
  //        case Success(widgetWithIdSeq) =>
  //          val responseWidget: Seq[PutWidgetInfo] = widgetWithIdSeq
  //            .map(widget =>
  //
  //              PutWidgetInfo(widget.id, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.olap_sql.orNull, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish))
  //          complete(OK, ResponseSeqJson[PutWidgetInfo](getHeader(200, session), responseWidget))
  //        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
  //      }
  //    } else complete(Forbidden, getHeader(403, session))
  //  }

  private def postWidgetComplete(session: SessionClass, postWidgetSeq: Seq[PostWidgetInfo]): Route = {
    if (session.admin) {
      val widgetSeq = postWidgetSeq.map(post => Widget(0, post.widgetlib_id, post.bizlogic_id, post.name, Some(post.olap_sql), post.desc, post.trigger_type, post.trigger_params, post.publish, active = true, null, session.userId, null, session.userId))
      val widget = Await.result(modules.widgetDal.insert(widgetSeq), Duration.Inf).head
      val responseWidget = PutWidgetInfo(widget.id, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.olap_sql.orNull, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish)
      val operation = for {
        a <- widgetService.getSourceInfo(widget.bizlogic_id)
        b <- widgetService.getSql(widget.id)
      } yield (a, b)
      onComplete(operation) {
        case Success(info) =>
          val (connectionUrl, _) = info._1.head
          val resultSql = formatSql(info._2.head)
          val result = getResult(connectionUrl, resultSql)
          complete(OK, ResponseJson[BizlogicResult](getHeader(200, session), BizlogicResult(responseWidget, result)))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  private def getWholeSqlComplete(session: SessionClass, widgetId: Long): Route = {
    onComplete(widgetService.getSql(widgetId)) {
      case Success(sqlSeq) =>
        val resultSql = formatSql(sqlSeq.head)
        complete(OK, ResponseJson[String](getHeader(200, session), resultSql))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  private def formatSql(sqlInfo: (String, String, String)): String = {
    val (olapSql, sqlTmpl, result_table) = sqlInfo
    val sqlParts = olapSql.split("from")
    sqlParts(0) + s" from ($sqlTmpl as $result_table) " + sqlParts(1)
  }

  private def getResult(connectionUrl: String, sqls: String): List[Seq[String]] = {
    val resultList = new ListBuffer[Seq[String]]
    val columnList = new ListBuffer[String]
    if (connectionUrl != null) {
      val connectionInfo = connectionUrl.split("""<:>""")
      if (connectionInfo.size != 3)
        null.asInstanceOf[List[Seq[String]]]
      else {
        val dbConnection = DbConnection.getConnection(connectionInfo(0), connectionInfo(1), connectionInfo(2))
        val statement = dbConnection.createStatement()
        val resultSet = statement.executeQuery(sqls)
        val meta = resultSet.getMetaData
        for (i <- 1 to meta.getColumnCount)
          columnList.append(meta.getColumnName(i))
        resultList.append(columnList)
        while (resultSet.next())
          resultList.append(getRow(resultSet))
        resultList.toList
      }
    } else {
      null.asInstanceOf[List[Seq[String]]]
    }
  }


}
