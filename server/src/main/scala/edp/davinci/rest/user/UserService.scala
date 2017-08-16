package edp.davinci.rest.user

import edp.davinci.util.ResponseUtils
import edp.davinci.module._
import edp.davinci.persistence.entities._
import edp.davinci.rest.{LoginUserInfo, SessionClass}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class UserService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val uDal = modules.userDal
  private lazy val relDal = modules.relUserGroupDal
  private lazy val userTQ = uDal.getTableQuery
  private lazy val relUGTQ = relDal.getTableQuery
  private lazy val db = uDal.getDB

  def getAll(session: SessionClass, active: Boolean = true): Future[Seq[(Long, String, String, String, Boolean, Boolean)]] = {
    val tmpQuery = if (active) userTQ.filter(u => u.active && !u.admin) else userTQ.filter(!_.admin)
    if (session.admin)
      db.run(tmpQuery.map(r => (r.id, r.email, r.title, r.name, r.admin, r.active)).result)
    else
      db.run(tmpQuery.filter(_.id === session.userId).map(r => (r.id, r.email, r.title, r.name, r.admin, r.active)).result)
  }

  def update(userSeq: Seq[PutUserInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(userSeq.map(r => {
      userTQ.filter(_.id === r.id).map(user => (user.admin, user.name, user.email, user.title, user.active, user.update_by, user.update_time)).update(r.admin, r.name, r.email, r.title, r.active.getOrElse(true), session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def updateLoginUser(loginUser: LoginUserInfo, session: SessionClass): Future[Int] = {
    db.run(userTQ.filter(_.id === session.userId).map(user => (user.name, user.title, user.update_by, user.update_time)).update(loginUser.name, loginUser.title, session.userId, ResponseUtils.currentTime))
  }

  def getAllGroups(userId: Long): Future[Seq[PutRelUserGroup]] = {
    val query = relUGTQ.filter(rel => rel.user_id === userId)
      .map(r => (r.id, r.group_id)).result
    db.run(query).mapTo[Seq[PutRelUserGroup]]
  }


  def deleteFromRelByUserId(userIdSeq: Seq[Long]): Future[Unit] = {
    val query = DBIO.seq(userIdSeq.map(userId => {
      relUGTQ.filter(_.user_id === userId).delete
    }): _*)
    db.run(query)
  }

  def getUserInfo(session: SessionClass): Future[Seq[(Long, String, String, String, Boolean, Boolean)]] = {
    db.run(userTQ.filter(_.id === session.userId).map(r => (r.id, r.email, r.title, r.name, r.admin, r.active)).result)
  }

  def deleteUser(userId: Long): Future[Int] = {
    db.run(userTQ.filter(_.id === userId).delete)
  }

}
