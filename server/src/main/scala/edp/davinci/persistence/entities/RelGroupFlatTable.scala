package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class RelGroupFlatTable(id: Long,
                             group_id: Long,
                             flatTable_id: Long,
                             sql_params: String,
                             active: Boolean,
                             create_time: String,
                             create_by: Long,
                             update_time: String,
                             update_by: Long) extends BaseEntity

case class SimpleRelGroupFlatTable(group_id: Long,
                                   flatTable_id: Long,
                                   sql_params: String,
                                   active: Boolean,
                                   create_time: String,
                                   create_by: Long,
                                   update_time: String,
                                   update_by: Long) extends SimpleBaseEntity

case class PostRelGroupFlatTable(group_id: Long,
                                 sql_params: String) extends SimpleBaseEntity

case class PutRelGroupFlatTable(id: Long,
                                group_id: Long,
                                sql_params: String)

class RelGroupFlatTblTable(tag: Tag) extends BaseTable[RelGroupFlatTable](tag, "rel_group_flattable") {
  def group_id = column[Long]("group_id")

  def flatTable_id = column[Long]("flatTable_id")

  def sql_params = column[String]("sql_params")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, group_id, flatTable_id, sql_params, active, create_time, create_by, update_time, update_by) <> (RelGroupFlatTable.tupled, RelGroupFlatTable.unapply)
}