package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

case class Bizlogic(id: Long,
                    source_id: Long,
                    name: String,
                    sql_tmpl: String,
                    result_table: String,
                    desc: String,
                    active: Boolean,
                    create_time: String,
                    create_by: Long,
                    update_time: String,
                    update_by: Long) extends BaseEntity

case class SimpleBizlogic(source_id: Long,
                          name: String,
                          sql_tmpl: String,
                          result_table: String,
                          desc: String,
                          active: Boolean,
                          create_time: String,
                          create_by: Long,
                          update_time: String,
                          update_by: Long) extends SimpleBaseEntity

case class PostBizlogicInfo(source_id: Long,
                            name: String,
                            sql_tmpl: String,
                            desc: String,
                            relBG: Seq[PostRelGroupBizlogic]) extends SimpleBaseEntity

case class PutBizlogicInfo(id: Long,
                           source_id: Long,
                           name: String,
                           sql_tmpl: String,
                           desc: String,
                           relBG: Seq[PostRelGroupBizlogic])


case class QueryBizlogic(id: Long,
                         source_id: Long,
                         name: String,
                         sql_tmpl: String,
                         result_table: String,
                         desc: String)

class BizlogicTable(tag: Tag) extends BaseTable[Bizlogic](tag, "bizlogic") {

  def sql_tmpl: Rep[String] = column[String]("sql_tmpl")

  def result_table: Rep[String] = column[String]("result_table")

  def source_id: Rep[Long] = column[Long]("source_id")

  def desc: Rep[String] = column[String]("desc")

  def create_time: Rep[String] = column[String]("create_time")

  def create_by: Rep[Long] = column[Long]("create_by")

  def update_time: Rep[String] = column[String]("update_time")

  def update_by: Rep[Long] = column[Long]("update_by")

  def * : ProvenShape[Bizlogic] = (id, source_id, name, sql_tmpl, result_table, desc, active, create_time, create_by, update_time, update_by) <> (Bizlogic.tupled, Bizlogic.unapply)
}
