package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable}
import slick.jdbc.H2Profile.api._

case class Sql(id: Long,
               bizlogic_id: Long,
               name: String,
               sql_type: String,
               sql_tmpl: String,
               sql_order: Int,
               desc: String,
               active: Boolean,
               create_time: String,
               create_by: Long,
               update_time: String,
               update_by: Long) extends BaseEntity

case class SimpleSql(bizlogic_id: Long,
                     name: String,
                     sql_type: String,
                     sql_tmpl: String,
                     sql_order: Int,
                     desc: String,
                     active: Boolean,
                     create_time: String,
                     create_by: Long,
                     update_time: String,
                     update_by: Long)

class SqlTable(tag: Tag) extends BaseTable[Sql](tag, "sql") {

  def bizlogic_id = column[Long]("bizlogic_id")

  //  def name = column[String]("name")

  def sql_type = column[String]("sql_type")

  def sql_tmpl = column[String]("sql_tmpl")

  def sql_order = column[Int]("sql_order")

  def desc = column[String]("desc")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, bizlogic_id, name, sql_type, sql_tmpl, sql_order, desc, active, create_time, create_by, update_time, update_by) <> (Sql.tupled, Sql.unapply)
}
