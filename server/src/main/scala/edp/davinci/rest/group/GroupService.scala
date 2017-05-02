package edp.davinci.rest.group

import edp.davinci.module._
import edp.davinci.persistence.entities.PutGroupInfo
import edp.davinci.rest.SessionClass
import edp.davinci.util.CommonUtils
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future


class GroupService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
 private lazy val gDal = modules.groupDal

  def getAll: Future[Seq[PutGroupInfo]] = {
    val groupTQ = gDal.getTableQuery
    gDal.getDB.run(groupTQ.filter(_.active === true).map(r => (r.id, r.name, r.desc)).result).mapTo[Seq[PutGroupInfo]]
  }

  def update(groupSeq: Seq[PutGroupInfo], session: SessionClass): Future[Unit] = {
    val groupTQ = gDal.getTableQuery
    val query = DBIO.seq(groupSeq.map(r => {
      groupTQ.filter(_.id === r.id).map(group => (group.name, group.desc, group.update_by, group.update_time)).update(r.name, r.desc, session.userId, CommonUtils.currentTime)
    }): _*)
    gDal.getDB.run(query)
  }


//  def getAllGroupsComplete(session: SessionClass): Route = {
//    if (session.admin) {
//      onComplete(getAll) {
//        case Success(groupSeq) =>
//          if (groupSeq.nonEmpty) complete(OK, ResponseSeqJson[PutGroupInfo](getHeader(200, session), groupSeq))
//          else complete(NotFound, getHeader(404, session))
//        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
//      }
//    } else complete(Forbidden, getHeader(403, session))
//  }
//
//  def putUserComplete(session: SessionClass, groupSeq: Seq[PutGroupInfo]): Route = {
//    if (session.admin) {
//      val future = update(groupSeq, session)
//      onComplete(future) {
//        case Success(_) => complete(OK, getHeader(200, session))
//        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
//      }
//    } else complete(Forbidden, getHeader(403, session))
//  }
//
//  def postGroup(session: SessionClass, postGroupSeq: Seq[PostGroupInfo]) = {
//    if (session.admin) {
//      val groupSeq = postGroupSeq.map(post => UserGroup(0, post.name, post.desc, active = true, null, session.userId, null, session.userId))
//      onComplete(groupDal.insert(groupSeq)) {
//        case Success(groupWithIdSeq) =>
//          val responseGroup = groupWithIdSeq.map(group => PutGroupInfo(group.id, group.name, group.desc))
//          complete(OK, ResponseSeqJson[PutGroupInfo](getHeader(200, session), responseGroup))
//        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
//      }
//    } else complete(Forbidden, getHeader(403, session))
//
//  }

}
