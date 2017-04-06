package edp.davinci.rest.dashboard

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.CommonUtils
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


trait DashboardRepository extends ConfigurationModuleImpl with PersistenceModuleImpl {
  def getDashboardInfo(session: SessionClass, dashboardId: Long, bizIdSeq: Seq[Long]): Future[Seq[(Long, Long, Int, Int, Int, Int, Long, Long, Long, String, String, String, String, Boolean)]] = {
    val query = if (session.admin)
      (relDashboardWidgetQuery.filter(obj => obj.dashboard_id === dashboardId && obj.active === true) join widgetQuery.filter(widget => widget.active === true) on (_.widget_id === _.id)).
        map {
          case (r, w) => (r.id, r.dashboard_id, r.position_x, r.position_y, r.length, r.width, w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.desc, w.trigger_type, w.trigger_params, w.publish)
        }.result
    else {
      val bizIds = relGroupBizlogicQuery.withFilter(rel => {
        rel.group_id inSet session.groupIdList
        rel.active === true
      }).map(_.bizlogic_id)

      (relDashboardWidgetQuery.filter(obj => obj.dashboard_id === dashboardId && obj.active === true) join
        widgetQuery.filter(obj => obj.bizlogic_id in bizIds).filter(obj => obj.active === true && obj.publish === true) on (_.widget_id === _.id)).
        map {
          case (r, w) => (r.id, r.dashboard_id, r.position_x, r.position_y, r.length, r.width, w.id, w.widgetlib_id, w.bizlogic_id, w.name, w.desc, w.trigger_type, w.trigger_params, w.publish)
        }
    }.result
    db.run(query)
  }

  def getBizIds(session: SessionClass): Future[Seq[Long]] = {
    val query = relGroupBizlogicQuery.withFilter(rel => {
      rel.group_id inSet session.groupIdList
      rel.active === true
    }).map(_.bizlogic_id).result
    db.run(query).mapTo[Seq[Long]]
  }

  def updateDashboard(session: SessionClass, dashboardSeq: Seq[PutDashboardInfo]): Future[Unit] = {
    val query = DBIO.seq(dashboardSeq.map(r => {
      dashboardQuery.filter(obj => obj.id === r.id && obj.active === true).map(dashboard => (dashboard.name, dashboard.desc, dashboard.publish, dashboard.update_by, dashboard.update_time)).update(r.name, r.desc, r.publish, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def updateRelDashboardWidget(session: SessionClass, relSeq: Seq[PutRelDashboardWidget]): Future[Unit] = {
    val query = DBIO.seq(relSeq.map(r => {
      relDashboardWidgetQuery.filter(obj => obj.id === r.id && obj.active === true).map(rel => (rel.dashboard_id, rel.widget_id, rel.position_x, rel.position_y, rel.width, rel.length, rel.update_by, rel.update_time))
        .update(r.dashboard_id, r.widget_id, r.position_x, r.position_y, r.width, r.length, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def getDashBoard(session: SessionClass, dashboardId: Long): Future[Option[Dashboard]] = {
    val future: Future[Option[Dashboard]] =
      if (session.admin) dashboardDal.findById(dashboardId)
      else
        db.run(dashboardQuery.filter(obj => obj.id === dashboardId && obj.active === true && obj.publish === true).result.headOption).mapTo[Option[Dashboard]]
    future
  }
}

trait DashboardService extends Directives with DashboardRepository {

  def getDashboardById(dashboardId: Long, session: SessionClass): Route = {
    onComplete(getBizIds(session)) {
      case Success(bizIds) =>
        onComplete(getDashboardInfo(session, dashboardId, bizIds)) {
          case Success(dashboardInfoSeq) =>
            val infoSeq = dashboardInfoSeq.map(r => {
              val widgetInfo = (r._7, r._8, r._9, r._10, r._11, r._12, r._13, r._14).mapTo[PutWidgetInfo]
              (r._1, r._2, r._3, r._4, r._5, r._6, widgetInfo).asInstanceOf[DashboardInfo]
            })
            complete(OK, ResponseSeqJson[DashboardInfo](getHeader(200, session), infoSeq))
          case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
        }
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }


  def putDashboardComplete(session: SessionClass, dashboardSeq: Seq[PutDashboardInfo]): Route = {
    if (session.admin) {
      onComplete(updateDashboard(session, dashboardSeq)) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }
    else complete(Forbidden, getHeader(403, session))
  }

  def putWidgetInDashboard(session: SessionClass, relSeq: Seq[PutRelDashboardWidget]): Route = {
    if (session.admin) {
      onComplete(updateRelDashboardWidget(session, relSeq)) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }
    else complete(Forbidden, getHeader(403, session))
  }

  def postDashBoard(session: SessionClass, postDashboardSeq: Seq[PostDashboardInfo]): Route = {
    if (session.admin) {
      val dashboardSeq = postDashboardSeq.map(post => Dashboard(0, post.name, post.desc, post.publish, active = true, null, session.userId, null, session.userId))
      onComplete(dashboardDal.insert(dashboardSeq)) {
        case Success(dashWithIdSeq) =>
          val responseDashSeq = dashWithIdSeq.map(dashboard => PutDashboardInfo(dashboard.id, dashboard.name, dashboard.desc, dashboard.publish))
          complete(OK, ResponseSeqJson[PutDashboardInfo](getHeader(200, session), responseDashSeq))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }else complete(Forbidden, getHeader(403, session))
  }


  def postWidget2Dashboard(session: SessionClass, postRelDWSeq: Seq[PostRelDashboardWidget]): Route ={
    if (session.admin) {
      val relDWSeq = postRelDWSeq.map(post => RelDashboardWidget(0,post.dashboard_id,post.widget_id,post.position_x,post.position_y,post.length,post.width,active = true, null, session.userId, null, session.userId))
      onComplete(relDashboardWidgetDal.insert(relDWSeq)) {
        case Success(relDWWithIdSeq) =>
          val responseRelDWSeq = relDWWithIdSeq.map(rel => PutRelDashboardWidget(rel.id,rel.dashboard_id,rel.widget_id,rel.position_x,rel.position_y,rel.length,rel.width))
          complete(OK, ResponseSeqJson[PutRelDashboardWidget](getHeader(200, session), responseRelDWSeq))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }else complete(Forbidden, getHeader(403, session))
  }

}
