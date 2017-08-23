package edp.davinci.rest

import edp.davinci.KV
import edp.davinci.persistence.entities._

case class LoginClass(username: String, password: String)

case class LoginUserInfo(title: String, name: String)

case class SessionClass(userId: Long, groupIdList: List[Long], admin: Boolean, currentTs: Long = System.currentTimeMillis())

case class ChangePwdClass(oldPass: String, newPass: String)

case class ChangeUserPwdClass(id: Long, oldPass: String, newPass: String)

case class SqlInfo(sqls: Array[String])

case class BaseInfo(id: Long, name: String)

case class ManualInfo(adHoc: Option[String] = None, manualFilters: Option[String] = None, params: Option[List[KV]] = None, shareInfo: Option[String] = None)

case class ShareWidgetInfo(userId: Long, infoId: Long)

case class ShareDashboardInfo(userId: Long, dashboardId: Long)

case class ShareInfo(userId: Long, infoId: Long, md5: String)

case class WidgetInfo(id: Long, widget_id: Long, flatTableId: Long, position_x: Int, position_y: Int, width: Int, length: Int, trigger_type: String, trigger_params: String, aesStr: String = "")

case class DashboardInfo(id: Long, name: String, pic: String, desc: String, publish: Boolean, widgets: Seq[WidgetInfo])

case class PostRelUserGroupSeq(payload: Seq[PostRelUserGroup])

case class PutRelUserGroupSeq(payload: Seq[PutRelUserGroup])

case class PostRelDashboardWidgetSeq(payload: Seq[PostRelDashboardWidget])

case class PutRelDashboardWidgetSeq(payload: Seq[PutRelDashboardWidget])

case class PostRelGroupFlatTableSeq(payload: Seq[PostRelGroupFlatTable])

case class PutRelGroupFlatTableSeq(payload: Seq[PutRelGroupFlatTable])

case class PostUserInfoSeq(payload: Seq[PostUserInfo])

case class PostFlatTableInfoSeq(payload: Seq[PostFlatTableInfo])

case class PostDashboardInfoSeq(payload: Seq[PostDashboardInfo])

case class PostGroupInfoSeq(payload: Seq[PostGroupInfo])

case class SimpleLibWidgetSeq(payload: Seq[LibWidget])

case class PostSourceInfoSeq(payload: Seq[PostSourceInfo])

case class SimpleSqlLogSeq(payload: Seq[SimpleSqlLog])

case class PostWidgetInfoSeq(payload: Seq[PostWidgetInfo])

case class SimpleRelUserGroupSeq(payload: Seq[SimpleRelUserGroup])

case class PutFlatTableInfoSeq(payload: Seq[PutFlatTableInfo])

case class PutDashboardSeq(payload: Seq[PutDashboardInfo])

case class PutGroupInfoSeq(payload: Seq[PutGroupInfo])

case class QueryLibWidgetSeq(payload: Seq[QueryLibWidget])

case class PutSourceInfoSeq(payload: Seq[PutSourceInfo])

case class SqlLogSeq(payload: Seq[SqlLog])

case class PutUserInfoSeq(payload: Seq[PutUserInfo])

case class PutWidgetInfoSeq(payload: Seq[PutWidgetInfo])

case class FlatTableResult(result: Seq[String] = null, totalCount: Long)

case class ShareResult(result: Seq[String], totalCount: Long)

case class ResponsePayload(response: String)

case class RequestJson[A](payload: A)

case class RequestSeqJson[A](payload: Seq[A])

case class ResponseHeader(code: Int, msg: String, token: String = "")

case class ResponseJson[A](header: ResponseHeader, payload: A)

case class ResponseSeqJson[A](header: ResponseHeader, payload: Seq[A])