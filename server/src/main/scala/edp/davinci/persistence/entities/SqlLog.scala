package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

case class SqlLog(id: Long,
                  user_id: Long,
                  user_email: String,
                  sql: String,
                  start_time: String,
                  end_time: String,
                  success: Boolean,
                  error: String) extends BaseEntity

case class SimpleSqlLog(user_id: Long,
                        user_email: String,
                        sql: String,
                        start_time: String,
                        end_time: String,
                        success: Boolean,
                        error: String) extends SimpleBaseEntity


class SqlLogTable(tag: Tag) extends BaseTable[SqlLog](tag, "sql_log") {
  def user_email: Rep[String] = column[String]("user_email")

  def sql: Rep[String] = column[String]("sql")

  def user_id: Rep[Long] = column[Long]("user_id")

  def start_time: Rep[String] = column[String]("start_time")

  def end_time: Rep[String] = column[String]("end_time")

  def success: Rep[Boolean] = column[Boolean]("success")

  def error: Rep[String] = column[String]("error")

  def * : ProvenShape[SqlLog] = (id, user_id, user_email, sql, start_time, end_time, success, error) <> (SqlLog.tupled, SqlLog.unapply)
}
