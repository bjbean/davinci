package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class Dashboard(id: Long,
                     name: String,
                     pic: Option[String],
                     desc: String,
                     publish: Boolean,
                     active: Boolean,
                     create_time: String,
                     create_by: Long,
                     update_time: String,
                     update_by: Long) extends BaseEntity

case class PostDashboardInfo(name: String,
                             pic: String,
                             desc: String,
                             publish: Boolean) extends SimpleBaseEntity

case class PutDashboardInfo(id: Long,
                            name: String,
                            pic: String,
                            desc: String,
                            publish: Boolean,
                            active: Option[Boolean]=Some(true)
                           )

class DashboardTable(tag: Tag) extends BaseTable[Dashboard](tag, "dashboard") {

  //  def name = column[String]("name")
  def pic = column[Option[String]]("pic")

  def desc = column[String]("desc")

  def publish = column[Boolean]("publish")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, name, pic, desc, publish, active, create_time, create_by, update_time, update_by) <> (Dashboard.tupled, Dashboard.unapply)
}
