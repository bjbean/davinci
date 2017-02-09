package edp.davinci.module

import edp.davinci.Boot
import edp.davinci.persistence.entities._
import edp.davinci.rest.{BaseRoutes, BaseRoutesImpl}


/*trait RoutesModule {
  // davinci
  val groupRoutes: BaseRoutes[GroupTable, Group]
  val sqlRoutes: BaseRoutes[SqlTable, Sql]
  val sqlLogRoutes: BaseRoutes[SqlLogTable, SqlLog]
  val sourceRoutes: BaseRoutes[SourceTable, Source]
  val userRoutes: BaseRoutes[UserTable, User]
  val relUserGroupRoutes: BaseRoutes[RelUserGroupTable, RelUserGroup]
  val dashboardRoutes: BaseRoutes[DashboardTable, Dashboard]
  val relDashboardWidgetRoutes: BaseRoutes[RelDashboardWidgetTable, RelDashboardWidget]
  val widgetRoutes: BaseRoutes[WidgetTable, Widget]
  val libWidgetRoutes: BaseRoutes[LibWidgetTable, LibWidget]
  val domainRoutes: BaseRoutes[DomainTable, Domain]
  val bizlogicRoutes: BaseRoutes[BizlogicTable, Bizlogic]
  val relGroupBizlogicRoutes: BaseRoutes[RelGroupBizlogicTable, RelGroupBizlogic]
}

trait RoutesModuleImpl extends RoutesModule {
  this: ConfigurationModule with PersistenceModule =>

  // davinci
  override val groupRoutes = new BaseRoutesImpl[GroupTable, Group](groupDal)
  override val sqlRoutes = new BaseRoutesImpl[SqlTable, Sql](sqlDal)
  override val sqlLogRoutes = new BaseRoutesImpl[SqlLogTable, SqlLog](sqlLogDal)
  override val sourceRoutes = new BaseRoutesImpl[SourceTable, Source](sourceDal)
  override val userRoutes = new BaseRoutesImpl[UserTable, User](userDal)
  override val relUserGroupRoutes = new BaseRoutesImpl[RelUserGroupTable, RelUserGroup](relUserGroupDal)
  override val dashboardRoutes = new BaseRoutesImpl[DashboardTable, Dashboard](dashboardDal)
  override val relDashboardWidgetRoutes = new BaseRoutesImpl[RelDashboardWidgetTable, RelDashboardWidget](relDashboardWidgetDal)
  override val widgetRoutes = new BaseRoutesImpl[WidgetTable, Widget](widgetDal)
  override val libWidgetRoutes = new BaseRoutesImpl[LibWidgetTable, LibWidget](libWidgetDal)
  override val domainRoutes = new BaseRoutesImpl[DomainTable, Domain](domainDal)
  override val bizlogicRoutes = new BaseRoutesImpl[BizlogicTable, Bizlogic](bizlogicDal)
  override val relGroupBizlogicRoutes = new BaseRoutesImpl[RelGroupBizlogicTable, RelGroupBizlogic](relGroupBizlogicDal)

}*/



trait RoutesModuleImpl {
  this: ConfigurationModule with PersistenceModule =>
  // davinci
   val groupRoutes = new BaseRoutesImpl[GroupTable, Group](groupDal)
   val sqlRoutes = new BaseRoutesImpl[SqlTable, Sql](sqlDal)
   val sqlLogRoutes = new BaseRoutesImpl[SqlLogTable, SqlLog](sqlLogDal)
   val sourceRoutes = new BaseRoutesImpl[SourceTable, Source](sourceDal)
   val userRoutes = new BaseRoutesImpl[UserTable, User](userDal)
   val relUserGroupRoutes = new BaseRoutesImpl[RelUserGroupTable, RelUserGroup](relUserGroupDal)
   val dashboardRoutes = new BaseRoutesImpl[DashboardTable, Dashboard](dashboardDal)
   val relDashboardWidgetRoutes = new BaseRoutesImpl[RelDashboardWidgetTable, RelDashboardWidget](relDashboardWidgetDal)
   val widgetRoutes = new BaseRoutesImpl[WidgetTable, Widget](widgetDal)
   val libWidgetRoutes = new BaseRoutesImpl[LibWidgetTable, LibWidget](libWidgetDal)
   val domainRoutes = new BaseRoutesImpl[DomainTable, Domain](domainDal)
   val bizlogicRoutes = new BaseRoutesImpl[BizlogicTable, Bizlogic](bizlogicDal)
   val relGroupBizlogicRoutes = new BaseRoutesImpl[RelGroupBizlogicTable, RelGroupBizlogic](relGroupBizlogicDal)

}

