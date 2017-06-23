package edp.davinci.rest.shareinfo

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.{PostSourceInfo, PutSourceInfo, Source}
import edp.davinci.rest._
import edp.davinci.util.{AuthorizationProvider, SqlProcessor}
import edp.davinci.util.JsonProtocol._
import edp.davinci.util.JsonUtils.caseClass2json
import edp.davinci.util.ResponseUtils.getHeader
import io.swagger.annotations._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


class ShareRoute(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with SqlProcessor {
  val routes: Route = getShareURLRoute
  private lazy val shareService = new ShareService(modules)
  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val routeName = "shares"

  @Path("/{widget_id}")
  @ApiOperation(value = "get the share url", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget_id", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getShareURLRoute: Route = path(routeName / LongNumber) { widgetId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session => getShareURLComplete(session, widgetId)

      }
    }
  }

  private def getShareURLComplete(session: SessionClass, widgetId: Long) = {
    onComplete(shareService.getWidgetById(widgetId)) {
      case Success(widgetInfo) =>
        //        val flatTableId = widgetInfo.flatTable_id
        //        val widgetJsonStr = caseClass2json(widgetInfo)
        //        onComplete(shareService.getSourceInfo(flatTableId,session)){
        //          case Success(sourceInfo)=>
        //            if (sourceInfo.nonEmpty) {
        //              try {
        //                val (sqlTemp, tableName, connectionUrl, _) = sourceInfo.head
        //                val sqlParam = sourceInfo.map(_._4)
        //               val sqls = sqlParam.map(param =>{
        //                  getSqlArr(sqlTemp,param,tableName,widgetInfo.adhoc_sql)
        //                })
        //
        //                complete(OK, ResponseJson(getHeader(200, session), ""))
        //              } catch {
        //                case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
        //              }
        //            }
        //            else
        complete(OK, ResponseJson[String](getHeader(200, "source info is empty", session), ""))
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
    }
  }





//  @ApiOperation(value = "Add new sources to the system", notes = "", nickname = "", httpMethod = "POST")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "sources", value = "Source objects to be added", required = true, dataType = "edp.davinci.rest.PostSourceInfoSeq", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "post success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 400, message = "bad request")
//  ))
//  def postSourceRoute: Route = path(routeName) {
//    post {
//      entity(as[PostSourceInfoSeq]) {
//        sourceSeq =>
//          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
//            session => postSource(session, sourceSeq.payload)
//          }
//      }
//    }
//  }
//
//
//  private def postSource(session: SessionClass, postSourceSeq: Seq[PostSourceInfo]): Route = {
//    if (session.admin) {
//      val sourceSeq = postSourceSeq.map(post => Source(0, post.name, post.connection_url, post.desc, post.`type`, post.config, active = true, null, session.userId, null, session.userId))
//      onComplete(modules.sourceDal.insert(sourceSeq)) {
//        case Success(sourceWithIdSeq) =>
//          val responseSourceSeq = sourceWithIdSeq.map(source => PutSourceInfo(source.id, source.name, source.connection_url, source.desc, source.`type`, source.config, Some(source.active)))
//          complete(OK, ResponseSeqJson[PutSourceInfo](getHeader(200, session), responseSourceSeq))
//        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
//      }
//    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
//  }
//
//
//  @ApiOperation(value = "update sources in the system", notes = "", nickname = "", httpMethod = "PUT")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "sources", value = "Source objects to be updated", required = true, dataType = "edp.davinci.rest.PutSourceInfoSeq", paramType = "body")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "put success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 404, message = "sources not found"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 400, message = "bad request")
//  ))
//  def putSourceRoute: Route = path(routeName) {
//    put {
//      entity(as[PutSourceInfoSeq]) {
//        sourceSeq =>
//          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
//            session => putSourceComplete(session, sourceSeq.payload)
//          }
//      }
//    }
//  }
//
//  private def putSourceComplete(session: SessionClass, sourceSeq: Seq[PutSourceInfo]): Route = {
//    if (session.admin) {
//      val future = sourceService.update(sourceSeq, session)
//      onComplete(future) {
//        case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
//        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
//      }
//    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
//  }
//
//
//  @Path("/{id}")
//  @ApiOperation(value = "delete source by id", notes = "", nickname = "", httpMethod = "DELETE")
//  @ApiImplicitParams(Array(
//    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path")
//  ))
//  @ApiResponses(Array(
//    new ApiResponse(code = 200, message = "delete success"),
//    new ApiResponse(code = 403, message = "user is not admin"),
//    new ApiResponse(code = 401, message = "authorization error"),
//    new ApiResponse(code = 400, message = "bad request")
//  ))
//  def deleteSourceByIdRoute: Route = modules.sourceRoutes.deleteByIdRoute(routeName)

}
