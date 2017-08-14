package edp.davinci.rest.view

import edp.davinci.util.ResponseUtils
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities._
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class ViewService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val fDal = modules.flatTableDal
  private lazy val relGFDal = modules.relGroupFlatTableDal
  private lazy val flatTableTQ = fDal.getTableQuery
  private lazy val relGFTQ = relGFDal.getTableQuery
  private lazy val sourceTQ = modules.sourceDal.getTableQuery
  private lazy val widgetTQ = modules.widgetDal.getTableQuery
  private lazy val db = fDal.getDB

  def getAllFlatTbls(active: Boolean): Future[Seq[(Long, Long, String, String, String, Option[String], String, String, String, Boolean)]] = {
    if (active)
      db.run(flatTableTQ.filter(_.active).map(r => (r.id, r.source_id, r.name, r.sql_tmpl, r.result_table, r.desc, r.trigger_type, r.frequency, r.`catch`, r.active)).result)
    else
      db.run(flatTableTQ.map(r => (r.id, r.source_id, r.name, r.sql_tmpl, r.result_table, r.desc, r.trigger_type, r.frequency, r.`catch`, r.active)).result)
  }

  def updateFlatTbl(flatTableSeq: Seq[PutFlatTableInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(flatTableSeq.map(r => {
      flatTableTQ.filter(obj => obj.id === r.id).map(flatTable => (flatTable.name, flatTable.source_id, flatTable.sql_tmpl, flatTable.desc, flatTable.trigger_type, flatTable.frequency, flatTable.`catch`, flatTable.active, flatTable.update_by, flatTable.update_time))
        .update(r.name, r.source_id, r.sql_tmpl, Some(r.desc), r.trigger_type, r.frequency, r.`catch`, r.active.getOrElse(true), session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def deleteFromView(idSeq: Seq[Long]): Future[Unit] = {
    val query = DBIO.seq(idSeq.map(r => {
      flatTableTQ.filter(_.id === r).delete
    }): _*)
    db.run(query)
  }

  def deleteFromRel(idSeq: Seq[Long]): Future[Unit] = {
    val query = DBIO.seq(idSeq.map(r => {
      relGFTQ.filter(_.flatTable_id === r).delete
    }): _*)
    db.run(query)
  }

  def getGroups(flatId: Long): Future[Seq[PutRelGroupFlatTable]] = {
    db.run(relGFTQ.filter(_.flatTable_id === flatId).map(rel => (rel.id, rel.group_id, rel.sql_params)).result).mapTo[Seq[PutRelGroupFlatTable]]
  }

  def updateWidget(flatTableId: Long): Future[Int] = {
    db.run(widgetTQ.filter(_.flatTable_id === flatTableId).map(_.flatTable_id).update(0))
  }

  def getSourceInfo(flatTableId: Long, session: SessionClass): Future[Seq[(String, String, String, String)]] = {
    val rel = if (session.admin) relGFTQ.filter(_.flatTable_id === flatTableId) else relGFTQ.filter(_.flatTable_id === flatTableId).filter(_.group_id inSet session.groupIdList)
    val query = (rel join flatTableTQ.filter(obj => obj.id === flatTableId) on (_.flatTable_id === _.id) join
      sourceTQ on (_._2.source_id === _.id))
      .map {
        case (rf, s) => (rf._2.sql_tmpl, rf._2.result_table, s.connection_url, rf._1.sql_params)
      }.result
    db.run(query)
  }
}
