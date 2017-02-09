package edp.davinci.persistence.base

import slick.jdbc.H2Profile.api._
import slick.lifted.Tag

abstract class BaseTable[T](tag: Tag, desc: String) extends Table[T](tag, desc) {
  def id = column[Long]("id", O.AutoInc)

  def active = column[Boolean]("active")

  def domain_id = column[Long]("domain_id")

  def name = column[String]("name")

  def email = column[String]("email")

  def `type` = column[String]("type")

}
