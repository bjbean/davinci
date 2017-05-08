package edp.davinci.rest.widget

import edp.davinci.module._
import edp.davinci.persistence.entities.PutWidgetInfo
import edp.davinci.rest.SessionClass
import edp.davinci.util.CommonUtils
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future

class WidgetService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val wDal = modules.widgetDal
  private lazy val bDal = modules.bizlogicDal

  def getAll(session: SessionClass): Future[Seq[(Long, Long, Long, String, Option[String], String, String, String, Option[String], Boolean)]] = {
    val widgetTQ = wDal.getTableQuery
    if (session.admin)
      wDal.getDB.run(widgetTQ.filter(_.active === true)
        .map(r => (r.id, r.widgetlib_id, r.bizlogic_id, r.name, r.olap_sql, r.desc, r.trigger_type, r.trigger_params, r.chart_params, r.publish)).result)
    else
      wDal.getDB.run(widgetTQ.filter(obj => obj.active === true && obj.publish === true)
        .map(r => (r.id, r.widgetlib_id, r.bizlogic_id, r.name, r.olap_sql, r.desc, r.trigger_type, r.trigger_params, r.chart_params, r.publish)).result)
  }

  def update(widgetSeq: Seq[PutWidgetInfo], session: SessionClass): Future[Unit] = {
    val widgetTQ = wDal.getTableQuery
    val query = DBIO.seq(widgetSeq.map(r => {
      widgetTQ.filter(_.id === r.id).map(widget => (widget.bizlogic_id, widget.widgetlib_id, widget.name, widget.olap_sql, widget.desc, widget.trigger_type, widget.trigger_params, widget.chart_params, widget.publish, widget.update_by, widget.update_time))
        .update(r.bizlogic_id, r.widgetlib_id, r.name, Some(r.olap_sql), r.desc, r.trigger_type, r.trigger_params, Some(r.chart_params), r.publish, session.userId, CommonUtils.currentTime)
    }): _*)
    wDal.getDB.run(query)
  }

  def getSql(widgetId: Long): Future[Seq[(String, String, String)]] = {
    val widgetTQ = wDal.getTableQuery
    val bizlogicTQ = bDal.getTableQuery
    val query = (widgetTQ.filter(obj => obj.id === widgetId && obj.active === true) join bizlogicTQ.filter(_.active === true) on (_.bizlogic_id === _.id))
      .map {
        case (w, b) => (w.olap_sql.getOrElse(""), b.sql_tmpl, b.result_table)
      }.result
    wDal.getDB.run(query)
  }


}
