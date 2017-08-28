package edp.davinci.rest.group

import edp.davinci.ModuleInstance
import edp.davinci.module.DbModule._
import edp.davinci.persistence.entities.PutGroupInfo
import edp.davinci.rest.SessionClass
import edp.davinci.util.ResponseUtils
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

object GroupService extends GroupService

trait GroupService {
  private lazy val modules = ModuleInstance.getModule

  def getAll(session: SessionClass): Future[Seq[(Long, String, Option[String])]] = {
    if (session.admin)
      db.run(modules.groupQuery.map(r => (r.id, r.name, r.desc)).result)
    else
      db.run(modules.groupQuery.filter(g => g.id inSet session.groupIdList).map(r => (r.id, r.name, r.desc)).result)
  }

  def update(groupSeq: Seq[PutGroupInfo], session: SessionClass): Future[Unit] = {
    val query = DBIO.seq(groupSeq.map(r => {
      modules.groupQuery.filter(_.id === r.id).map(group => (group.name, group.desc, group.update_by, group.update_time)).update(r.name, Some(r.desc), session.userId, ResponseUtils.currentTime)
    }): _*)
    db.run(query)
  }

  def deleteGroup(groupId: Long): Future[Int] = {
    modules.groupDal.deleteById(groupId)
  }

  def deleteRelGF(groupId: Long): Future[Int] = {
    modules.relGroupViewDal.deleteByFilter(_.group_id === groupId)
  }

  def deleteRelGU(groupId: Long): Future[Int] = {
    modules.relUserGroupDal.deleteByFilter(_.group_id === groupId)
  }

}