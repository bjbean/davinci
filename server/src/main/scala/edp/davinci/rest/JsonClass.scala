package edp.davinci.rest

import edp.davinci.persistence.base.BaseEntity
import edp.davinci.persistence.entities._

import scala.reflect.ClassTag

//token class
case class LoginClass(username: String, password: String)

case class SessionClass(userId: Long, groupIdList: List[Long], admin: Boolean, currentTs: Long = System.currentTimeMillis())

case class ChangePwdClass(oldPass: String, newPass: String)

case class PaginationClass(pageIndex: Int, size: Int)

case class SimpleUserSeq(payload: Seq[SimpleUser])

case class SimpleBizlogicSeq(payload: Seq[SimpleBizlogic])

case class SimpleDashboardSeq(payload: Seq[SimpleDashboard]) 

case class SimpleGroupSeq(payload: Seq[SimpleGroup])

case class SimpleLibWidgetSeq(payload: Seq[LibWidget])

case class SimpleSourceSeq(payload: Seq[SimpleSource])

case class SimpleSqlSeq(payload: Seq[SimpleSql])

case class SimpleSqlLogSeq(payload: Seq[SimpleSqlLog])

case class SimpleWidgetSeq(payload: Seq[SimpleWidget])

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

case class ResponseSeqJson[A](header: ResponseHeader, payload: Seq[A])