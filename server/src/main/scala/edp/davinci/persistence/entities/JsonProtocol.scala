package edp.davinci.persistence.entities

import java.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import edp.davinci.persistence.base.BaseEntity
import edp.davinci.rest._
import spray.json._

import scala.collection.immutable.Map
import scala.collection.mutable.Map

object JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  // davinci
  implicit val formatGroup = jsonFormat9(Group)
  implicit val formatSimpleGroup = jsonFormat8(SimpleGroup)

  implicit val formatSql = jsonFormat13(Sql)
  implicit val formatSimpleSql = jsonFormat12(SimpleSql)

  implicit val formatSqlLog = jsonFormat8(SqlLog)
  implicit val formatSimpleSqlLog = jsonFormat7(SimpleSqlLog)

  implicit val formatTablePrivilege = jsonFormat12(Source)
  implicit val formatSimpleTablePrivilege = jsonFormat11(SimpleSource)

  implicit val formatUser = jsonFormat12(User)
  implicit val formatSimpleUser = jsonFormat11(SimpleUser)

  implicit val formatRelUserGroup = jsonFormat8(RelUserGroup)
  implicit val formatRelSimpleUserGroup = jsonFormat7(SimpleRelUserGroup)

  implicit val formatDashboard = jsonFormat10(Dashboard)
  implicit val formatSimpleDashboard = jsonFormat9(SimpleDashboard)

  implicit val formatRelDashboardWidget = jsonFormat12(RelDashboardWidget)
  implicit val formatSimpleRelDashboardWidget = jsonFormat11(SimpleRelDashboardWidget)

  implicit val formatWidget = jsonFormat14(Widget)
  implicit val formatSimpleWidget = jsonFormat13(SimpleWidget)

  implicit val formatLibWidget = jsonFormat7(LibWidget)
  implicit val formatSimpleLibWidget = jsonFormat6(SimpleLibWidget)

  implicit val formatDomain = jsonFormat8(Domain)
  implicit val formatSimpleDomain = jsonFormat7(SimpleDomain)

  implicit val formatBizlogic = jsonFormat10(Bizlogic)
  implicit val formatSimpleBizlogic = jsonFormat9(SimpleBizlogic)

  implicit val formatRelGroupBizlogic = jsonFormat9(RelGroupBizlogic)
  implicit val formatSimpleRelGroupBizlogic = jsonFormat8(SimpleRelGroupBizlogic)

  implicit val formatLoginClass = jsonFormat2(LoginClass)

  implicit val formatChangePwdClass = jsonFormat2(ChangePwdClass)

  implicit val formatSessionClass = jsonFormat5(SessionClass)

  implicit val formatPaginationClass = jsonFormat2(PaginationClass)

  implicit val formatBizlogicClass = jsonFormat3(BizlogicClass)

  implicit val formatDashboardClass = jsonFormat3(DashboardClass)

  implicit val formatDomainClass = jsonFormat2(DomainClass)

  implicit val formatLibWidgetClass = jsonFormat1(LibWidgetClass)

  implicit val formatSourceClass = jsonFormat5(SourceClass)

  implicit val formatSqlClass = jsonFormat6(SqlClass)

  implicit val formatSqlLogClass = jsonFormat6(SqlLogClass)

  implicit val formatWidgetClass = jsonFormat7(WidgetClass)

  implicit val formatUserClass = jsonFormat3(UserClass)

  implicit val formatPostGroupClass = jsonFormat2(GroupClass)

  implicit val formatUserClassSeq = jsonFormat1(UserClassSeq)

  implicit val formatBizlogicClassSeq = jsonFormat1(BizlogicClassSeq)

  implicit val formatDashboardClassSeq = jsonFormat1(DashboardClassSeq)

  implicit val formatGroupClassSeq = jsonFormat1(GroupClassSeq)

  implicit val formatLibWidgetClassSeq = jsonFormat1(LibWidgetClassSeq)

  implicit val formatDomainClassSeq = jsonFormat1(DomainClassSeq)

  implicit val formatSourceClassSeq = jsonFormat1(SourceClassSeq)

  implicit val formatSqlClassSeq = jsonFormat1(SqlClassSeq)

  implicit val formatSqlLogClassSeq = jsonFormat1(SqlLogClassSeq)

  implicit val formatWidgetClassSeq = jsonFormat1(WidgetClassSeq)

  implicit val formatUserSeq = jsonFormat1(UserSeq)

  implicit val formatBizlogicSeq = jsonFormat1(BizlogicSeq)

  implicit val formatDashboardSeq = jsonFormat1(DashboardSeq)

  implicit val formatGroupSeq = jsonFormat1(GroupSeq)

  implicit val formatLibWidgetSeq = jsonFormat1(LibWidgetSeq)

  implicit val formatDomainSeq = jsonFormat1(DomainSeq)

  implicit val formatSourceSeq = jsonFormat1(SourceSeq)

  implicit val formatSqlSeq = jsonFormat1(SqlSeq)

  implicit val formatSqlLogSeq = jsonFormat1(SqlLogSeq)

  implicit val formatWidgetSeq = jsonFormat1(WidgetSeq)

  //  implicit val formatUserJson = jsonFormat1(UserJson)
  //  implicit val formatUserJsonSeq = jsonFormat1(UserJsonSeq)

  implicit def formatRequestJson[A: JsonFormat] = jsonFormat1(RequestJson.apply[A])

  implicit def formatRequestSeqJson[A: JsonFormat] = jsonFormat1(RequestSeqJson.apply[A])

  implicit val formatResponseHeader = jsonFormat3(ResponseHeader)

  implicit def formatResponseJson[A: JsonFormat] = jsonFormat2(ResponseJson.apply[A])


  //  implicit val formatResponseStatusClass = jsonFormat1(ResponseStatusClass)

  implicit object formatBaseEntity extends RootJsonFormat[BaseEntity] {

    def write(obj: BaseEntity) = obj match {
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
      case unknown@_ => serializationError(s"Marshalling issue with ${unknown}")
    }

    def read(value: JsValue) = {
      value match {
        case unknown@_ => deserializationError(s"Unmarshalling issue with ${unknown} ")
      }
    }
  }

  implicit object formatBaseClass extends RootJsonFormat[BaseClass] {
    def write(obj: BaseClass) = obj match {
      case user: UserClass => user.toJson
      case unknown@_ => serializationError(s"Marshalling issue with ${unknown}")
    }

    def read(value: JsValue) = {
      value match {
        case unknown@_ => deserializationError(s"Unmarshalling issue with ${unknown} ")
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


