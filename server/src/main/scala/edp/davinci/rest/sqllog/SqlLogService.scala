package edp.davinci.rest.sqllog

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities.SqlLog
import edp.davinci.rest.{ResponseSeqJson, SessionClass}
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait SqlLogRepository extends ConfigurationModuleImpl with PersistenceModuleImpl {
  def getAll(session: SessionClass): Future[Seq[SqlLog]] = {
    db.run(sqlLogQuery.filter(_.user_id === session.userId).result).mapTo[Seq[SqlLog]]
  }

  def update(sqlLogSeq: Seq[SqlLog], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(sqlLogSeq.map(r => {
      sqlLogQuery.filter(_.id === r.id).map(log => (log.id, log.user_id, log.user_email, log.sql, log.start_time, log.end_time, log.success, log.error))
        .update(r.id, r.user_id, r.user_email, r.sql, r.start_time, r.end_time, r.success, r.error)
    }): _*)
    db.run(query)
  }
}

trait SqlLogService extends SqlLogRepository with Directives {
  def getAllLogsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(getAll(session)) {
        case Success(logSeq) =>
          if (logSeq.nonEmpty) complete(OK, ResponseSeqJson[SqlLog](getHeader(200, session), logSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def putSqlLogComplete(session: SessionClass, sqlLogSeq: Seq[SqlLog]): Route = {
    if (session.admin) {
      val future = update(sqlLogSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }
}
