package edp.davinci.rest.user

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities._
import edp.davinci.rest.{LoginUserInfo, ResponseSeqJson, SessionClass}
import edp.davinci.util.CommonUtils
import edp.davinci.util.CommonUtils.getHeader
import slick.jdbc.MySQLProfile.api._
import edp.davinci.util.JsonProtocol._
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait UserRepository extends ConfigurationModuleImpl with PersistenceModuleImpl {
  def getAll: Future[Seq[QueryUserInfo]] =
    db.run(userQuery.filter(_.active === true).map(r => (r.id, r.email, r.title, r.name, r.admin)).result).mapTo[Seq[QueryUserInfo]]


  def update(userSeq: Seq[PutUserInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(userSeq.map(r => {
      userQuery.filter(_.id === r.id).map(user => (user.admin, user.name, user.email, user.title, user.update_by, user.update_time)).update(r.admin, r.name, r.email, r.title, session.userId, CommonUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def updateLoginUser(loginUser: LoginUserInfo, session: SessionClass): Future[Int] = {
    db.run(userQuery.filter(_.id === session.userId).map(user => (user.name, user.title, user.update_by, user.update_time)).update(loginUser.name, loginUser.title, session.userId, CommonUtils.currentTime))
  }

  def getAllGroups(userId: Long): Future[Seq[PutRelUserGroup]] = {
    val query = relUserGroupQuery.filter(rel => rel.active === true && rel.user_id === userId)
      .map(r => (r.id, r.group_id)).result
    db.run(query).mapTo[Seq[PutRelUserGroup]]
  }


  def deleteAllByUserId(userSeq: Seq[PutUserInfo]): Future[Unit] = {
    val query = DBIO.seq(userSeq.map(r => {
      relUserGroupQuery.filter(_.user_id === r.id).map(_.active).update(false)
    }): _*)
    db.run(query)
  }
}


trait UserService extends Directives with UserRepository {
  def getAllUsersComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(getAll) {
        case Success(userSeq) =>
          if (userSeq.nonEmpty) complete(OK, ResponseSeqJson[QueryUserInfo](getHeader(200, session), userSeq))
          else complete(NotFound, getHeader(404, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def putUserComplete(session: SessionClass, userSeq: Seq[PutUserInfo]): Route = {
    if (session.admin) {
      val future = update(userSeq, session)
      onComplete(future) {
        case Success(_) =>
          onComplete(deleteAllByUserId(userSeq)) {
            case Success(_) =>
              val relSeq = for {rel <- userSeq.head.relUG
              } yield RelUserGroup(0, userSeq.head.id, rel.group_id, active = true, null, session.userId, null, session.userId)
              onComplete(relUserGroupDal.insert(relSeq)) {
                case Success(_) => complete(OK, getHeader(200, session))
                case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
              }
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def putLoginUserComplete(session: SessionClass, user: LoginUserInfo): Route = {
    if (session.admin) {
      val future = updateLoginUser(user, session)
      onComplete(future) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

  def getGroupsByUserIdComplete(session: SessionClass, userId: Long): Route = {
    val future = getAllGroups(userId)
    onComplete(future) {
      case Success(relSeq) =>
        if (relSeq.nonEmpty) complete(OK, ResponseSeqJson[PutRelUserGroup](getHeader(200, session), relSeq))
        else complete(NotFound, getHeader(404, session))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  def postUserComplete(session: SessionClass, userSeq: Seq[PostUserInfo]): Route = {
    if (session.admin) {
      val userEntity = userSeq.map(postUser => User(0, postUser.email, postUser.password, postUser.title, postUser.name, postUser.admin, active = true, null, session.userId, null, session.userId))
      onComplete(userDal.insert(userEntity)) {
        case Success(users) =>
          val relEntity = userSeq.head.relUG.map(rel => RelUserGroup(0, users.head.id, rel.group_id, active = true, null, session.userId, null, session.userId))
          onComplete(relUserGroupDal.insert(relEntity)) {
            case Success(_) =>
              val queryUsers = users.map(user => QueryUserInfo(user.id, user.email, user.title, user.name, user.admin))
              complete(OK, ResponseSeqJson[QueryUserInfo](getHeader(200, session), queryUsers))
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }
}
