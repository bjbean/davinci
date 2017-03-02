package edp.davinci.rest

import edp.davinci.persistence.base.BaseEntity
import edp.davinci.persistence.entities._

import scala.reflect.ClassTag

//token class
case class LoginClass(username: String, password: String)

case class SessionClass(userId: Long, groupIdList: List[Long], admin: Boolean, currentTs: Long = System.currentTimeMillis())

case class ChangePwdClass(oldPass: String, newPass: String)

//request body class
trait BaseClass

trait BaseClassN extends BaseClass {
  val name: String
}

case class BizlogicClass(source_id: Long, name: String, desc: String) extends BaseClassN

case class DashboardClass(name: String, desc: String, publish: Boolean) extends BaseClassN

case class GroupClass(name: String, desc: String) extends BaseClassN

case class LibWidgetClass(`type`: String) extends BaseClass

case class SourceClass(group_id: Long, name: String, desc: String, `type`: String, config: String) extends BaseClassN

case class SqlClass(bizlogic_id: Long, name: String, sql_type: String, sql_tmpl: String, sql_order: Int, desc: String) extends BaseClassN

case class SqlLogClass(sql_id: Long, start_time: String, end_time: String, active: Boolean, success: Boolean, error: String) extends BaseClass

case class UserClass(name: String, email: String, title: String) extends BaseClassN

case class WidgetClass(widgetlib_id: Long, bizlogic_id: Long, name: String, desc: String, trigger_type: String, trigger_params: String, publish: Boolean) extends BaseClassN

case class PaginationClass(pageIndex: Int, size: Int)

trait BaseClassSeq{
  val payload: Seq[BaseClass]
}

case class UserClassSeq(payload: Seq[UserClass]) extends BaseClassSeq

case class BizlogicClassSeq(payload: Seq[BizlogicClass]) extends BaseClassSeq

case class DashboardClassSeq(payload: Seq[DashboardClass]) extends BaseClassSeq

case class GroupClassSeq(payload: Seq[GroupClass]) extends BaseClassSeq

case class LibWidgetClassSeq(payload: Seq[LibWidgetClass]) extends BaseClassSeq

case class SourceClassSeq(payload: Seq[SourceClass]) extends BaseClassSeq

case class SqlClassSeq(payload: Seq[SqlClass]) extends BaseClassSeq

case class SqlLogClassSeq(payload: Seq[SqlLogClass]) extends BaseClassSeq

case class WidgetClassSeq(payload: Seq[WidgetClass]) extends BaseClassSeq

case class BizlogicSeq(payload: Seq[Bizlogic])

case class DashboardSeq(payload: Seq[Dashboard])

case class GroupSeq(payload: Seq[Group])

case class LibWidgetSeq(payload: Seq[LibWidget])

case class SourceSeq(payload: Seq[Source])

case class SqlSeq(payload: Seq[Sql])

case class SqlLogSeq(payload: Seq[SqlLog])

case class UserSeq(payload: Seq[User])

case class WidgetSeq(payload: Seq[Widget])

case class RequestJson[A](payload: A)

case class RequestSeqJson[A](payload: Seq[A])

case class ResponseHeader(code: Int, msg: String, token: String = null)

case class ResponseJson[A](header: ResponseHeader, payload: A)
