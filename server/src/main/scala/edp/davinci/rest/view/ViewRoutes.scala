package edp.davinci.rest.view

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.DavinciConstants
import edp.davinci.module.{ConfigurationModule, PersistenceModule, _}
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.JsonProtocol._
import edp.davinci.util.ResponseUtils._
import io.swagger.annotations._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Api(value = "/flattables", consumes = "application/json", produces = "application/json")
@Path("/flattables")
class ViewRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = postViewRoute ~ putViewRoute ~ getViewByAllRoute ~ deleteViewByIdRoute ~ getGroupsByViewIdRoute ~ getCalculationResRoute ~ deleteRelGFById
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val adHocTable = "table"
  private lazy val routeName = "flattables"


  @ApiOperation(value = "get all views", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 404, message = "not found")
  ))
  def getViewByAllRoute: Route = path(routeName) {
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          if (session.admin) {
            onComplete(ViewService.getAllViews) {
              case Success(viewSeq) =>
                val queryResult = viewSeq.map(biz => PutViewInfo(Some(biz._1), biz._2, biz._3, biz._4, biz._5.getOrElse(""), biz._6, biz._7, biz._8))
                complete(OK, ResponseSeqJson[PutViewInfo](getHeader(200, session), queryResult))
              case Failure(ex) =>
                logger.error(" get views exception", ex)
                complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
            }
          } else complete(Forbidden, ResponseJson[String](getHeader(403, "user is not admin", session), ""))
      }
    }
  }


  @ApiOperation(value = "Add new view to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "flattables", value = "FlatTable objects to be added", required = true, dataType = "edp.davinci.rest.PutViewInfoSeq", paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 405, message = "unspecified error")
  ))
  def postViewRoute: Route = path(routeName) {
    post {
      entity(as[PutViewInfoSeq]) { putViewSeq =>
        authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) { session =>
          val viewSeq = putViewSeq.payload
          if (session.admin) {
            val uniqueTableName = adHocTable + java.util.UUID.randomUUID().toString
            val bizEntitySeq = viewSeq.map(biz => ViewTable(0, biz.source_id, biz.name, biz.sql_tmpl, uniqueTableName, Some(biz.desc), biz.trigger_type, biz.frequency, biz.`catch`, active = true, null, session.userId, null, session.userId))
            onComplete(modules.viewDal.insert(bizEntitySeq)) {
              case Success(bizSeq) =>
                val queryBiz = bizSeq.map(biz => PutViewInfo(Some(biz.id), biz.source_id, biz.name, biz.sql_tmpl, biz.desc.getOrElse(""), biz.trigger_type, biz.frequency, biz.`catch`, Some(biz.result_table)))
                val relSeq = for {biz <- bizSeq
                                  rel <- viewSeq.head.relBG.get
                } yield RelGroupView(0, rel.group_id, biz.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
                onComplete(modules.relGroupViewDal.insert(relSeq)) {
                  case Success(_) => complete(OK, ResponseSeqJson[PutViewInfo](getHeader(200, session), queryBiz))
                  case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
                }
              case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
            }
          } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
        }
      }
    }
  }

  @ApiOperation(value = "update views in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "flatTable", value = "FlatTable objects to be updated", required = true, dataType = "edp.davinci.rest.PutFlatTableInfoSeq", paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 405, message = "put flatTable error")
  ))
  def putViewRoute: Route = path(routeName) {
    put {
      entity(as[PutViewInfoSeq]) { putViewSeq =>
        authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) { session =>
          val viewSeq = putViewSeq.payload
          val operation = for {
            updateOP <- ViewService.updateFlatTbl(viewSeq, session)
            deleteOp <- ViewService.deleteByViewId(viewSeq.map(_.id.get))
          } yield (updateOP, deleteOp)
          onComplete(operation) {
            case Success(_) => val relSeq = for {rel <- viewSeq.head.relBG.get
            } yield RelGroupView(0, rel.group_id, viewSeq.head.id.get, rel.sql_params, active = true, null, session.userId, null, session.userId)
              onComplete(modules.relGroupViewDal.insert(relSeq)) {
                case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
                case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
              }
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete view by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "id", value = "flat table id", required = true, dataType = "integer", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteViewByIdRoute: Route = path(routeName / LongNumber) { flatTableId =>
    delete {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          if (session.admin) {
            val operation = for {
              deleteFlatTable <- ViewService.deleteByViewId(Seq(flatTableId))
              deleteRel <- ViewService.deleteRelId(flatTableId)
              updateWidget <- ViewService.updateWidget(flatTableId)
            } yield (deleteFlatTable, deleteRel, updateWidget)
            onComplete(operation) {
              case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
              case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
            }
          } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
      }
    }
  }

  @Path("/groups/{rel_id}")
  @ApiOperation(value = "delete view from group by rel id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "rel_id", value = "rel_id", required = true, dataType = "integer", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteRelGFById: Route = path(routeName / "groups" / LongNumber) { relId =>
    delete {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          if (session.admin) {
            onComplete(modules.relGroupViewDal.deleteById(relId).mapTo[Int]) {
              case Success(r) => complete(OK, ResponseJson[Int](getHeader(200, session), r))
              case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
            }
          } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
      }
    }
  }

  @Path("/{id}/groups")
  @ApiOperation(value = "get groups by view id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "id", value = "flat table id", required = true, dataType = "integer", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 405, message = "internal get error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getGroupsByViewIdRoute: Route = path(routeName / LongNumber / "groups") { viewId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          val future = ViewService.getGroups(viewId)
          onComplete(future) {
            case Success(relSeq) =>
              val putRelSeq = relSeq.map(r => PutRelGroupView(Some(r._1), r._2, r._3))
              complete(OK, ResponseSeqJson[PutRelGroupView](getHeader(200, session), putRelSeq))
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
      }
    }
  }


  @Path("/{id}/resultset")
  @ApiOperation(value = "get calculation results by biz id", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "flattable id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "manualInfo", value = "manualInfo", required = false, dataType = "edp.davinci.rest.ManualInfo", paramType = "body"),
    new ApiImplicitParam(name = "offset", value = "offset", required = false, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "limit", value = "limit", required = false, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "sortby", value = "sort by", required = false, dataType = "string", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getCalculationResRoute: Route = path(routeName / LongNumber / "resultset") { viewId =>
    post {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          entity(as[ManualInfo]) { manualInfo =>
            parameters('offset.as[Int] ? 0, 'limit.as[Int] ? 1000, 'sortby.as[String] ? "") { (offset, limit, sortBy) =>
              val paginationInfo = s" limit $limit offset $offset"
              val sortInfo = if (sortBy != "") "ORDER BY " + sortBy.map(ch => if (ch == ':') ' ' else ch) else ""
              val paginateAndSort = sortInfo + paginationInfo
              val sourceFuture = ViewService.getSourceInfo(viewId, session)
              RouteHelper.getResultBySource(sourceFuture,
                DavinciConstants.appJson,
                manualInfo.manualFilters.orNull,
                manualInfo.params.orNull,
                paginateAndSort,
                manualInfo.adHoc.orNull)
            }
          }
      }
    }
  }

}
