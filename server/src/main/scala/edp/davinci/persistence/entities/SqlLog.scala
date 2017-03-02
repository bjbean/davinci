package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class SqlLog(id: Long,
                  sql_id: Long,
                  user_id: Long,
                  start_time: String,
                  end_time: String,
                  active: Boolean,
                  success: Boolean,
                  error: String) extends BaseEntity

case class SimpleSqlLog(sql_id: Long,
                        user_id: Long,
                        start_time: String,
                        end_time: String,
                        active: Boolean,
                        success: Boolean,
                        error: String) extends SimpleBaseEntity

class SqlLogTable(tag: Tag) extends BaseTable[SqlLog](tag, "sql_log") {
  def sql_id = column[Long]("sql_id")

  def user_id = column[Long]("user_id")

  def start_time = column[String]("start_time")

  def end_time = column[String]("end_time")

  def success = column[Boolean]("success")

  def error = column[String]("error")

  def * = (id, sql_id, user_id, start_time, end_time, active, success, error) <> (SqlLog.tupled, SqlLog.unapply)
}
