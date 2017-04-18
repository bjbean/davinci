package edp.davinci.rest.bizlogic

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities._
import edp.davinci.rest.{ResponseSeqJson, SessionClass}
import edp.davinci.util.CommonUtils
import edp.davinci.util.CommonUtils.getHeader
import slick.jdbc.MySQLProfile.api._
import edp.davinci.util.JsonProtocol._
import scala.concurrent.Future
import scala.util.{Failure, Success}


trait BizlogicRepository extends ConfigurationModuleImpl with PersistenceModuleImpl with Directives {
  def getAllBiz: Future[Seq[QueryBizlogic]] = {
    db.run(bizlogicQuery.filter(_.active === true).map(r => (r.id, r.source_id, r.name, r.sql_tmpl, r.result_table, r.desc)).result).mapTo[Seq[QueryBizlogic]]
  }

  def updateBiz(bizlogicSeq: Seq[PutBizlogicInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(bizlogicSeq.map(r => {
      bizlogicQuery.filter(obj => obj.id === r.id && obj.active === true).map(bizlogic => (bizlogic.name, bizlogic.source_id, bizlogic.sql_tmpl, bizlogic.result_table, bizlogic.desc, bizlogic.update_by, bizlogic.update_time))
        .update(r.name, r.source_id, r.sql_tmpl, r.result_table, r.desc, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def deleteByBizId(bizlogicSeq: Seq[PutBizlogicInfo]): Future[Unit] = {
    val query = DBIO.seq(bizlogicSeq.map(r => {
      relGroupBizlogicQuery.filter(_.bizlogic_id === r.id).map(_.active).update(false)
    }): _*)
    db.run(query)
  }

  def getGroups(bizId: Long): Future[Seq[PutRelGroupBizlogic]] = {
    db.run(relGroupBizlogicQuery.filter(_.bizlogic_id === bizId).map(rel => (rel.id, rel.group_id, rel.sql_params)).result).mapTo[Seq[PutRelGroupBizlogic]]
  }
}


trait BizlogicService extends BizlogicRepository with Directives {
  def getAllBizlogicsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(getAllBiz) {
        case Success(bizlogicSeq) =>
          if (bizlogicSeq.nonEmpty) complete(OK, ResponseSeqJson[QueryBizlogic](getHeader(200, session), bizlogicSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def getGroupsByBizId(session: SessionClass, bizId: Long): Route = {
    val future = getGroups(bizId)
    onComplete(future) {
      case Success(relSeq) =>
        if (relSeq.nonEmpty) complete(OK, ResponseSeqJson[PutRelGroupBizlogic](getHeader(200, session), relSeq))
        else complete(NotFound, getHeader(404, session))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  def putBizlogicComplete(session: SessionClass, bizlogicSeq: Seq[PutBizlogicInfo]): Route = {
    val future = updateBiz(bizlogicSeq, session)
    onComplete(future) {
      case Success(_) =>
        onComplete(deleteByBizId(bizlogicSeq)) {
          case Success(_) =>
            val relSeq = for {rel <- bizlogicSeq.head.relBG
            } yield RelGroupBizlogic(0, rel.group_id, bizlogicSeq.head.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
            onComplete(relGroupBizlogicDal.insert(relSeq)) {
              case Success(_) => complete(OK, getHeader(200, session))
              case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
            }
          case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
        }
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  def postBizlogic(session: SessionClass, bizlogicSeq: Seq[PostBizlogicInfo]): Route = {
    if (session.admin) {
      val bizEntitySeq = bizlogicSeq.map(biz => Bizlogic(0, biz.source_id, biz.name, biz.sql_tmpl, biz.result_table, biz.desc, active = true, null, session.userId, null, session.userId))
      onComplete(bizlogicDal.insert(bizEntitySeq)) {
        case Success(bizSeq) =>
          val queryBiz = bizSeq.map(biz => QueryBizlogic(biz.id, biz.source_id, biz.name, biz.sql_tmpl, biz.result_table, biz.desc))
          val relSeq = for {biz <- bizSeq
                            rel <- bizlogicSeq.head.relBG
          } yield RelGroupBizlogic(0, rel.group_id, biz.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
          onComplete(relGroupBizlogicDal.insert(relSeq)) {
            case Success(_) => complete(OK, ResponseSeqJson[QueryBizlogic](getHeader(200, session), queryBiz))
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }
}