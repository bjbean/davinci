package edp.davinci.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import edp.davinci.persistence.base.BaseEntity
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import spray.json._

object JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  // davinci
  implicit val formatUserGroup: RootJsonFormat[UserGroup] = jsonFormat8(UserGroup)
  implicit val formatPostGroupInfo: RootJsonFormat[PostGroupInfo] = jsonFormat2(PostGroupInfo)
  implicit val formatPutGroupInfo: RootJsonFormat[PutGroupInfo] = jsonFormat3(PutGroupInfo)
  implicit val formatGroupClassSeq: RootJsonFormat[PostGroupInfoSeq] = jsonFormat1(PostGroupInfoSeq)
  implicit val formatPutGroupSeq: RootJsonFormat[PutGroupInfoSeq] = jsonFormat1(PutGroupInfoSeq)

  implicit val formatPostSourceInfo: RootJsonFormat[PostSourceInfo] = jsonFormat6(PostSourceInfo)
  implicit val formatPutSourceInfo: RootJsonFormat[PutSourceInfo] = jsonFormat7(PutSourceInfo)
  implicit val formatPostSourceInfoSeq: RootJsonFormat[PostSourceInfoSeq] = jsonFormat1(PostSourceInfoSeq)
  implicit val formatPutSourceInfoSeq: RootJsonFormat[PutSourceInfoSeq] = jsonFormat1(PutSourceInfoSeq)

  implicit val formatSqlLog: RootJsonFormat[SqlLog] = jsonFormat8(SqlLog)
  implicit val formatSimpleSqlLog: RootJsonFormat[SimpleSqlLog] = jsonFormat7(SimpleSqlLog)
  implicit val formatSimpleSqlLogSeq: RootJsonFormat[SimpleSqlLogSeq] = jsonFormat1(SimpleSqlLogSeq)
  implicit val formatSqlLogSeq: RootJsonFormat[SqlLogSeq] = jsonFormat1(SqlLogSeq)

  implicit val formatTablePrivilege: RootJsonFormat[Source] = jsonFormat12(Source)
  implicit val formatSimpleTablePrivilege: RootJsonFormat[SimpleSource] = jsonFormat11(SimpleSource)

  implicit val formatRelUserGroupResponse: RootJsonFormat[PostRelUserGroup] = jsonFormat1(PostRelUserGroup)
  implicit val formatRelUserGroupResponseSeq: RootJsonFormat[PostRelUserGroupSeq] = jsonFormat1(PostRelUserGroupSeq)
  implicit val formatRelUserGroupRequest: RootJsonFormat[PutRelUserGroup] = jsonFormat2(PutRelUserGroup)
  implicit val formatRelUserGroupRequestSeq: RootJsonFormat[PutRelUserGroupSeq] = jsonFormat1(PutRelUserGroupSeq)

  implicit val formatUser: RootJsonFormat[User] = jsonFormat11(User)
  implicit val formatPostUserInfo: RootJsonFormat[PostUserInfo] = jsonFormat6(PostUserInfo)
  implicit val formatPutUserInfo: RootJsonFormat[PutUserInfo] = jsonFormat6(PutUserInfo)
  implicit val formatQueryUserInfo: RootJsonFormat[QueryUserInfo] = jsonFormat5(QueryUserInfo)
  implicit val formatPostUserInfoSeq: RootJsonFormat[PostUserInfoSeq] = jsonFormat1(PostUserInfoSeq)
  implicit val formatPutUserInfoSeq: RootJsonFormat[PutUserInfoSeq] = jsonFormat1(PutUserInfoSeq)

  implicit val formatWidget: RootJsonFormat[Widget] = jsonFormat13(Widget)
  implicit val formatPostWidgetInfo: RootJsonFormat[PostWidgetInfo] = jsonFormat7(PostWidgetInfo)
  implicit val formatPutWidgetInfo: RootJsonFormat[PutWidgetInfo] = jsonFormat8(PutWidgetInfo)
  implicit val formatPutWidgetSeq: RootJsonFormat[PutWidgetInfoSeq] = jsonFormat1(PutWidgetInfoSeq)
  implicit val formatPostWidgetSeq: RootJsonFormat[PostWidgetInfoSeq] = jsonFormat1(PostWidgetInfoSeq)

  implicit val formatDashboard: RootJsonFormat[Dashboard] = jsonFormat9(Dashboard)
  implicit val formatDashboardInfo: RootJsonFormat[DashboardInfo] = jsonFormat7(DashboardInfo)
  implicit val formatPostDashboardInfo: RootJsonFormat[PostDashboardInfo] = jsonFormat3(PostDashboardInfo)
  implicit val formatPutDashboardInfo: RootJsonFormat[PutDashboardInfo] = jsonFormat4(PutDashboardInfo)
  implicit val formatDashboardSeq: RootJsonFormat[PutDashboardSeq] = jsonFormat1(PutDashboardSeq)
  implicit val formatDashboardClassSeq: RootJsonFormat[PostDashboardInfoSeq] = jsonFormat1(PostDashboardInfoSeq)

  implicit val formatPostRelDashboardWidget: RootJsonFormat[PostRelDashboardWidget] = jsonFormat6(PostRelDashboardWidget)
  implicit val formatPutRelDashboardWidget: RootJsonFormat[PutRelDashboardWidget] = jsonFormat7(PutRelDashboardWidget)
  implicit val formatPostRelDashboardWidgetSeq: RootJsonFormat[PostRelDashboardWidgetSeq] = jsonFormat1(PostRelDashboardWidgetSeq)
  implicit val formatPutRelDashboardWidgetSeq: RootJsonFormat[PutRelDashboardWidgetSeq] = jsonFormat1(PutRelDashboardWidgetSeq)

  implicit val formatPostRelGroupBizlogic: RootJsonFormat[PostRelGroupBizlogic] = jsonFormat2(PostRelGroupBizlogic)
  implicit val formatPutRelGroupBizlogic: RootJsonFormat[PutRelGroupBizlogic] = jsonFormat3(PutRelGroupBizlogic)
  implicit val formatPostRelGroupBizlogicSeq: RootJsonFormat[PostRelGroupBizlogicSeq] = jsonFormat1(PostRelGroupBizlogicSeq)
  implicit val formatPutRelGroupBizlogicSeq: RootJsonFormat[PutRelGroupBizlogicSeq] = jsonFormat1(PutRelGroupBizlogicSeq)

  implicit val formatBizlogic: RootJsonFormat[Bizlogic] = jsonFormat11(Bizlogic)
  implicit val formatPostBizlogicInfo: RootJsonFormat[PostBizlogicInfo] = jsonFormat6(PostBizlogicInfo)
  implicit val formatPutBizlogicInfo: RootJsonFormat[PutBizlogicInfo] = jsonFormat7(PutBizlogicInfo)
  implicit val formatQUeryBizlogic: RootJsonFormat[QueryBizlogic] = jsonFormat6(QueryBizlogic)
  implicit val formatPostBizlogicInfoSeq: RootJsonFormat[PostBizlogicInfoSeq] = jsonFormat1(PostBizlogicInfoSeq)
  implicit val formatPutBizlogicInfoSeq: RootJsonFormat[PutBizlogicInfoSeq] = jsonFormat1(PutBizlogicInfoSeq)

  implicit val formatLibWidget: RootJsonFormat[LibWidget] = jsonFormat8(LibWidget)
  implicit val formatLibWidgetSeq: RootJsonFormat[LibWidgetSeq] = jsonFormat1(LibWidgetSeq)
  implicit val formatSimpleLibWidget: RootJsonFormat[SimpleLibWidget] = jsonFormat7(SimpleLibWidget)
  implicit val formatLibWidgetClassSeq: RootJsonFormat[SimpleLibWidgetSeq] = jsonFormat1(SimpleLibWidgetSeq)

  implicit val formatLoginClass: RootJsonFormat[LoginClass] = jsonFormat2(LoginClass)

  implicit val formatChangePwdClass: RootJsonFormat[ChangePwdClass] = jsonFormat2(ChangePwdClass)

  implicit val formatChangeUserPwdClass: RootJsonFormat[ChangeUserPwdClass] = jsonFormat3(ChangeUserPwdClass)

  implicit val formatSessionClass: RootJsonFormat[SessionClass] = jsonFormat4(SessionClass)

  implicit val formatResponseHeader: RootJsonFormat[ResponseHeader] = jsonFormat3(ResponseHeader)

  implicit val formatBaseInfo: RootJsonFormat[BaseInfo] = jsonFormat2(BaseInfo)

  implicit val formatPutLoginUserInfo: RootJsonFormat[LoginUserInfo] = jsonFormat2(LoginUserInfo)

  implicit def formatRequestJson[A: JsonFormat]: RootJsonFormat[RequestJson[A]] = jsonFormat1(RequestJson.apply[A])

  implicit def formatRequestSeqJson[A: JsonFormat]: RootJsonFormat[RequestSeqJson[A]] = jsonFormat1(RequestSeqJson.apply[A])

  implicit def formatResponseJson[A: JsonFormat]: RootJsonFormat[ResponseJson[A]] = jsonFormat2(ResponseJson.apply[A])

  implicit def formatResponseSeqJson[A: JsonFormat]: RootJsonFormat[ResponseSeqJson[A]] = jsonFormat2(ResponseSeqJson.apply[A])

  implicit object formatBaseEntity extends RootJsonFormat[BaseEntity] {

    def write(obj: BaseEntity): JsValue = obj match {
      case biz: Bizlogic => biz.toJson
      case dashboard: Dashboard => dashboard.toJson
      case group: UserGroup => group.toJson
      case ligWidget: LibWidget => ligWidget.toJson
      case source: Source => source.toJson
      case sqlLog: SqlLog => sqlLog.toJson
      case user: User => user.toJson
      case widget: Widget => widget.toJson
      case unknown@_ => serializationError(s"Marshalling issue with $unknown")
    }

    def read(value: JsValue): Nothing = {
      value match {
        case unknown@_ => deserializationError(s"Unmarshalling issue with $unknown ")
      }
    }
  }


}


