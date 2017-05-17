package edp.davinci.persistence.entities

import edp.davinci.persistence.base.{BaseEntity, BaseTable, SimpleBaseEntity}
import slick.jdbc.H2Profile.api._

case class User(id: Long,
                email: String,
                password: String,
                title: String,
                name: String,
                admin: Boolean,
                active: Boolean,
                create_time: String,
                create_by: Long,
                update_time: String,
                update_by: Long) extends BaseEntity

case class PostUserInfo(email: String,
                        password: String,
                        title: String,
                        name: String,
                        admin: Boolean,
                        relUG: Seq[PostRelUserGroup]
                       ) extends SimpleBaseEntity

case class PutUserInfo(id: Long,
                       email: String,
                       title: String,
                       name: String,
                       admin: Boolean,
                       relUG: Seq[PostRelUserGroup])

case class QueryUserInfo(id: Long,
                         email: String,
                         title: String,
                         name: String,
                         admin: Boolean,
                         active: Boolean)

class UserTable(tag: Tag) extends BaseTable[User](tag, "user") {

  def password = column[String]("password")

  def title = column[String]("title")

  override def name = column[String]("name")

  def admin = column[Boolean]("admin")

  def create_time = column[String]("create_time")

  def create_by = column[Long]("create_by")

  def update_time = column[String]("update_time")

  def update_by = column[Long]("update_by")

  def * = (id, email, password, title, name, admin, active, create_time, create_by, update_time, update_by) <> (User.tupled, User.unapply)
}
