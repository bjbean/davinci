package edp.davinci.rest.group

import edp.davinci.util.ResponseUtils
import edp.davinci.module._
import edp.davinci.persistence.entities.PutGroupInfo
import edp.davinci.rest.SessionClass
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future


class GroupService(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) {
  private lazy val gDal = modules.groupDal
  private lazy val db = gDal.getDB
  private lazy val groupTQ = gDal.getTableQuery
  private lazy val relGFTQ = modules.relGroupFlatTableDal.getTableQuery
  private lazy val relGUTQ = modules.relUserGroupDal.getTableQuery

  def getAll(session: SessionClass, active: Boolean): Future[Seq[(Long, String, Option[String], Boolean)]] = {
    if (session.admin)
      if (active)
        db.run(groupTQ.filter(_.active).map(r => (r.id, r.name, r.desc, r.active)).result)
      else
        db.run(groupTQ.map(r => (r.id, r.name, r.desc, r.active)).result)
    else if (active)
      db.run(groupTQ.withFilter(g => {
        g.active === true
        g.id inSet session.groupIdList
      }).map(r => (r.id, r.name, r.desc, r.active)).result)
    else
      db.run(groupTQ.filter(g => g.id inSet session.groupIdList).map(r => (r.id, r.name, r.desc, r.active)).result)
  }

  def update(groupSeq: Seq[PutGroupInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(groupSeq.map(r => {
      groupTQ.filter(_.id === r.id).map(group => (group.name, group.desc, group.active, group.update_by, group.update_time)).update(r.name, Some(r.desc), r.active.getOrElse(true), session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def deleteGroup(groupId: Long): Future[Int] = {
    db.run(groupTQ.filter(_.id ===  groupId).delete)
  }

  def deleteRelGF(groupId: Long): Future[Int] = {
    db.run(relGFTQ.filter(_.group_id === groupId).delete)
  }

  def deleteRelGU(groupId: Long): Future[Int] = {
    db.run(relGUTQ.filter(_.group_id === groupId).delete)
  }

}
