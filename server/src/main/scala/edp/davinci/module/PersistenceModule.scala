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
  val bizlogicDal: BaseDal[BizlogicTable, Bizlogic]
  val relGroupBizlogicDal: BaseDal[RelGroupBizlogicTable, RelGroupBizlogic]
}

trait PersistenceModuleImpl extends PersistenceModule with DbModule {
  this: ConfigurationModule =>

  private lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("mysqldb")
  println("before~~~~~~~~~~~~~~~~~~~~~~")
  override implicit lazy val profile: JdbcProfile = dbConfig.profile
  override implicit lazy val db: JdbcProfile#Backend#Database = dbConfig.db
  println("after~~~~~~~~~~~~~~~~~~~~~~~")

  override val groupDal = new BaseDalImpl[GroupTable, UserGroup](TableQuery[GroupTable])
  override val sqlLogDal = new BaseDalImpl[SqlLogTable, SqlLog](TableQuery[SqlLogTable])
  override val sourceDal = new BaseDalImpl[SourceTable, Source](TableQuery[SourceTable])
  override val userDal = new BaseDalImpl[UserTable, User](TableQuery[UserTable])
  override val relUserGroupDal = new BaseDalImpl[RelUserGroupTable, RelUserGroup](TableQuery[RelUserGroupTable])
  override val dashboardDal = new BaseDalImpl[DashboardTable, Dashboard](TableQuery[DashboardTable])
  override val relDashboardWidgetDal = new BaseDalImpl[RelDashboardWidgetTable, RelDashboardWidget](TableQuery[RelDashboardWidgetTable])
  override val widgetDal = new BaseDalImpl[WidgetTable, Widget](TableQuery[WidgetTable])
  override val libWidgetDal = new BaseDalImpl[LibWidgetTable, LibWidget](TableQuery[LibWidgetTable])
  override val bizlogicDal = new BaseDalImpl[BizlogicTable, Bizlogic](TableQuery[BizlogicTable])
  override val relGroupBizlogicDal = new BaseDalImpl[RelGroupBizlogicTable, RelGroupBizlogic](TableQuery[RelGroupBizlogicTable])

}
