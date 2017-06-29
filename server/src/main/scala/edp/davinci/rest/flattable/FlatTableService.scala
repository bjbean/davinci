package edp.davinci.rest.flattable

import edp.davinci.util.ResponseUtils
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities._
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class FlatTableService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val fDal = modules.flatTableDal
  private lazy val relGFDal = modules.relGroupFlatTableDal
  private lazy val flatTableTQ = fDal.getTableQuery
  private lazy val relGFTQ = relGFDal.getTableQuery
  private lazy val sourceTQ = modules.sourceDal.getTableQuery
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

  def deleteByFlatId(flatTableSeq: Seq[PutFlatTableInfo]): Future[Unit] = {
    val query = DBIO.seq(flatTableSeq.map(r => {
      relGFTQ.filter(_.flatTable_id === r.id).delete
    }): _*)
    db.run(query)
  }

  def getGroups(flatId: Long): Future[Seq[PutRelGroupFlatTable]] = {
    db.run(relGFTQ.filter(_.flatTable_id === flatId).map(rel => (rel.id, rel.group_id, rel.sql_params)).result).mapTo[Seq[PutRelGroupFlatTable]]
  }


  def getSourceInfo(flatTableId: Long, session: SessionClass): Future[Seq[(String, String, String, String)]] = {
    val rel = if (session.admin) relGFTQ.filter(_.flatTable_id === flatTableId) else relGFTQ.filter(_.flatTable_id === flatTableId).filter(_.group_id inSet session.groupIdList)
    val query = (flatTableTQ.filter(obj => obj.id === flatTableId) join sourceTQ on (_.source_id === _.id) join
      rel on (_._1.id === _.flatTable_id))
      .map {
        case (fs, r) => (fs._1.sql_tmpl, fs._1.result_table, fs._2.connection_url, r.sql_params)
      }.result
    db.run(query)
  }
}
