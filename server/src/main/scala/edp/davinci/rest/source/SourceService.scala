package edp.davinci.rest.source

import edp.davinci.module._
import edp.davinci.persistence.entities.PutSourceInfo
import edp.davinci.rest.SessionClass
import edp.davinci.util.CommonUtils
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future

class SourceService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val sDal = modules.sourceDal

  def getAll: Future[Seq[PutSourceInfo]] = {
    val sourceTQ = sDal.getTableQuery
    sDal.getDB.run(sourceTQ.filter(_.active === true).map(r => (r.id, r.name, r.connection_url, r.desc, r.`type`, r.config)).result).mapTo[Seq[PutSourceInfo]]
  }

  def update(sourceSeq: Seq[PutSourceInfo], session: SessionClass): Future[Unit] = {
    val sourceTQ = sDal.getTableQuery
    val query = DBIO.seq(sourceSeq.map(r => {
      sourceTQ.filter(_.id === r.id).map(source => (source.id, source.name, source.connection_url, source.desc, source.`type`, source.config, source.update_by, source.update_time)).update(r.id, r.name, r.connection_url, r.desc, r.`type`, r.config, session.userId, CommonUtils.currentTime)
    }): _*)
    sDal.getDB.run(query)
  }
}