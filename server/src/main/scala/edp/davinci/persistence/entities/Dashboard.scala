package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable}
import slick.jdbc.H2Profile.api._

case class Dashboard(id: Long,
                     domain_id: Long,
                     name: String,
                     desc: String,
                     publish: Boolean,
                     active: Boolean,
                     create_time: String,
                     create_by: Long,
                     update_time: String,
                     update_by: Long) extends BaseEntity

case class SimpleDashboard(domain_id: Long,
                           name: String,
                           desc: String,
                           publish: Boolean,
                           active: Boolean,
                           create_time: String,
                           create_by: Long,
                           update_time: String,
                           update_by: Long)

class DashboardTable(tag: Tag) extends BaseTable[Dashboard](tag, "dashboard") {
//  def domain_id = column[Long]("domain_id")

//  def name = column[String]("name")

  def desc = column[String]("desc")

  def publish = column[Boolean]("publish")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, domain_id, name, desc, publish, active, create_time, create_by, update_time, update_by) <> (Dashboard.tupled, Dashboard.unapply)
}