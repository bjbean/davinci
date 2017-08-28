package edp.davinci.rest.widget

import edp.davinci.ModuleInstance
import edp.davinci.module.DbModule._
import edp.davinci.persistence.entities._
import edp.davinci.rest.SessionClass
import edp.davinci.util.ResponseUtils
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future

object WidgetService extends WidgetService

trait WidgetService {
  private lazy val modules = ModuleInstance.getModule

  def getAll(session: SessionClass): Future[Seq[(Long, Long, Long, String, Option[String], String, Option[String], Boolean)]] = {
    if (session.admin)
      db.run(modules.widgetQuery.map(r => (r.id, r.widgetlib_id, r.flatTable_id, r.name, r.adhoc_sql, r.desc, r.chart_params, r.publish)).result)
    else {
      val query = (modules.widgetQuery.filter(_.publish)
        join modules.relGroupViewQuery.filter(r => r.group_id inSet session.groupIdList)
        on (_.flatTable_id === _.flatTable_id))
        .map {
          case (w, _) => (w.id, w.widgetlib_id, w.flatTable_id, w.name, w.adhoc_sql, w.desc, w.chart_params, w.publish)
        }.result
      db.run(query)
    }
  }

  def getWidgetById(id: Long): Future[(Long, Long, Long, String, Option[String], String, Option[String], Boolean)] = {
    db.run(modules.widgetQuery.filter(_.id === id).map(w => (w.id, w.widgetlib_id, w.flatTable_id, w.name, w.adhoc_sql, w.desc, w.chart_params, w.publish)).result.head)
  }

  def getFlatTableId(widgetId: Long): Future[(Long, Option[String])] = {
    db.run(modules.widgetQuery.filter(_.id === widgetId).map(w => (w.flatTable_id, w.adhoc_sql)).result.head)
  }

  def update(widgetSeq: Seq[PutWidgetInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(widgetSeq.map(r => {
      modules.widgetQuery.filter(_.id === r.id).map(widget => (widget.flatTable_id, widget.widgetlib_id, widget.name, widget.adhoc_sql, widget.desc, widget.chart_params, widget.publish, widget.update_by, widget.update_time))
        .update(r.flatTable_id, r.widgetlib_id, r.name, Some(r.adhoc_sql), r.desc, Some(r.chart_params), r.publish, session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def getSql(widgetId: Long): Future[Seq[(String, String, String)]] = {
    val query = (modules.widgetQuery.filter(obj => obj.id === widgetId) join modules.viewQuery on (_.flatTable_id === _.id))
      .map {
        case (w, b) => (w.adhoc_sql.getOrElse(""), b.sql_tmpl, b.result_table)
      }.result
    db.run(query)
  }

  def deleteWidget(widgetId: Long): Future[Int] = {
    modules.widgetDal.deleteById(widgetId)
  }

  def deleteRelDW(widgetId: Long): Future[Int] = {
    modules.relDashboardWidgetDal.deleteByFilter(_.widget_id === widgetId)
  }


}