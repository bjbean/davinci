package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable}
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape


case class ShareInfo(id: Long,
                     identifier: String,
                     widget_sql: String,
                     widget: String,
                     connection_url: String,
                     create_time: String,
                     create_by: Long,
                     expiration: Long) extends BaseEntity

class ShareInfoTable(tag: Tag) extends BaseTable[ShareInfo](tag, "share_info") {

  def identifier = column[String]("identifier")

  def widget_sql = column[String]("widget_sql")

  def widget = column[String]("widget")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def connection_url = column[String]("connection_url")

  def expiration = column[Long]("expiration")

  def * : ProvenShape[ShareInfo] = (id, identifier, widget_sql, widget, connection_url, create_time, create_by, expiration) <> (ShareInfo.tupled, ShareInfo.unapply)
}
