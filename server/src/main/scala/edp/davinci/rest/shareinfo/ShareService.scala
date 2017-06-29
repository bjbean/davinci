package edp.davinci.rest.shareinfo

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
  private lazy val db = shareDal.getDB

  def getWidgetById(id: Long): Future[(Long, Long, Long, String, Option[String], String, Option[String], Boolean, Boolean)] = {
    db.run(widgetTQ.filter(_.id === id).map(w => (w.id, w.widgetlib_id, w.flatTable_id, w.name, w.adhoc_sql, w.desc, w.chart_params, w.publish, w.active)).result.head)
  }

  def getUserGroup(userId:Long): Future[Seq[Long]] ={
    db.run(relGUTQ.filter(_.user_id === userId).map(_.group_id).result)
  }

  def getSourceInfo(flatTableId: Long, groupIds:Seq[Long]): Future[Seq[(String, String, String, String)]] = {
    val query = (flatTableTQ.filter(obj => obj.id === flatTableId) join sourceTQ on (_.source_id === _.id) join
      relGFTQ.filter(_.flatTable_id === flatTableId).filter(_.group_id inSet groupIds) on (_._1.id === _.flatTable_id))
      .map {
        case (fs, r) => (fs._1.sql_tmpl, fs._1.result_table, fs._2.connection_url, r.sql_params)
      }.result
    db.run(query)
  }

  def getShareInfo(identifier: String): Future[Seq[(String, String, String)]] = {
    val query = shareTQ.filter(_.identifier === identifier).map(s => (s.connection_url, s.widget, s.widget_sql)).result
    db.run(query)
  }

}
