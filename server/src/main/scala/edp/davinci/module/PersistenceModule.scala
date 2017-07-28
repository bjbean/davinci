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
  val groupDal: BaseDal[GroupTable, UserGroup]
  val sqlLogDal: BaseDal[SqlLogTable, SqlLog]
  val sourceDal: BaseDal[SourceTable, Source]
  val userDal: BaseDal[UserTable, User]
  val relUserGroupDal: BaseDal[RelUserGroupTable, RelUserGroup]
  val dashboardDal: BaseDal[DashboardTable, Dashboard]
  val relDashboardWidgetDal: BaseDal[RelDashboardWidgetTable, RelDashboardWidget]
  val widgetDal: BaseDal[WidgetTable, Widget]
  val libWidgetDal: BaseDal[LibWidgetTable, LibWidget]
  val flatTableDal: BaseDal[FlatTbl, FlatTable]
  val relGroupFlatTableDal: BaseDal[RelGroupFlatTblTable, RelGroupFlatTable]
  val shareInfoDal: BaseDal[ShareInfoTable, ShareInfo]
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: ConfigurationModule =>

  private lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("mysqldb")
  override implicit lazy val profile: JdbcProfile = dbConfig.profile
  override implicit lazy val db: JdbcProfile#Backend#Database = dbConfig.db

  override lazy val groupDal = new BaseDalImpl[GroupTable, UserGroup](TableQuery[GroupTable])
  override lazy val sqlLogDal = new BaseDalImpl[SqlLogTable, SqlLog](TableQuery[SqlLogTable])
  override lazy val sourceDal = new BaseDalImpl[SourceTable, Source](TableQuery[SourceTable])
  override lazy val userDal = new BaseDalImpl[UserTable, User](TableQuery[UserTable])
  override lazy val relUserGroupDal = new BaseDalImpl[RelUserGroupTable, RelUserGroup](TableQuery[RelUserGroupTable])
  override lazy val dashboardDal = new BaseDalImpl[DashboardTable, Dashboard](TableQuery[DashboardTable])
  override lazy val relDashboardWidgetDal = new BaseDalImpl[RelDashboardWidgetTable, RelDashboardWidget](TableQuery[RelDashboardWidgetTable])
  override lazy val widgetDal = new BaseDalImpl[WidgetTable, Widget](TableQuery[WidgetTable])
  override lazy val libWidgetDal = new BaseDalImpl[LibWidgetTable, LibWidget](TableQuery[LibWidgetTable])
  override lazy val flatTableDal = new BaseDalImpl[FlatTbl, FlatTable](TableQuery[FlatTbl])
  override lazy val relGroupFlatTableDal = new BaseDalImpl[RelGroupFlatTblTable, RelGroupFlatTable](TableQuery[RelGroupFlatTblTable])
  override lazy val shareInfoDal = new BaseDalImpl[ShareInfoTable, ShareInfo](TableQuery[ShareInfoTable])

}
