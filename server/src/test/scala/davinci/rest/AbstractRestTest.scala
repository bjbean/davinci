//package edp.davinci.rest
//
//import com.typesafe.config.{Config, ConfigFactory}
//import edp.davinci.module.{ActorModule, ConfigurationModuleImpl, PersistenceModule}
//import edp.davinci.persistence.base.BaseDal
//import edp.davinci.persistence.entities.{Supplier, SuppliersTable}
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//import spray.testkit.Specs2RouteTest
//
//trait AbstractRestTest extends Specification with Specs2RouteTest with Mockito{
//
//  trait Modules extends ConfigurationModuleImpl with ActorModule with PersistenceModule {
//    val system = AbstractRestTest.this.system
//
//    override val suppliersDal = mock[BaseDal[SuppliersTable,Supplier]]
//
//    override def config = getConfig.withFallback(super.config)
//  }
//
//  def getConfig: Config = ConfigFactory.empty();
//
//
//}
