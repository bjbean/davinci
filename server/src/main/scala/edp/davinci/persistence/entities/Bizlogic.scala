package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable}
import slick.jdbc.H2Profile.api._

case class Bizlogic(id: Long,
                    source_id: Long,
                    name: String,
                    desc: String,
                    active: Boolean,
                    create_time: String,
                    create_by: Long,
                    update_time: String,
                    update_by: Long) extends BaseEntity

case class SimpleBizlogic(source_id: Long,
                          name: String,
                          desc: String,
                          active: Boolean,
                          create_time: String,
                          create_by: Long,
                          update_time: String,
                          update_by: Long)

class BizlogicTable(tag: Tag) extends BaseTable[Bizlogic](tag, "bizlogic") {

  def source_id = column[Long]("source_id")

//  def name = column[String]("name")

  def desc = column[String]("desc")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, source_id, name, desc, active, create_time, create_by, update_time, update_by) <> (Bizlogic.tupled, Bizlogic.unapply)
}
