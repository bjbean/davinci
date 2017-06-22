package edp.davinci.rest.widget

import edp.davinci.common.ResponseUtils
import edp.davinci.module._
import edp.davinci.persistence.entities.PutWidgetInfo
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class WidgetService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val wDal = modules.widgetDal
  private lazy val fDal = modules.flatTableDal
  private lazy val rGFDal = modules.relGroupFlatTableDal
  private lazy val widgetTQ = wDal.getTableQuery
  private lazy val rGFTQ = rGFDal.getTableQuery
  private lazy val db = wDal.getDB

  def getAll(session: SessionClass, active: Boolean): Future[Seq[(Long, Long, Long, String, Option[String], String, Option[String], Boolean, Boolean)]] = {
    val tmpQuery = if (active) widgetTQ.filter(_.active === true) else widgetTQ
    if (session.admin)
      db.run(tmpQuery.map(r => (r.id, r.widgetlib_id, r.flatTable_id, r.name, r.olap_sql, r.desc, r.chart_params, r.publish, r.active)).result)
    else {
      val query = (tmpQuery.filter(_.publish)
        join rGFTQ.filter(r => r.group_id inSet session.groupIdList)
        on (_.flatTable_id === _.flatTable_id))
        .map {
          case (w, _) => (w.id, w.widgetlib_id, w.flatTable_id, w.name, w.olap_sql, w.desc, w.chart_params, w.publish, w.active)
        }.result
      db.run(query)
    }
  }

  def update(widgetSeq: Seq[PutWidgetInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(widgetSeq.map(r => {
      widgetTQ.filter(_.id === r.id).map(widget => (widget.flatTable_id, widget.widgetlib_id, widget.name, widget.olap_sql, widget.desc, widget.chart_params, widget.publish,widget.active, widget.update_by, widget.update_time))
        .update(r.flatTable_id, r.widgetlib_id, r.name, Some(r.olap_sql), r.desc, Some(r.chart_params), r.publish,r.active.getOrElse(true), session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def getSql(widgetId: Long): Future[Seq[(String, String, String)]] = {
    val flatTableTQ = fDal.getTableQuery
    val query = (widgetTQ.filter(obj => obj.id === widgetId) join flatTableTQ on (_.flatTable_id === _.id))
      .map {
        case (w, b) => (w.olap_sql.getOrElse(""), b.sql_tmpl, b.result_table)
      }.result
    db.run(query)
  }


}
