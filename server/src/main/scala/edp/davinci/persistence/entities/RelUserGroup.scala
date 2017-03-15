package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class RelUserGroup(id: Long,
                        user_id: Long,
                        group_id: Long,
                        active: Boolean,
                        create_time: String,
                        create_by: Long,
                        update_time: String,
                        update_by: Long) extends BaseEntity

case class SimpleRelUserGroup(user_id: Long,
                              group_id: Long,
                              active: Boolean,
                              create_time: String,
                              create_by: Long,
                              update_time: String,
                              update_by: Long) extends SimpleBaseEntity

class RelUserGroupTable(tag: Tag) extends BaseTable[RelUserGroup](tag, "rel_user_group") {
  def user_id = column[Long]("user_id")

  def group_id = column[Long]("group_id")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, user_id, group_id, active, create_time, create_by, update_time, update_by) <> (RelUserGroup.tupled, RelUserGroup.unapply)
}
