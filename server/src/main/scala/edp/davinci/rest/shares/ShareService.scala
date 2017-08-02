package edp.davinci.rest.shares

import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.PutWidgetInfo
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class ShareService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val wDal = modules.widgetDal
  private lazy val widgetTQ = wDal.getTableQuery
  private lazy val shareDal = modules.shareInfoDal
  private lazy val shareTQ = shareDal.getTableQuery
  private lazy val flatTableTQ = modules.flatTableDal.getTableQuery
  private lazy val relGFTQ = modules.relGroupFlatTableDal.getTableQuery
  private lazy val sourceTQ = modules.sourceDal.getTableQuery
  private lazy val relGUTQ = modules.relUserGroupDal.getTableQuery
  private lazy val userTQ = modules.userDal.getTableQuery
  private lazy val relDWTQ = modules.relDashboardWidgetDal.getTableQuery
  private lazy val dashboardTQ=modules.dashboardDal.getTableQuery
  private lazy val db = shareDal.getDB

  def getWidgetById(id: Long): Future[(Long, Long, Long, String, Option[String], String, Option[String], Boolean, Boolean)] = {
    db.run(widgetTQ.filter(_.id === id).map(w => (w.id, w.widgetlib_id, w.flatTable_id, w.name, w.adhoc_sql, w.desc, w.chart_params, w.publish, w.active)).result.head)
  }

  def getUserGroup(userId: Long): Future[Seq[Long]] = {
    db.run(relGUTQ.filter(_.user_id === userId).map(_.group_id).result)
  }

  def getUserInfo(userId: Long): Future[Boolean] = {
    db.run(userTQ.filter(_.id === userId).map(_.admin).result.head)
  }

  def getSourceInfo(flatTableId: Long, groupIds: Seq[Long], admin: Boolean): Future[Seq[(String, String, String, String)]] = {
    val rel = if (admin) relGFTQ.filter(_.flatTable_id === flatTableId) else relGFTQ.filter(_.flatTable_id === flatTableId).filter(_.group_id inSet groupIds)
    val query = (rel join flatTableTQ.filter(obj => obj.id === flatTableId) on (_.flatTable_id === _.id) join
      sourceTQ on (_._2.source_id === _.id))
      .map {
        case (rf, s) => (rf._2.sql_tmpl, rf._2.result_table, s.connection_url, rf._1.sql_params)
      }.result
    db.run(query)
  }

  def getShareInfo(identifier: String): Future[Seq[(String, String, String)]] = {
    val query = shareTQ.filter(_.identifier === identifier).map(s => (s.connection_url, s.widget, s.widget_sql)).result
    db.run(query)
  }
  def getDashBoard(dashboardId: Long): Future[(Long, String, Option[String], String, Boolean)] = {
    val query = dashboardTQ.filter(_.id === dashboardId).map(d => (d.id, d.name, d.pic, d.desc, d.publish)).result.head
    db.run(query)
  }

  def getShareDashboard(dashboardId:Long,groupIds: Seq[Long], admin: Boolean): Future[Seq[(Long, Long, Long, Int, Int, Int, Int, String, String)]] ={
    val query = if (admin)
      (relDWTQ.filter(obj => obj.dashboard_id === dashboardId) join widgetTQ on (_.widget_id === _.id)).
        map {
          case (r, w) => (r.id, w.id, w.flatTable_id, r.position_x, r.position_y, r.width, r.length, r.trigger_type, r.trigger_params)
        }.result
    else {
      val flatIds = relGFTQ.filter(_.group_id inSet groupIds).map(_.flatTable_id)
      (relDWTQ.filter(obj => obj.dashboard_id === dashboardId) join
        widgetTQ.filter(_.publish).filter(_.flatTable_id in flatIds) on (_.widget_id === _.id))
        .map {
          case (rDW, w) => (rDW.id, w.id, w.flatTable_id, rDW.position_x, rDW.position_y, rDW.width, rDW.length, rDW.trigger_type, rDW.trigger_params)
        }.result
    }
    db.run(query)
  }

}
