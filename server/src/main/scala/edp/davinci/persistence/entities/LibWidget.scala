package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable}
import slick.jdbc.H2Profile.api._

case class LibWidget(id: Long,
                     `type`: String,
                     active: Boolean,
                     create_time: String,
                     create_by: Long,
                     update_time: String,
                     update_by: Long) extends BaseEntity

case class SimpleLibWidget(`type`: String,
                           active: Boolean,
                           create_time: String,
                           create_by: Long,
                           update_time: String,
                           update_by: Long)

class LibWidgetTable(tag: Tag) extends BaseTable[LibWidget](tag, "lib_Widget") {
//  def `type` = column[String]("type")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, `type`, active, create_time, create_by, update_time, update_by) <> (LibWidget.tupled, LibWidget.unapply)
}
