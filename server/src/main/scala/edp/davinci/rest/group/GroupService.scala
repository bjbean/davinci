package edp.davinci.rest.group

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities.{PostGroupInfo, PutGroupInfo, UserGroup}
import edp.davinci.rest.{ResponseSeqJson, SessionClass}
import edp.davinci.util.CommonUtils
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.Group


trait GroupRepository extends ConfigurationModuleImpl with PersistenceModuleImpl {
  def getAll: Future[Seq[PutGroupInfo]] = {
    db.run(groupQuery.filter(_.active === true).map(r => (r.id, r.name,r.desc)).result).mapTo[Seq[PutGroupInfo]]
  }

  def update(groupSeq: Seq[PutGroupInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(groupSeq.map(r => {
      groupQuery.filter(_.id === r.id).map(group => (group.name,group.desc,group.update_by,group.update_time)).update(r.name, r.desc,session.userId,CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }
}


trait GroupService extends Directives with GroupRepository{
  def getAllGroupsComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(getAll) {
        case Success(groupSeq) =>
          if (groupSeq.nonEmpty) complete(OK, ResponseSeqJson[PutGroupInfo](getHeader(200, session), groupSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def putUserComplete(session: SessionClass, groupSeq: Seq[PutGroupInfo]): Route = {
    if (session.admin) {
      val future = update(groupSeq,session)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def postGroup(session: SessionClass, postGroupSeq: Seq[PostGroupInfo])={
    if(session.admin){
      val groupSeq = postGroupSeq.map(post=>UserGroup(0,post.name,post.desc,active = true,null,session.userId,null,session.userId))
      onComplete(groupDal.insert(groupSeq)){
        case Success(groupWithIdSeq)=>
          val responseGroup = groupWithIdSeq.map(group=>PutGroupInfo(group.id,group.name,group.desc))
          complete(OK,ResponseSeqJson[PutGroupInfo](getHeader(200,session),responseGroup))
        case  Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }else complete(Forbidden, getHeader(403, session))

  }

}
