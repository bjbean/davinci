//package edp.davinci.persistence.base
//
//import edp.davinci.module.{ConfigurationModule, ConfigurationModuleImpl, DbModule, PersistenceModule}
//import edp.davinci.persistence.entities.{Supplier, SuppliersTable}
//import org.scalatest.Suite
//import slick.backend.DatabaseConfig
//import slick.driver.JdbcProfile
//import slick.lifted.TableQuery
//import spray.testkit.ScalatestRouteTest
//
//trait AbstractPersistenceTest extends ScalatestRouteTest{  this: Suite =>
//
//
//  trait Modules extends ConfigurationModuleImpl  with PersistenceModuleTest {
//  }
//
//
//  trait PersistenceModuleTest extends PersistenceModule with DbModule{
//    this: ConfigurationModule  =>
//
//    private val dbConfig : DatabaseConfig[JdbcProfile]  = DatabaseConfig.forConfig("h2test")
//
//    override implicit val profile: JdbcProfile = dbConfig.driver
//    override implicit val db: JdbcProfile#Backend#Database = dbConfig.db
//
//    override val suppliersDal = new BaseDalImpl[SuppliersTable,Supplier](TableQuery[SuppliersTable]) {}
//
//    val self = this
//
//  }
//
//}