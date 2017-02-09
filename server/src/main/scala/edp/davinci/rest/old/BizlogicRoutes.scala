//package edp.davinci.rest.old
//
//import javax.ws.rs.Path
//
//import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
//import akka.http.scaladsl.model.headers.RawHeader
//import akka.http.scaladsl.server.{Directives, Route}
//import edp.davinci.module.{ConfigurationModule, PersistenceModule}
//import edp.davinci.persistence.entities.Bizlogic
//import edp.davinci.persistence.entities.JsonProtocol._
//import edp.davinci.rest.SessionClass
//import edp.davinci.util.Utils._
//import edp.davinci.util.{AuthorizationProvider, JwtSupport}
//import io.swagger.annotations._
//import slick.jdbc.H2Profile.api._
//
//import scala.util.{Failure, Success}
//
//@Api(value = "/bizlogic", consumes = "application/json", produces = "application/json")
//@Path("/bizlogic")
//class BizlogicRoutes(modules: ConfigurationModule with PersistenceModule) extends Directives {
//
//  val routes: Route = getAllBizlogicRoute ~ getBizlogicByIdRoute ~ createBizlogicRoute ~ deleteBizlogicRoute
//
//  @ApiOperation(value = "list all bizlogics", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "bizlogics", value = "", required = false, dataType = "", paramType = "")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "bizlogics not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 500, message = "internal server error")
//  ))
//  def getAllBizlogicRoute: Route = path("bizlogic") {
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            onComplete(modules.bizlogic.
//              findByFilter(biz => biz.domain_id === session.domainId && biz.active && session.admin).mapTo[Seq[Bizlogic]]) {
//              case Success(bizlogicSeq) =>
//                if (bizlogicSeq.nonEmpty)
//                  complete(OK, bizlogicSeq.map(bizlogic => (bizlogic.id, bizlogic.name)))
//                else
//                  complete(NotFound, "bizlogics not found")
//              case Failure(ex) =>
//                complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//            }
//          }
//      }
//    }
//  }
//
//  @Path("/{bizlogicid}")
//  @ApiOperation(value = "get one bizlogic from system by id", notes = "", nickname = "", httpMethod = "GET")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "bizlogicid", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 404, message = "bizlogic not found"),
//    new ApiResponse(code = 401, message = "authentication failed"),
//    new ApiResponse(code = 500, message = "There was an internal server error")
//  ))
//  def getBizlogicByIdRoute: Route = path("bizlogic" / LongNumber) { bizlogicId =>
//    get {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          responseHeaderWithToken(session) {
//            onComplete(modules.bizlogic.findById(bizlogicId).mapTo[Option[Bizlogic]]) {
//              case Success(bizlogicOpt) => bizlogicOpt match {
//                case Some(bizlogic) =>
//                    complete(OK, bizlogic)
//                case None =>
//                    complete(NotFound, "bizlogic not found")
//              }
//              case Failure(ex) =>
//                respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                  complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//                }
//            }
//          }
//      }
//    }
//  }
//
//
//  @ApiOperation(value = "create a new bizlogic", nickname = "createBizlogic", httpMethod = "POST", consumes = "application/json")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "body", value = "Bizlogic object that needs to be created", required = true, dataType = "edp.davinci.persistence.entities.Bizlogic", paramType = "body")))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 405, message = "Invalid input"),
//    new ApiResponse(code = 500, message = "There was an internal server error")
//  ))
//  def createBizlogicRoute: Route = post {
//    path("bizlogic") {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          entity(as[Bizlogic]) {
//            bizlogic2Insert =>
//              if (session.admin)
//                onComplete(modules.bizlogic.insert(bizlogic2Insert)) {
//                  case Success(_) => respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                    complete(OK, "a new bizlogic is created")
//                  }
//                  case Failure(ex) => respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                    complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//                  }
//                }
//              else respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                complete(Forbidden, "permission denied to create")
//              }
//          }
//      }
//    }
//  }
//
//  @Path("{/bizlogicid}")
//  @ApiOperation(value = "Delete a bizlogic by id", nickname = "deleteBizlogic", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "bizlogicId", value = "Bizlogic id to delete", required = true, dataType = "integer", paramType = "path")))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "OK"),
//    new ApiResponse(code = 405, message = "Invalid input"),
//    new ApiResponse(code = 500, message = "There was an internal server error")
//  ))
//  def deleteBizlogicRoute: Route = path("/bizlogic" / LongNumber) { id =>
//    delete {
//      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
//        session =>
//          if (session.admin)
//            onComplete(modules.bizlogic.deleteById(id)) {
//              case Success(_) =>
//                respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                  complete(OK, "a  bizlogic is deleted")
//                }
//              case Failure(ex) => respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//                complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
//              }
//            }
//          else
//            respondWithHeader(RawHeader("token", JwtSupport.generateToken(session))) {
//              complete(Forbidden, "permission denied to delete")
//            }
//      }
//    }
//  }
//
//
//}
