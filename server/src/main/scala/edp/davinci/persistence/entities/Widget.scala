package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape


case class Widget(id: Long,
                  widgetlib_id: Long,
                  bizlogic_id: Long,
                  name: String,
                  olap_sql: Option[String] = None,
                  desc: String,
                  chart_params: Option[String] = None,
                  publish: Boolean,
                  active: Boolean,
                  create_time: String,
                  create_by: Long,
                  update_time: String,
                  update_by: Long) extends BaseEntity


case class PostWidgetInfo(widgetlib_id: Long,
                          bizlogic_id: Long,
                          name: String,
                          olap_sql: String,
                          desc: String,
                          chart_params: String,
                          publish: Boolean) extends SimpleBaseEntity

case class PutWidgetInfo(id: Long,
                         widgetlib_id: Long,
                         bizlogic_id: Long,
                         name: String,
                         olap_sql: String,
                         desc: String,
                         chart_params: String,
                         publish: Boolean)

class WidgetTable(tag: Tag) extends BaseTable[Widget](tag, "widget") {

  def widgetlib_id: Rep[Long] = column[Long]("widgetlib_id")

  def bizlogic_id: Rep[Long] = column[Long]("bizlogic_id")

  def olap_sql: Rep[Option[String]] = column[Option[String]]("olap_sql", O.Default(null))

  def desc: Rep[String] = column[String]("desc")

  def chart_params: Rep[Option[String]] = column[Option[String]]("chart_params", O.Default(null))

  def publish: Rep[Boolean] = column[Boolean]("publish")

  def create_time: Rep[String] = column[String]("create_time")

  def create_by: Rep[Long] = column[Long]("create_by")

  def update_time: Rep[String] = column[String]("update_time")

  def update_by: Rep[Long] = column[Long]("update_by")

  def * : ProvenShape[Widget] = (id, widgetlib_id, bizlogic_id, name, olap_sql, desc, chart_params, publish, active, create_time, create_by, update_time, update_by) <> (Widget.tupled, Widget.unapply)
}
