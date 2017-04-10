package edp.davinci.rest.widget

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities.{PostWidgetInfo, PutWidgetInfo, Widget}
import edp.davinci.rest.{ResponseSeqJson, SessionClass}
import edp.davinci.util.CommonUtils
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait WidgetRepository extends ConfigurationModuleImpl with PersistenceModuleImpl {
  def getAll(session: SessionClass): Future[Seq[PutWidgetInfo]] = {
    if (session.admin)
      db.run(widgetQuery.filter(_.active === true).map(r => (r.id, r.bizlogic_id, r.widgetlib_id, r.name, r.desc, r.trigger_type, r.trigger_params, r.publish)).result).mapTo[Seq[PutWidgetInfo]]
    else
      db.run(widgetQuery.filter(obj => obj.active === true && obj.publish === true).map(r => (r.id, r.bizlogic_id, r.widgetlib_id, r.name, r.desc, r.trigger_type, r.trigger_params, r.publish)).result).mapTo[Seq[PutWidgetInfo]]
  }

  def update(widgetSeq: Seq[PutWidgetInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(widgetSeq.map(r => {
      widgetQuery.filter(_.id === r.id).map(widget => (widget.bizlogic_id, widget.widgetlib_id, widget.name, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish, widget.update_by, widget.update_time))
        .update(r.bizlogic_id, r.widgetlib_id, r.name, r.desc, r.trigger_type, r.trigger_params, r.publish, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }
}

trait WidgetService extends Directives with WidgetRepository {
  def getAllWidgetsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(getAll(session)) {
        case Success(widgetSeq) =>
          if (widgetSeq.nonEmpty) complete(OK, ResponseSeqJson[PutWidgetInfo](getHeader(200, session), widgetSeq))
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
      val widgetSeq = postWidgetSeq.map(post => Widget(0, post.widgetlib_id, post.bizlogic_id, post.name, post.desc, post.trigger_type, post.trigger_params, post.publish, active = true, null, session.userId, null, session.userId))
      onComplete(widgetDal.insert(widgetSeq)) {
        case Success(widgetWithIdSeq) =>
          val responseWidget = widgetWithIdSeq.map(widget => PutWidgetInfo(widget.id, widget.widgetlib_id, widget.bizlogic_id, widget.name, widget.desc, widget.trigger_type, widget.trigger_params, widget.publish))
          complete(OK, ResponseSeqJson[PutWidgetInfo](getHeader(200, session), responseWidget))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }
}
