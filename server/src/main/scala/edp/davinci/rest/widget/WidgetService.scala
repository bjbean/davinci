package edp.davinci.rest.widget

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities.{PostWidgetInfo, PutWidgetInfo, Widget}
import edp.davinci.rest.{ResponseJson, ResponseSeqJson, SessionClass, SqlInfo}
import edp.davinci.util.CommonUtils
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import edp.endurance.db.DbConnection
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

trait WidgetRepository extends ConfigurationModuleImpl with PersistenceModuleImpl {
  def getAll(session: SessionClass) = {
    if (session.admin)
      db.run(widgetQuery.filter(_.active === true)
        .map(r => (r.id, r.widgetlib_id, r.bizlogic_id, r.name, r.olap_sql, r.desc, r.trigger_type, r.trigger_params, r.publish)).result)
    else
      db.run(widgetQuery.filter(obj => obj.active === true && obj.publish === true)
        .map(r => (r.id, r.widgetlib_id, r.bizlogic_id, r.name, r.olap_sql, r.desc, r.trigger_type, r.trigger_params, r.publish)).result)
  }

  def update(widgetSeq: Seq[PutWidgetInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(widgetSeq.map(r => {
      widgetQuery.filter(_.id === r.id).map(widget => (widget.bizlogic_id, widget.widgetlib_id, widget.name, widget.olap_sql, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish, widget.update_by, widget.update_time))
        .update(r.bizlogic_id, r.widgetlib_id, r.name, Some(r.olap_sql), r.desc, r.trigger_type, r.trigger_params, r.publish, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def getSql(widgetId: Long): Future[Seq[(String, String)]] = {
    val query = (widgetQuery.filter(obj => obj.id === widgetId && obj.active === true) join bizlogicQuery.filter(_.active === true) on (_.bizlogic_id === _.id))
      .map {
        case (w, b) => (w.olap_sql.getOrElse(null.asInstanceOf[String]), b.sql_tmpl)
      }.result
    db.run(query)
  }

  def getSourceInfo(bizlogicId: Long): Future[Seq[(String, String)]] = {
    val query = (bizlogicQuery.filter(obj => obj.active === true && obj.id === bizlogicId) join sourceQuery.filter(_.active === true) on (_.source_id === _.id))
      .map { case (_, s) => (s.connection_url, s.config) }.result
    db.run(query)
  }
}

trait WidgetService extends Directives with WidgetRepository {
  def getAllWidgetsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(getAll(session)) {
        case Success(widgetSeq) =>
          val responseSeq: Seq[PutWidgetInfo] = widgetSeq.map(r => PutWidgetInfo(r._1, r._2, r._3, r._4, r._5.getOrElse(""), r._6, r._7, r._8, r._9))
          println("response "+responseSeq.size)
          if (widgetSeq.nonEmpty) {
            println("in if")
            complete(OK, ResponseJson[Seq[PutWidgetInfo]](getHeader(200, session),responseSeq))
          }
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def putWidgetComplete(session: SessionClass, putWidgetSeq: Seq[PutWidgetInfo]): Route = {
    if (session.admin) {
      val future = update(putWidgetSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def postWidget(session: SessionClass, postWidgetSeq: Seq[PostWidgetInfo]): Route = {
    if (session.admin) {
      val widgetSeq = postWidgetSeq.map(post => Widget(0, post.widgetlib_id, post.bizlogic_id, post.name, Some(post.olap_sql), post.desc, post.trigger_type, post.trigger_params, post.publish, active = true, null, session.userId, null, session.userId))
      onComplete(widgetDal.insert(widgetSeq)) {
        case Success(widgetWithIdSeq) =>
          val responseWidget: Seq[PutWidgetInfo] = widgetWithIdSeq.map(widget => PutWidgetInfo(widget.id, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.olap_sql.orNull, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish))
          complete(OK, ResponseSeqJson[PutWidgetInfo](getHeader(200, session), responseWidget))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def getWholeSql(session: SessionClass, widgetId: Long): Route = {
    onComplete(getSql(widgetId)) {
      case Success(sqlSeq) =>
        val (olap_sql, sql_tmpl) = sqlSeq.head
        val sqlList = SqlInfo((sql_tmpl + s";$olap_sql").split(";").toList)
        complete(OK, ResponseJson[SqlInfo](getHeader(200, session), sqlList))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  def getResult(bizlogicId: Long, session: SessionClass) = {
    onComplete(getSourceInfo(bizlogicId)) {
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
