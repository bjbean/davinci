package edp.davinci.rest.source

import edp.davinci.util.ResponseUtils
import edp.davinci.module._
import edp.davinci.persistence.entities.PutSourceInfo
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class SourceService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val sourceTQ = modules.sourceDal.getTableQuery
  private lazy val flatTableTQ = modules.flatTableDal.getTableQuery
  private lazy val db =  modules.sourceDal.getDB

  def getAll(active: Boolean): Future[Seq[(Long, String, String, String, String, String, Boolean)]] = {

    val tmpQuery = if (active) sourceTQ.filter(_.active) else sourceTQ
   db.run(tmpQuery.map(r => (r.id, r.name, r.connection_url, r.desc, r.`type`, r.config, r.active)).result)
  }

  def update(sourceSeq: Seq[PutSourceInfo], session: SessionClass): Future[Unit] = {

    val query = DBIO.seq(sourceSeq.map(r => {
      sourceTQ.filter(_.id === r.id).map(source => (source.id, source.name, source.connection_url, source.desc, source.`type`, source.config, source.update_by, source.update_time)).update(r.id, r.name, r.connection_url, r.desc, r.`type`, r.config, session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def deleteSource(sourceId:Long): Future[Int] ={
    db.run(sourceTQ.filter(_.id === sourceId).delete)
  }

  def updateFlatTable(sourceId:Long): Future[Int] ={
    db.run(flatTableTQ.filter(_.source_id=== sourceId).map(_.source_id).update(0))
  }
}
