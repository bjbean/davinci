//package edp.davinci.rest
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModule}
//import edp.davinci.persistence.base.BaseEntity
//import edp.davinci.persistence.entities.JsonProtocol._
//import edp.davinci.persistence.entities.{Sql, SqlTable}
//import edp.davinci.util.Utils._
//import edp.davinci.util.{AuthorizationProvider, Utils}
//import io.swagger.annotations._
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//@Api(value = "/sqls", consumes = "application/json", produces = "application/json")
//@Path("/sqls")
//class SqlRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModule) extends Directives {
//
//  val routes = getSqlByIdRoute ~ getSqlByAllRoute ~ getSqlByPageRoute ~ postSqlRoute ~ putSqlRoute ~ deleteSqlByIdRoute ~ deleteSqlByAllRoute
//
//  @Path("/{id}")
//  @ApiOperation(value = "get one sql from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "sql id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 404, message = "sql not found"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSqlByIdRoute: Route = modules.sqlRoutes.getByIdRoute("sqls")
//
//  @ApiOperation(value = "get all sql with the same domain", notes = "", nickname = "", httpMethod = "GET")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSqlByAllRoute = modules.sqlRoutes.getByAllRoute("sqls", "domain_id")
//
//  @Path("{page=\\d+&size=\\d+}")
//  @ApiOperation(value = "get sqls with pagenifation", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getSqlByPageRoute = modules.sqlRoutes.pagenifationRoute("sqls")
//
//
//  @ApiOperation(value = "Add a new sql to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "sql", value = "Sql object to be added", required = true, dataType = "edp.davinci.rest.SqlClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def postSqlRoute = path("sqls") {
//    post {
//      entity(as[SqlClass]) {
//        baseclass =>
//          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//            session =>
//              Utils.responseHeaderWithToken(session) {
//                modules.sqlRoutes.postComplete(session, baseclass, modules.userDal.findByFilter(table => table.name === baseclass.name))
//              }
//          }
//      }
//    }
//  }
//
//  @Path("/{id}")
//  @ApiOperation(value = "update a sql in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "sql id", required = true, dataType = "integer", paramType = "path"),
//    new ApiImplicitParam(name = "sql", value = "Sql object to be updated", required = true, dataType = "edp.davinci.rest.SqlClass", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "sql not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def putSqlRoute = modules.sqlRoutes.putRoute("sqls")
//
//  @Path("/{id}")
//  @ApiOperation(value = "delete sql by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "sql id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteSqlByIdRoute = modules.sqlRoutes.deleteByIdRoute("sqls")
//
//  @ApiOperation(value = "delete all sqls with the same domain", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def deleteSqlByAllRoute = modules.sqlRoutes.deleteByAllRoute("sqls")
//}
