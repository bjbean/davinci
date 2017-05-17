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
  private lazy val rGBDal = modules.relGroupBizlogicDal
  private lazy val widgetTQ = wDal.getTableQuery
  private lazy val rGBTQ = rGBDal.getTableQuery
  private lazy val db = wDal.getDB

  def getAll(session: SessionClass, active: Boolean): Future[Seq[(Long, Long, Long, String, Option[String], String, Option[String], Boolean, Boolean)]] = {
    val tmpQuery = if (active) widgetTQ.filter(_.active === true) else widgetTQ
    if (session.admin)
      db.run(tmpQuery.map(r => (r.id, r.widgetlib_id, r.bizlogic_id, r.name, r.olap_sql, r.desc, r.chart_params, r.publish, r.active)).result)
    else {
      val query = (tmpQuery.filter(obj => obj.publish === true)
        join rGBTQ.filter(r => r.group_id inSet session.groupIdList)
        on (_.bizlogic_id === _.bizlogic_id))
        .map {
          case (w, _) => (w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.olap_sql, w.desc, w.chart_params, w.publish, w.active)
        }.result
      db.run(query)
    }
  }

  def update(widgetSeq: Seq[PutWidgetInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(widgetSeq.map(r => {
      widgetTQ.filter(_.id === r.id).map(widget => (widget.bizlogic_id, widget.widgetlib_id, widget.name, widget.olap_sql, widget.desc, widget.chart_params, widget.publish,widget.active, widget.update_by, widget.update_time))
        .update(r.bizlogic_id, r.widgetlib_id, r.name, Some(r.olap_sql), r.desc, Some(r.chart_params), r.publish,r.active.getOrElse(true), session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def getSql(widgetId: Long): Future[Seq[(String, String, String)]] = {
    val bizlogicTQ = bDal.getTableQuery
    val query = (widgetTQ.filter(obj => obj.id === widgetId) join bizlogicTQ on (_.bizlogic_id === _.id))
      .map {
        case (w, b) => (w.olap_sql.getOrElse(""), b.sql_tmpl, b.result_table)
      }.result
    db.run(query)
  }


}
