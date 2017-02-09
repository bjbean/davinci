//package edp.davinci.rest
//
//import edp.davinci.persistence.entities.JsonProtocol._
//import edp.davinci.persistence.entities.{SimpleSupplier, Supplier}
//import spray.http.StatusCodes._
//import spray.httpx.SprayJsonSupport._
//
//import scala.concurrent.Future
//
//class RoutesSpec extends AbstractRestTest {
//  sequential
//
//  def actorRefFactory = system
//
//  val modules = new Modules {}
//
//  val suppliers = new SupplierHttpService(modules) {
//    override def actorRefFactory = system
//  }
//
//  "Supplier Routes" should {
//
//    "return an empty array of suppliers" in {
//      modules.suppliersDal.findById(1) returns Future(None)
//
//      Get("/supplier/1") ~> suppliers.SupplierGetRoute ~> check {
//        handled must beTrue
//        status mustEqual NotFound
//      }
//    }
//
//    "return an array with 2 suppliers" in {
//      modules.suppliersDal.findById(1) returns Future(Some(Supplier(1, "name 1", "desc 1")))
//      Get("/supplier/1") ~> suppliers.SupplierGetRoute ~> check {
//        handled must beTrue
//        status mustEqual OK
//        responseAs[Option[Supplier]].isEmpty mustEqual false
//      }
//    }
//
//    "create a supplier with the json in post" in {
//      modules.suppliersDal.insert(Supplier(0, "name 1", "desc 1")) returns Future(1)
//      Post("/supplier", SimpleSupplier("name 1", "desc 1")) ~> suppliers.SupplierPostRoute ~> check {
//        handled must beTrue
//        status mustEqual Created
//      }
//    }
//
//    "not handle the invalid json" in {
//      Post("/supplier", "{\"name\":\"1\"}") ~> suppliers.SupplierPostRoute ~> check {
//        handled must beFalse
//      }
//    }
//
//    "not handle an empty post" in {
//      Post("/supplier") ~> suppliers.SupplierPostRoute ~> check {
//        handled must beFalse
//      }
//    }
//  }
//}
