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
  private lazy val relDWTQ = relDWDal.getTableQuery
  private lazy val relGBTQ = relGBDal.getTableQuery
  private lazy val widgetTQ = wDal.getTableQuery
  private lazy val dashboardTQ = dDal.getTableQuery
  private lazy val db = dDal.getDB

  def getInsideInfo(session: SessionClass, dashboardId: Long): Future[Seq[(Long, Long, Long, Int, Int, Int, Int, String, String)]] = {
    val query = if (session.admin)
      (relDWTQ.filter(obj => obj.dashboard_id === dashboardId) join widgetTQ on (_.widget_id === _.id)).
        map {
          case (r, w) => (r.id, w.id, w.bizlogic_id, r.position_x, r.position_y, r.width, r.length, r.trigger_type, r.trigger_params)
        }.result
    else {
      val bizIds = relGBTQ.filter(_.group_id inSet session.groupIdList).map(_.bizlogic_id)
      (relDWTQ.filter(obj => obj.dashboard_id === dashboardId) join
        widgetTQ.filter(_.publish).filter(_.bizlogic_id in bizIds) on (_.widget_id === _.id))
        .map {
          case (rDW, w) => println("jhhh"); (rDW.id, w.id, w.bizlogic_id, rDW.position_x, rDW.position_y, rDW.width, rDW.length, rDW.trigger_type, rDW.trigger_params)
        }.result
    }
    db.run(query)
  }

  def getDashBoard(dashboardId: Long): Future[Seq[(Long, String, Option[String], String, Boolean)]] = {
    val query = dashboardTQ.filter(_.id === dashboardId).map(d => (d.id, d.name, d.pic, d.desc, d.publish)).result
    db.run(query)
  }

  def update(session: SessionClass, dashboardSeq: Seq[PutDashboardInfo]): Future[Unit] = {
    val query = DBIO.seq(dashboardSeq.map(r => {
      dashboardTQ.filter(obj => obj.id === r.id).map(dashboard => (dashboard.name, dashboard.desc, dashboard.publish, dashboard.active, dashboard.update_by, dashboard.update_time)).update(r.name, r.desc, r.publish, r.active.getOrElse(true), session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def updateRelDashboardWidget(session: SessionClass, relSeq: Seq[PutRelDashboardWidget]): Future[Unit] = {
    val query = DBIO.seq(relSeq.map(r => {
      relDWTQ.filter(obj => obj.id === r.id).map(rel => (rel.dashboard_id, rel.widget_id, rel.position_x, rel.position_y, rel.width, rel.length, rel.trigger_type, rel.trigger_params, rel.update_by, rel.update_time))
        .update(r.dashboard_id, r.widget_id, r.position_x, r.position_y, r.width, r.length, r.trigger_type, r.trigger_params, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def getAll(session: SessionClass, active: Boolean): Future[Seq[(Long, String, Option[String], String, Boolean, Boolean)]] = {
    val query =
      if (session.admin) {
        if (active)
          dashboardTQ.filter(_.active).map(obj => (obj.id, obj.name, obj.pic, obj.desc, obj.publish, obj.active)).result
        else
          dashboardTQ.map(obj => (obj.id, obj.name, obj.pic, obj.desc, obj.publish, obj.active)).result
      }
      else {
        if (active)
          dashboardTQ.filter(obj => obj.publish && obj.active).map(obj => (obj.id, obj.name, obj.pic, obj.desc, obj.publish, obj.active)).result
        else
          dashboardTQ.filter(obj => obj.publish ).map(obj => (obj.id, obj.name, obj.pic, obj.desc, obj.publish, obj.active)).result
      }
    db.run(query)
  }


  def deleteRelDWById(session: SessionClass, relId: Long): Future[Int] = {
    db.run(relDWTQ.filter(_.id === relId).delete)
  }
}
