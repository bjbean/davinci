package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class Widget(id: Long,
                  widgetlib_id: Long,
                  bizlogic_id: Long,
                  name: String,
                  desc: String,
                  trigger_type: String,
                  trigger_params: String,
                  publish: Boolean,
                  active: Boolean,
                  create_time: String,
                  create_by: Long,
                  update_time: String,
                  update_by: Long) extends BaseEntity

case class SimpleWidget(widgetlib_id: Long,
                        bizlogic_id: Long,
                        name: String,
                        desc: String,
                        trigger_type: String,
                        trigger_params: String,
                        publish: Boolean,
                        active: Boolean,
                        create_time: String,
                        create_by: Long,
                        update_time: String,
                        update_by: Long) extends SimpleBaseEntity

class WidgetTable(tag: Tag) extends BaseTable[Widget](tag, "Widget") {

  def widgetlib_id = column[Long]("widgetlib_id")

  override def bizlogic_id = column[Long]("bizlogic_id")

  //  def name = column[String]("name")

  def desc = column[String]("desc")

  def trigger_type = column[String]("trigger_type")

  def trigger_params = column[String]("trigger_params")

  def publish = column[Boolean]("publish")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, widgetlib_id, bizlogic_id, name, desc, trigger_type, trigger_params, publish, active, create_time, create_by, update_time, update_by) <> (Widget.tupled, Widget.unapply)
}
