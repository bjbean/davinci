package edp.davinci.rest.dashboard

import edp.davinci.module._
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.CommonUtils
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future


class DashboardService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val relDWDal = modules.relDashboardWidgetDal
  private lazy val relGBDal = modules.relGroupBizlogicDal
  private lazy val wDal = modules.widgetDal
  private lazy val dDal = modules.dashboardDal

  def getInsideInfo(session: SessionClass, dashboardId: Long, bizIdSeq: Seq[Long]): Future[Seq[(Long, Long, Int, Int, Int, Int, Long, Long, Long, String, Option[String], String, String, String, Boolean)]] = {
    val relDWTQ = relDWDal.getTableQuery
    val relGBTQ = relGBDal.getTableQuery
    val widgetTQ = wDal.getTableQuery
    val query = if (session.admin)
      (relDWTQ.filter(obj => obj.dashboard_id === dashboardId && obj.active === true) join widgetTQ.filter(widget => widget.active === true) on (_.widget_id === _.id)).
        map {
          case (r, w) => (r.id, r.dashboard_id, r.position_x, r.position_y, r.length, r.width, w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.olap_sql, w.desc, w.trigger_type, w.trigger_params, w.publish)
        }.result
    else {
      val bizIds = relGBTQ.withFilter(rel => {
        rel.group_id inSet session.groupIdList
        rel.active === true
      }).map(_.bizlogic_id)

      (relDWTQ.filter(obj => obj.dashboard_id === dashboardId && obj.active === true) join
        widgetTQ.filter(obj => obj.bizlogic_id in bizIds).filter(obj => obj.active === true && obj.publish === true) on (_.widget_id === _.id)).
        map {
          case (r, w) => (r.id, r.dashboard_id, r.position_x, r.position_y, r.length, r.width, w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.olap_sql, w.desc, w.trigger_type, w.trigger_params, w.publish)
        }
    }.result
    dDal.getDB.run(query)
  }

  def getBizIds(session: SessionClass): Future[Seq[Long]] = {
    val relGBTQ = relGBDal.getTableQuery
    val query = relGBTQ.withFilter(rel => {
      rel.group_id inSet session.groupIdList
      rel.active === true
    }).map(_.bizlogic_id).result
    dDal.getDB.run(query).mapTo[Seq[Long]]
  }

  def update(session: SessionClass, dashboardSeq: Seq[PutDashboardInfo]): Future[Unit] = {
    val dashboardTQ = dDal.getTableQuery
    val query = DBIO.seq(dashboardSeq.map(r => {
      dashboardTQ.filter(obj => obj.id === r.id && obj.active === true).map(dashboard => (dashboard.name, dashboard.desc, dashboard.publish, dashboard.update_by, dashboard.update_time)).update(r.name, r.desc, r.publish, session.userId, CommonUtils.currentTime)
    }): _*)
    dDal.getDB.run(query)
  }

  def updateRelDashboardWidget(session: SessionClass, relSeq: Seq[PutRelDashboardWidget]): Future[Unit] = {
    val relDWTQ = relDWDal.getTableQuery
    val query = DBIO.seq(relSeq.map(r => {
      relDWTQ.filter(obj => obj.id === r.id && obj.active === true).map(rel => (rel.dashboard_id, rel.widget_id, rel.position_x, rel.position_y, rel.width, rel.length, rel.update_by, rel.update_time))
        .update(r.dashboard_id, r.widget_id, r.position_x, r.position_y, r.width, r.length, session.userId, CommonUtils.currentTime)
    }): _*)
    dDal.getDB.run(query)
  }

  def getAll(session: SessionClass): Future[Seq[PutDashboardInfo]] = {
    val dashboardTQ = dDal.getTableQuery
    val query =
      if (session.admin) dashboardTQ.filter(_.active === true).map(obj => (obj.id, obj.name, obj.desc, obj.publish)).result
      else
        dashboardTQ.filter(obj => obj.active === true && obj.publish === true).result
    dDal.getDB.run(query).mapTo[Seq[PutDashboardInfo]]
  }

}