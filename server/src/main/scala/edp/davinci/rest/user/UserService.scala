package edp.davinci.rest.user

import edp.davinci.module._
import edp.davinci.persistence.entities._
import edp.davinci.rest.{LoginUserInfo, SessionClass}
import edp.davinci.util.CommonUtils
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future

class UserService (modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl){
  private lazy val uDal = modules.userDal
  private lazy val relDal = modules.relUserGroupDal

  def getAll(session: SessionClass): Future[Seq[QueryUserInfo]] ={
    val userTQ = uDal.getTableQuery
    if (session.admin)
      uDal.getDB.run(userTQ.filter(_.active === true).map(r => (r.id, r.email, r.title, r.name, r.admin)).result).mapTo[Seq[QueryUserInfo]]
    else
      uDal.getDB.run(userTQ.filter(_.id === session.userId).map(r => (r.id, r.email, r.title, r.name, r.admin)).result).mapTo[Seq[QueryUserInfo]]
  }

  def update(userSeq: Seq[PutUserInfo], session: SessionClass): Future[Unit] = {
    val userTQ = uDal.getTableQuery
    val query = DBIO.seq(userSeq.map(r => {
      userTQ.filter(_.id === r.id).map(user => (user.admin, user.name, user.email, user.title, user.update_by, user.update_time)).update(r.admin, r.name, r.email, r.title, session.userId, CommonUtils.currentTime)
    }): _*)
    uDal.getDB.run(query)
  }

  def updateLoginUser(loginUser: LoginUserInfo, session: SessionClass): Future[Int] = {
    val userTQ = uDal.getTableQuery
    uDal.getDB.run(userTQ.filter(_.id === session.userId).map(user => (user.name, user.title, user.update_by, user.update_time)).update(loginUser.name, loginUser.title, session.userId, CommonUtils.currentTime))
  }

  def getAllGroups(userId: Long): Future[Seq[PutRelUserGroup]] = {
    val relUGTQ = relDal.getTableQuery
    val query = relUGTQ.filter(rel => rel.active === true && rel.user_id === userId)
      .map(r => (r.id, r.group_id)).result
    relDal.getDB.run(query).mapTo[Seq[PutRelUserGroup]]
  }


  def deleteAllByUserId(userSeq: Seq[PutUserInfo]): Future[Unit] = {
    val relUGTQ = relDal.getTableQuery
    val query = DBIO.seq(userSeq.map(r => {
      relUGTQ.filter(_.user_id === r.id).map(_.active).update(false)
    }): _*)
    relDal.getDB.run(query)
  }
}
