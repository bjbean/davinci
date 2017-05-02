package edp.davinci.rest.sqllog

import edp.davinci.module._
import edp.davinci.persistence.entities.SqlLog
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future

class SqlLogService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl){
  private lazy val slogDal = modules.sqlLogDal

  def getAll(session: SessionClass): Future[Seq[SqlLog]] = {
    val sqlLogTQ = slogDal.getTableQuery
    slogDal.getDB.run(sqlLogTQ.filter(_.user_id === session.userId).result).mapTo[Seq[SqlLog]]
  }

  def update(sqlLogSeq: Seq[SqlLog], session: SessionClass): Future[Unit] = {
    val sqlLogTQ =slogDal.getTableQuery
    val query = DBIO.seq(sqlLogSeq.map(r => {
      sqlLogTQ.filter(_.id === r.id).map(log => (log.id, log.user_id, log.user_email, log.sql, log.start_time, log.end_time, log.success, log.error))
        .update(r.id, r.user_id, r.user_email, r.sql, r.start_time, r.end_time, r.success, r.error)
    }): _*)
    slogDal.getDB.run(query)
  }
}
