package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class Bizlogic(id: Long,
                    name: String,
                    desc: String,
                    active: Boolean,
                    create_time: String,
                    create_by: Long,
                    update_time: String,
                    update_by: Long) extends BaseEntity

case class SimpleBizlogic(name: String,
                          desc: String,
                          active: Boolean,
                          create_time: String,
                          create_by: Long,
                          update_time: String,
                          update_by: Long) extends SimpleBaseEntity

class BizlogicTable(tag: Tag) extends BaseTable[Bizlogic](tag, "bizlogic") {

  //  def name = column[String]("name")

  def desc = column[String]("desc")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, name, desc, active, create_time, create_by, update_time, update_by) <> (Bizlogic.tupled, Bizlogic.unapply)
}
