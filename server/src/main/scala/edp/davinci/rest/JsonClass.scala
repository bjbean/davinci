package edp.davinci.rest

import edp.davinci.persistence.entities._

//token class
case class LoginClass(username: String, password: String)

case class LoginUserInfo(title: String, name: String)

case class SessionClass(userId: Long, groupIdList: List[Long], admin: Boolean, currentTs: Long = System.currentTimeMillis())

case class ChangePwdClass(oldPass: String, newPass: String)

case class ChangeUserPwdClass(id: Long, oldPass: String, newPass: String)

//case class PaginationClass(pageIndex: Int, size: Int)
case class BaseInfo(id: Long, name: String)

case class SqlInfo(sqlList:List[String])

case class DashboardInfo(id: Long, dashboard_id: Long, position_x: Int, position_y: Int, length: Int, width: Int, widgets: PutWidgetInfo)

case class PostRelUserGroupSeq(payload: Seq[PostRelUserGroup])

case class PutRelUserGroupSeq(payload: Seq[PutRelUserGroup])

case class PostRelDashboardWidgetSeq(payload:Seq[PostRelDashboardWidget])

case class PutRelDashboardWidgetSeq(payload:Seq[PutRelDashboardWidget])

case class PostRelGroupBizlogicSeq(payload:Seq[PostRelGroupBizlogic])

case class PutRelGroupBizlogicSeq(payload:Seq[PutRelGroupBizlogic])

case class PostUserInfoSeq(payload: Seq[PostUserInfo])

case class PostBizlogicInfoSeq(payload: Seq[PostBizlogicInfo])

case class PostDashboardInfoSeq(payload: Seq[PostDashboardInfo])

case class PostGroupInfoSeq(payload: Seq[PostGroupInfo])

case class SimpleLibWidgetSeq(payload: Seq[LibWidget])

case class PostSourceInfoSeq(payload: Seq[PostSourceInfo])

case class SimpleSqlLogSeq(payload: Seq[SimpleSqlLog])

case class PostWidgetInfoSeq(payload: Seq[PostWidgetInfo])

case class SimpleRelUserGroupSeq(payload: Seq[SimpleRelUserGroup])

case class SimpleRelDashboardWidgetSeq(payload: Seq[SimpleRelDashboardWidget])

case class PutBizlogicInfoSeq(payload: Seq[PutBizlogicInfo])

case class PutDashboardSeq(payload: Seq[PutDashboardInfo])

case class PutGroupInfoSeq(payload: Seq[PutGroupInfo])

case class LibWidgetSeq(payload: Seq[LibWidget])

case class PutSourceInfoSeq(payload: Seq[PutSourceInfo])

case class SqlLogSeq(payload: Seq[SqlLog])

case class PutUserInfoSeq(payload: Seq[PutUserInfo])

case class PutWidgetInfoSeq(payload: Seq[PutWidgetInfo])

case class RequestJson[A](payload: A)

case class RequestSeqJson[A](payload: Seq[A])

case class ResponseHeader(code: Int, msg: String, token: String = null)

case class ResponseJson[A](header: ResponseHeader, payload: A)

case class ResponseSeqJson[A](header: ResponseHeader, payload: Seq[A])