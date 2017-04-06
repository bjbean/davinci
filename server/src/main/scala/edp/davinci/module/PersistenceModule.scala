package edp.davinci.module

import edp.davinci.persistence.base.{BaseDal, BaseDalImpl}
import edp.davinci.persistence.entities._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

trait DbModule {
  val profile: JdbcProfile
  val db: JdbcProfile#Backend#Database
}

trait PersistenceModule {
  // davinci

  // use an alternative database configuration ex:
   private val dbConfig : DatabaseConfig[JdbcProfile]  = DatabaseConfig.forConfig("mysqldb")
//  private val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("h2db")

  implicit val profile: JdbcProfile = dbConfig.profile
  implicit val db: JdbcProfile#Backend#Database = dbConfig.db

  val groupDal: BaseDal[GroupTable, UserGroup]
  val sqlLogDal: BaseDal[SqlLogTable, SqlLog]
  val sourceDal: BaseDal[SourceTable, Source]
  val userDal: BaseDal[UserTable, User]
  val relUserGroupDal: BaseDal[RelUserGroupTable, RelUserGroup]
  val dashboardDal: BaseDal[DashboardTable, Dashboard]
  val relDashboardWidgetDal: BaseDal[RelDashboardWidgetTable, RelDashboardWidget]
  val widgetDal: BaseDal[WidgetTable, Widget]
  val libWidgetDal: BaseDal[LibWidgetTable, LibWidget]
  val bizlogicDal: BaseDal[BizlogicTable, Bizlogic]
  val relGroupBizlogicDal: BaseDal[RelGroupBizlogicTable, RelGroupBizlogic]

  val groupQuery: TableQuery[GroupTable] = TableQuery[GroupTable]
  val sqlLogQuery = TableQuery[SqlLogTable]
  val sourceQuery = TableQuery[SourceTable]
  val userQuery = TableQuery[UserTable]
  val relUserGroupQuery = TableQuery[RelUserGroupTable]
  val dashboardQuery = TableQuery[DashboardTable]
  val relDashboardWidgetQuery = TableQuery[RelDashboardWidgetTable]
  val widgetQuery = TableQuery[WidgetTable]
  val libWidgetQuery = TableQuery[LibWidgetTable]
  val bizlogicQuery = TableQuery[BizlogicTable]
  val relGroupBizlogicQuery = TableQuery[RelGroupBizlogicTable]
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: ConfigurationModule =>

  // davinci
  override val groupDal = new BaseDalImpl[GroupTable, UserGroup](groupQuery)
  override val sqlLogDal = new BaseDalImpl[SqlLogTable, SqlLog](sqlLogQuery)
  override val sourceDal = new BaseDalImpl[SourceTable, Source](sourceQuery)
  override val userDal = new BaseDalImpl[UserTable, User](userQuery)
  override val relUserGroupDal = new BaseDalImpl[RelUserGroupTable, RelUserGroup](relUserGroupQuery)
  override val dashboardDal = new BaseDalImpl[DashboardTable, Dashboard](dashboardQuery)
  override val relDashboardWidgetDal = new BaseDalImpl[RelDashboardWidgetTable, RelDashboardWidget](relDashboardWidgetQuery)
  override val widgetDal = new BaseDalImpl[WidgetTable, Widget](widgetQuery)
  override val libWidgetDal = new BaseDalImpl[LibWidgetTable, LibWidget](libWidgetQuery)
  override val bizlogicDal = new BaseDalImpl[BizlogicTable, Bizlogic](bizlogicQuery)
  override val relGroupBizlogicDal = new BaseDalImpl[RelGroupBizlogicTable, RelGroupBizlogic](relGroupBizlogicQuery)

}
