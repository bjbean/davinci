package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class RelGroupBizlogic(id: Long,
                            group_id: Long,
                            bizlogic_id: Long,
                            sql_params: String,
                            active: Boolean,
                            create_time: String,
                            create_by: Long,
                            update_time: String,
                            update_by: Long) extends BaseEntity

case class SimpleRelGroupBizlogic(group_id: Long,
                                  bizlogic_id: Long,
                                  sql_params: String,
                                  active: Boolean,
                                  create_time: String,
                                  create_by: Long,
                                  update_time: String,
                                  update_by: Long) extends SimpleBaseEntity

class RelGroupBizlogicTable(tag: Tag) extends BaseTable[RelGroupBizlogic](tag, "rel_group_bizlogic") {
  override def group_id = column[Long]("group_id")

  override def bizlogic_id = column[Long]("bizlogic_id")

  def sql_params = column[String]("sql_params")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, group_id, bizlogic_id, sql_params, active, create_time, create_by, update_time, update_by) <> (RelGroupBizlogic.tupled, RelGroupBizlogic.unapply)
}
