package edp.davinci.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import edp.davinci.persistence.base.BaseEntity
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import spray.json._

object JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  // davinci
  implicit val formatGroup: RootJsonFormat[Group] = jsonFormat8(Group)
  implicit val formatSimpleGroup: RootJsonFormat[SimpleGroup] = jsonFormat7(SimpleGroup)

  implicit val formatSql: RootJsonFormat[Sql] = jsonFormat12(Sql)
  implicit val formatSimpleSql: RootJsonFormat[SimpleSql] = jsonFormat11(SimpleSql)

  implicit val formatSqlLog: RootJsonFormat[SqlLog] = jsonFormat8(SqlLog)
  implicit val formatSimpleSqlLog: RootJsonFormat[SimpleSqlLog] = jsonFormat7(SimpleSqlLog)

  implicit val formatTablePrivilege: RootJsonFormat[Source] = jsonFormat11(Source)
  implicit val formatSimpleTablePrivilege: RootJsonFormat[SimpleSource] = jsonFormat10(SimpleSource)

  implicit val formatUser: RootJsonFormat[User] = jsonFormat11(User)
  implicit val formatSimpleUser: RootJsonFormat[SimpleUser] = jsonFormat10(SimpleUser)

  implicit val formatRelUserGroup: RootJsonFormat[RelUserGroup] = jsonFormat8(RelUserGroup)
  implicit val formatRelSimpleUserGroup: RootJsonFormat[SimpleRelUserGroup] = jsonFormat7(SimpleRelUserGroup)

  implicit val formatDashboard: RootJsonFormat[Dashboard] = jsonFormat9(Dashboard)
  implicit val formatSimpleDashboard: RootJsonFormat[SimpleDashboard] = jsonFormat8(SimpleDashboard)

  implicit val formatRelDashboardWidget: RootJsonFormat[RelDashboardWidget] = jsonFormat12(RelDashboardWidget)
  implicit val formatSimpleRelDashboardWidget: RootJsonFormat[SimpleRelDashboardWidget] = jsonFormat11(SimpleRelDashboardWidget)

  implicit val formatWidget: RootJsonFormat[Widget] = jsonFormat13(Widget)
  implicit val formatSimpleWidget: RootJsonFormat[SimpleWidget] = jsonFormat12(SimpleWidget)

  implicit val formatLibWidget: RootJsonFormat[LibWidget] = jsonFormat7(LibWidget)
  implicit val formatSimpleLibWidget: RootJsonFormat[SimpleLibWidget] = jsonFormat6(SimpleLibWidget)

  implicit val formatBizlogic: RootJsonFormat[Bizlogic] = jsonFormat8(Bizlogic)
  implicit val formatSimpleBizlogic: RootJsonFormat[SimpleBizlogic] = jsonFormat7(SimpleBizlogic)

  implicit val formatRelGroupBizlogic: RootJsonFormat[RelGroupBizlogic] = jsonFormat9(RelGroupBizlogic)
  implicit val formatSimpleRelGroupBizlogic: RootJsonFormat[SimpleRelGroupBizlogic] = jsonFormat8(SimpleRelGroupBizlogic)

  implicit val formatLoginClass: RootJsonFormat[LoginClass] = jsonFormat2(LoginClass)

  implicit val formatChangePwdClass: RootJsonFormat[ChangePwdClass] = jsonFormat2(ChangePwdClass)

  implicit val formatSessionClass: RootJsonFormat[SessionClass] = jsonFormat4(SessionClass)

  //  implicit val formatPaginationClass: RootJsonFormat[PaginationClass] = jsonFormat2(PaginationClass)

  implicit val formatUserClassSeq: RootJsonFormat[SimpleUserSeq] = jsonFormat1(SimpleUserSeq)

  implicit val formatBizlogicClassSeq: RootJsonFormat[SimpleBizlogicSeq] = jsonFormat1(SimpleBizlogicSeq)

  implicit val formatDashboardClassSeq: RootJsonFormat[SimpleDashboardSeq] = jsonFormat1(SimpleDashboardSeq)

  implicit val formatGroupClassSeq: RootJsonFormat[SimpleGroupSeq] = jsonFormat1(SimpleGroupSeq)

  implicit val formatLibWidgetClassSeq: RootJsonFormat[SimpleLibWidgetSeq] = jsonFormat1(SimpleLibWidgetSeq)

  implicit val formatSourceClassSeq: RootJsonFormat[SimpleSourceSeq] = jsonFormat1(SimpleSourceSeq)

  implicit val formatSqlClassSeq: RootJsonFormat[SimpleSqlSeq] = jsonFormat1(SimpleSqlSeq)

  implicit val formatSqlLogClassSeq: RootJsonFormat[SimpleSqlLogSeq] = jsonFormat1(SimpleSqlLogSeq)

  implicit val formatWidgetClassSeq: RootJsonFormat[SimpleWidgetSeq] = jsonFormat1(SimpleWidgetSeq)

  implicit val formatRelUserGroupSeq: RootJsonFormat[SimpleRelUserGroupSeq] = jsonFormat1(SimpleRelUserGroupSeq)

  implicit val formatRelGroupBizlogicSeq: RootJsonFormat[SimpleRelGroupBizlogicSeq] = jsonFormat1(SimpleRelGroupBizlogicSeq)

  implicit val formatRelDashboardWidgetSeq: RootJsonFormat[SimpleRelDashboardWidgetSeq] = jsonFormat1(SimpleRelDashboardWidgetSeq)

  implicit val formatUserSeq: RootJsonFormat[UserSeq] = jsonFormat1(UserSeq)

  implicit val formatBizlogicSeq: RootJsonFormat[BizlogicSeq] = jsonFormat1(BizlogicSeq)

  implicit val formatDashboardSeq: RootJsonFormat[DashboardSeq] = jsonFormat1(DashboardSeq)

  implicit val formatGroupSeq: RootJsonFormat[GroupSeq] = jsonFormat1(GroupSeq)

  implicit val formatLibWidgetSeq: RootJsonFormat[LibWidgetSeq] = jsonFormat1(LibWidgetSeq)

  implicit val formatSourceSeq: RootJsonFormat[SourceSeq] = jsonFormat1(SourceSeq)

  implicit val formatSqlSeq: RootJsonFormat[SqlSeq] = jsonFormat1(SqlSeq)

  implicit val formatSqlLogSeq: RootJsonFormat[SqlLogSeq] = jsonFormat1(SqlLogSeq)

  implicit val formatWidgetSeq: RootJsonFormat[WidgetSeq] = jsonFormat1(WidgetSeq)

  implicit val formatResponseHeader: RootJsonFormat[ResponseHeader] = jsonFormat3(ResponseHeader)

  implicit val formatWidgetInfo: RootJsonFormat[WidgetInfo] = jsonFormat20(WidgetInfo)

  implicit val formatDashboardInfo: RootJsonFormat[DashboardInfo] = jsonFormat2(DashboardInfo)

  implicit val formatBaseInfo: RootJsonFormat[BaseInfo] = jsonFormat2(BaseInfo)

  implicit def formatRequestJson[A: JsonFormat]: RootJsonFormat[RequestJson[A]] = jsonFormat1(RequestJson.apply[A])

  implicit def formatRequestSeqJson[A: JsonFormat]: RootJsonFormat[RequestSeqJson[A]] = jsonFormat1(RequestSeqJson.apply[A])

  implicit def formatResponseJson[A: JsonFormat]: RootJsonFormat[ResponseJson[A]] = jsonFormat2(ResponseJson.apply[A])

  implicit def formatResponseSeqJson[A: JsonFormat]: RootJsonFormat[ResponseSeqJson[A]] = jsonFormat2(ResponseSeqJson.apply[A])


  //  implicit val formatResponseStatusClass = jsonFormat1(ResponseStatusClass)

  implicit object formatBaseEntity extends RootJsonFormat[BaseEntity] {

    def write(obj: BaseEntity): JsValue = obj match {
      case biz: Bizlogic => biz.toJson
      case dashboard: Dashboard => dashboard.toJson
      case group: Group => group.toJson
      case ligWidget: LibWidget => ligWidget.toJson
      case source: Source => source.toJson
      case sql: Sql => sql.toJson
      case sqlLog: SqlLog => sqlLog.toJson
      case user: User => user.toJson
      case widget: Widget => widget.toJson
      case relUserGroup: RelUserGroup => relUserGroup.toJson
      case relGroupBizlogic: RelGroupBizlogic => relGroupBizlogic.toJson
      case relDashboardWidget: RelDashboardWidget => relDashboardWidget.toJson
      case unknown@_ => serializationError(s"Marshalling issue with $unknown")
    }

    def read(value: JsValue): Nothing = {
      value match {
        case unknown@_ => deserializationError(s"Unmarshalling issue with $unknown ")
      }
    }
  }


}


