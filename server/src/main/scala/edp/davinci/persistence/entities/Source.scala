package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable}
import slick.jdbc.H2Profile.api._

case class Source(id: Long,
                  group_id: Long,
                  name: String,
                  desc: String,
                  `type`: String,
                  config: String,
                  active: Boolean,
                  create_time: String,
                  create_by: Long,
                  update_time: String,
                  update_by: Long) extends BaseEntity

case class SimpleSource(group_id: Long,
                        name: String,
                        desc: String,
                        `type`: String,
                        config: String,
                        active: Boolean,
                        create_time: String,
                        create_by: Long,
                        update_time: String,
                        update_by: Long)

class SourceTable(tag: Tag) extends BaseTable[Source](tag, "source") {
  //  def domain_id = column[Long]("domain_id")

  def group_id = column[Long]("group_id")

//  def name = column[String]("name")

  def desc = column[String]("desc")

  override def `type` = column[String]("type")

  def config = column[String]("config")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, group_id, name, desc, `type`, config, active, create_time, create_by, update_time, update_by) <> (Source.tupled, Source.unapply)
}
