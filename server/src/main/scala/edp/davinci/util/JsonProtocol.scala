package edp.davinci.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import edp.davinci.persistence.base.BaseEntity
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import spray.json._

object JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  // davinci
  implicit val formatGroup: RootJsonFormat[Group] = jsonFormat9(Group)
  implicit val formatSimpleGroup: RootJsonFormat[SimpleGroup] = jsonFormat8(SimpleGroup)

  implicit val formatSql: RootJsonFormat[Sql] = jsonFormat13(Sql)
  implicit val formatSimpleSql: RootJsonFormat[SimpleSql] = jsonFormat12(SimpleSql)

  implicit val formatSqlLog: RootJsonFormat[SqlLog] = jsonFormat8(SqlLog)
  implicit val formatSimpleSqlLog: RootJsonFormat[SimpleSqlLog] = jsonFormat7(SimpleSqlLog)

  implicit val formatTablePrivilege: RootJsonFormat[Source] = jsonFormat12(Source)
  implicit val formatSimpleTablePrivilege: RootJsonFormat[SimpleSource] = jsonFormat11(SimpleSource)

  implicit val formatUser: RootJsonFormat[User] = jsonFormat12(User)
  implicit val formatSimpleUser: RootJsonFormat[SimpleUser] = jsonFormat11(SimpleUser)

  implicit val formatRelUserGroup: RootJsonFormat[RelUserGroup] = jsonFormat8(RelUserGroup)
  implicit val formatRelSimpleUserGroup: RootJsonFormat[SimpleRelUserGroup] = jsonFormat7(SimpleRelUserGroup)

  implicit val formatDashboard: RootJsonFormat[Dashboard] = jsonFormat10(Dashboard)
  implicit val formatSimpleDashboard: RootJsonFormat[SimpleDashboard] = jsonFormat9(SimpleDashboard)

  implicit val formatRelDashboardWidget: RootJsonFormat[RelDashboardWidget] = jsonFormat12(RelDashboardWidget)
  implicit val formatSimpleRelDashboardWidget: RootJsonFormat[SimpleRelDashboardWidget] = jsonFormat11(SimpleRelDashboardWidget)

  implicit val formatWidget: RootJsonFormat[Widget] = jsonFormat14(Widget)
  implicit val formatSimpleWidget: RootJsonFormat[SimpleWidget] = jsonFormat13(SimpleWidget)

  implicit val formatLibWidget: RootJsonFormat[LibWidget] = jsonFormat7(LibWidget)
  implicit val formatSimpleLibWidget: RootJsonFormat[SimpleLibWidget] = jsonFormat6(SimpleLibWidget)

  implicit val formatDomain: RootJsonFormat[Domain] = jsonFormat8(Domain)
  implicit val formatSimpleDomain: RootJsonFormat[SimpleDomain] = jsonFormat7(SimpleDomain)

  implicit val formatBizlogic: RootJsonFormat[Bizlogic] = jsonFormat10(Bizlogic)
  implicit val formatSimpleBizlogic: RootJsonFormat[SimpleBizlogic] = jsonFormat9(SimpleBizlogic)

  implicit val formatRelGroupBizlogic: RootJsonFormat[RelGroupBizlogic] = jsonFormat9(RelGroupBizlogic)
  implicit val formatSimpleRelGroupBizlogic: RootJsonFormat[SimpleRelGroupBizlogic] = jsonFormat8(SimpleRelGroupBizlogic)

  implicit val formatLoginClass: RootJsonFormat[LoginClass] = jsonFormat2(LoginClass)

  implicit val formatChangePwdClass: RootJsonFormat[ChangePwdClass] = jsonFormat2(ChangePwdClass)

  implicit val formatSessionClass: RootJsonFormat[SessionClass] = jsonFormat5(SessionClass)

  implicit val formatPaginationClass: RootJsonFormat[PaginationClass] = jsonFormat2(PaginationClass)

  implicit val formatBizlogicClass: RootJsonFormat[BizlogicClass] = jsonFormat3(BizlogicClass)

  implicit val formatDashboardClass: RootJsonFormat[DashboardClass] = jsonFormat3(DashboardClass)

  implicit val formatDomainClass: RootJsonFormat[DomainClass] = jsonFormat2(DomainClass)

  implicit val formatLibWidgetClass: RootJsonFormat[LibWidgetClass] = jsonFormat1(LibWidgetClass)

  implicit val formatSourceClass: RootJsonFormat[SourceClass] = jsonFormat5(SourceClass)

  implicit val formatSqlClass: RootJsonFormat[SqlClass] = jsonFormat6(SqlClass)

  implicit val formatSqlLogClass: RootJsonFormat[SqlLogClass] = jsonFormat6(SqlLogClass)

  implicit val formatWidgetClass: RootJsonFormat[WidgetClass] = jsonFormat7(WidgetClass)

  implicit val formatUserClass: RootJsonFormat[UserClass] = jsonFormat3(UserClass)

  implicit val formatPostGroupClass: RootJsonFormat[GroupClass] = jsonFormat2(GroupClass)

  implicit val formatUserClassSeq: RootJsonFormat[UserClassSeq] = jsonFormat1(UserClassSeq)

  implicit val formatBizlogicClassSeq: RootJsonFormat[BizlogicClassSeq] = jsonFormat1(BizlogicClassSeq)

  implicit val formatDashboardClassSeq: RootJsonFormat[DashboardClassSeq] = jsonFormat1(DashboardClassSeq)

  implicit val formatGroupClassSeq: RootJsonFormat[GroupClassSeq] = jsonFormat1(GroupClassSeq)

  implicit val formatLibWidgetClassSeq: RootJsonFormat[LibWidgetClassSeq] = jsonFormat1(LibWidgetClassSeq)

  implicit val formatDomainClassSeq: RootJsonFormat[DomainClassSeq] = jsonFormat1(DomainClassSeq)

  implicit val formatSourceClassSeq: RootJsonFormat[SourceClassSeq] = jsonFormat1(SourceClassSeq)

  implicit val formatSqlClassSeq: RootJsonFormat[SqlClassSeq] = jsonFormat1(SqlClassSeq)

  implicit val formatSqlLogClassSeq: RootJsonFormat[SqlLogClassSeq] = jsonFormat1(SqlLogClassSeq)

  implicit val formatWidgetClassSeq: RootJsonFormat[WidgetClassSeq] = jsonFormat1(WidgetClassSeq)

  implicit val formatUserSeq: RootJsonFormat[UserSeq] = jsonFormat1(UserSeq)

  implicit val formatBizlogicSeq: RootJsonFormat[BizlogicSeq] = jsonFormat1(BizlogicSeq)

  implicit val formatDashboardSeq: RootJsonFormat[DashboardSeq] = jsonFormat1(DashboardSeq)

  implicit val formatGroupSeq: RootJsonFormat[GroupSeq] = jsonFormat1(GroupSeq)

  implicit val formatLibWidgetSeq: RootJsonFormat[LibWidgetSeq] = jsonFormat1(LibWidgetSeq)

  implicit val formatDomainSeq: RootJsonFormat[DomainSeq] = jsonFormat1(DomainSeq)

  implicit val formatSourceSeq: RootJsonFormat[SourceSeq] = jsonFormat1(SourceSeq)

  implicit val formatSqlSeq: RootJsonFormat[SqlSeq] = jsonFormat1(SqlSeq)

  implicit val formatSqlLogSeq: RootJsonFormat[SqlLogSeq] = jsonFormat1(SqlLogSeq)

  implicit val formatWidgetSeq: RootJsonFormat[WidgetSeq] = jsonFormat1(WidgetSeq)

  //  implicit val formatUserJson = jsonFormat1(UserJson)
  //  implicit val formatUserJsonSeq = jsonFormat1(UserJsonSeq)

  implicit def formatRequestJson[A: JsonFormat]: RootJsonFormat[RequestJson[A]] = jsonFormat1(RequestJson.apply[A])

  implicit def formatRequestSeqJson[A: JsonFormat]: RootJsonFormat[RequestSeqJson[A]] = jsonFormat1(RequestSeqJson.apply[A])

  implicit val formatResponseHeader: RootJsonFormat[ResponseHeader] = jsonFormat3(ResponseHeader)

  implicit def formatResponseJson[A: JsonFormat]: RootJsonFormat[ResponseJson[A]] = jsonFormat2(ResponseJson.apply[A])


  //  implicit val formatResponseStatusClass = jsonFormat1(ResponseStatusClass)

  implicit object formatBaseEntity extends RootJsonFormat[BaseEntity] {

    def write(obj: BaseEntity): JsValue = obj match {
      case biz: Bizlogic => biz.toJson
      case dashboard: Dashboard => dashboard.toJson
      case group: Group => group.toJson
      case domain: Domain => domain.toJson
      case ligWidget: LibWidget => ligWidget.toJson
      case source: Source => source.toJson
      case sql: Sql => sql.toJson
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

  implicit object formatBaseClass extends RootJsonFormat[BaseClass] {
    def write(obj: BaseClass): JsValue = obj match {
      case user: UserClass => user.toJson
      case unknown@_ => serializationError(s"Marshalling issue with $unknown")
    }

    def read(value: JsValue): Nothing = {
      value match {
        case unknown@_ => deserializationError(s"Unmarshalling issue with $unknown ")
      }
    }
  }

  //  private def typeInfer(list: List[String], map: scala.Predef.Map[scala.Predef.String, spray.json.JsValue]): Boolean = {
  //    var flag = true
  //    for (field <- list) {
  //      if (flag && map.contains(field)) flag = true
  //      else flag = false
  //    }
  //    flag
  //  }

}


