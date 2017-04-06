package edp.davinci.rest.source

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities.{PostSourceInfo, PutSourceInfo, Source}
import edp.davinci.rest.{ResponseSeqJson, SessionClass}
import edp.davinci.util.CommonUtils
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait SourceRepository extends ConfigurationModuleImpl with PersistenceModuleImpl {
  def getAll: Future[Seq[PutSourceInfo]] = {
    db.run(sourceQuery.filter(_.active === true).map(r => (r.id, r.group_id, r.name, r.desc, r.`type`, r.config)).result).mapTo[Seq[PutSourceInfo]]
  }

  def update(sourceSeq: Seq[PutSourceInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(sourceSeq.map(r => {
      sourceQuery.filter(_.id === r.id).map(source => (source.id, source.name, source.desc, source.config, source.`type`, source.config, source.update_by, source.update_time)).update(r.id, r.name, r.desc, r.config, r.`type`, r.config, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }
}


trait SourceService extends SourceRepository with Directives {
  def getAllSourcesComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(getAll) {
        case Success(sourceSeq) =>
          if (sourceSeq.nonEmpty) complete(OK, ResponseSeqJson[PutSourceInfo](getHeader(200, session), sourceSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def putSourceComplete(session: SessionClass, sourceSeq: Seq[PutSourceInfo]): Route = {
    if (session.admin) {
      val future = update(sourceSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def postSource(session: SessionClass, postSourceSeq: Seq[PostSourceInfo]): Route ={
    if (session.admin) {
      val sourceSeq = postSourceSeq.map(post => Source(0,post.group_id,post.name,post.connection_url,post.desc,post.`type`,post.config,active = true, null, session.userId, null, session.userId))
      onComplete(sourceDal.insert(sourceSeq)) {
        case Success(sourceWithIdSeq) =>
          val responseSourceSeq = sourceWithIdSeq.map(source => PutSourceInfo(source.id,source.group_id,source.name,source.connection_url,source.desc,source.`type`,source.config))
          complete(OK, ResponseSeqJson[PutSourceInfo](getHeader(200, session), responseSourceSeq))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }else complete(Forbidden, getHeader(403, session))
  }
}
