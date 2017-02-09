//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.base.BaseEntity
//import edp.davinci.persistence.entities.{Domain, DomainTable}
//import edp.davinci.persistence.entities.JsonProtocol._
//import edp.davinci.util.Utils._
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations._
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//@Api(value = "/domains", consumes = "application/json", produces = "application/json")
//@Path("/domains")
//class DomainRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//
//  val routes = getDomainByIdRoute ~ getDomainByAllRoute ~ postDomainRoute ~ putDomainRoute ~ deleteDomainByIdRoute ~ deleteDomainByAllRoute
//
//  @Path("/{id}")
//  @ApiOperation(value = "get one domain from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "domain id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 404, message = "domain not found"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getDomainByIdRoute: Route = modules.domainRoutes.getByIdRoute("domains")
//
//  @ApiOperation(value = "get all domain with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getDomainByAllRoute = modules.domainRoutes.getByAllRoute("domains", "id")
//
////  @Path("{page=\\d+&size=\\d+}")
////  @ApiOperation(value = "get domains with pagenifation", notes = "", nickname = "", httpMethod = "GET")
////  @ApiImplicitParams(Array(
////    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
////  ))
////  @ApiResponses(Array(
////    new ApiResponse(code = 200, message = "OK"),
////    new ApiResponse(code = 403, message = "user is not admin"),
////    new ApiResponse(code = 401, message = "authorization error"),
////    new ApiResponse(code = 500, message = "internal server error")
////  ))
////  def getDomainByPageRoute = modules.domainRoutes.pagenifationRoute(_.id === true)("domains")
//
//
//  @ApiOperation(value = "Add a new domain to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "domain", value = "Domain object to be added", required = true, dataType = "edp.davinci.rest.DomainClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def postDomainRoute = path("domains") {
//    post {
//      entity(as[DomainClass]) {
//        baseclass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                modules.domainRoutes.postComplete(session, baseclass, modules.userDal.findByFilter(table => table.name === baseclass.name))
//              }
//          }
//      }
//    }
//  }
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a domain in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "domain id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "domain", value = "Domain object to be updated", required = true, dataType = "edp.davinci.rest.DomainClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "domain not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def putDomainRoute = modules.domainRoutes.putRoute("domains")
//
//  @Path("/{id}")
//  @ApiOperation(value = "delete domain by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "domain id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteDomainByIdRoute = modules.domainRoutes.deleteByIdRoute("domains")
//
//  @ApiOperation(value = "delete all domains with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteDomainByAllRoute = path("domains") {
//    delete {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            if (session.admin)
//              onComplete(modules.domainDal.findByFilter(_.active === true).mapTo[Seq[Domain]]) {
//                case Success(seq) => onComplete(modules.domainDal.update(seq.map(base => modules.domainRoutes.generateEntity(base, session)).asInstanceOf[Seq[Domain]]).mapTo[Unit]) {
//                  case Success(unit) => complete(HttpResponse(OK, entity = "delete success"))
//                  case Failure(ex) => complete(InternalServerError, "internal server error")
//                }
//                case Failure(ex) => complete(InternalServerError, "internal server error")
//              }
//            else complete(Forbidden, "user is not admin")
//          }
//      }
//    }
//  }
//}
