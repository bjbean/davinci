package edp.davinci.module

import edp.davinci.persistence.entities._
import edp.davinci.rest.BaseRoutesImpl


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
   val bizlogicRoutes = new BaseRoutesImpl[BizlogicTable, Bizlogic](bizlogicDal)
   val relGroupBizlogicRoutes = new BaseRoutesImpl[RelGroupBizlogicTable, RelGroupBizlogic](relGroupBizlogicDal)

}

