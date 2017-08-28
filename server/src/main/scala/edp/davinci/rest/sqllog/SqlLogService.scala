package edp.davinci.rest.sqllog

import edp.davinci.module.DbModule._
import edp.davinci.ModuleInstance
import edp.davinci.persistence.entities.SqlLog
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future

object SqlLogService extends SqlLogService

trait SqlLogService {
  private lazy val modules = ModuleInstance.getModule

  def getAll(session: SessionClass): Future[Seq[SqlLog]] = {
    db.run(modules.sqlLogQuery.filter(_.user_id === session.userId).result).mapTo[Seq[SqlLog]]
  }

  def update(sqlLogSeq: Seq[SqlLog], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(sqlLogSeq.map(r => {
      modules.sqlLogQuery.filter(_.id === r.id).map(log => (log.id, log.user_id, log.user_email, log.sql, log.start_time, log.end_time, log.success, log.error))
        .update(r.id, r.user_id, r.user_email, r.sql, r.start_time, r.end_time, r.success, r.error)
    }): _*)
    db.run(query)
  }
}