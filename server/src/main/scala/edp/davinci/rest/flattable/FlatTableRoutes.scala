package edp.davinci.rest.flattable

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.{DavinciConstants, KV}
import edp.davinci.module.{ConfigurationModule, PersistenceModule, _}
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.JsonProtocol._
import edp.davinci.util.JsonUtils.json2caseClass
import edp.davinci.util.ResponseUtils._
import edp.davinci.util.{AuthorizationProvider, SqlUtils}
import io.swagger.annotations._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Api(value = "/flattables", consumes = "application/json", produces = "application/json")
@Path("/flattables")
class FlatTableRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = postFlatTableRoute ~ putFlatTableRoute ~ getFlatTableByAllRoute ~ deleteFlatTableByIdRoute ~ getGroupsByFlatIdRoute ~ getCalculationResRoute ~ deleteRelGFById
  private lazy val flatTableService = new FlatTableService(modules)
  private lazy val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val adHocTable = "table"
  private lazy val routeName = "flattables"


  @ApiOperation(value = "get all flattables", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "active", value = "true or false", required = false, dataType = "boolean", paramType = "query")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 404, message = "not found")
  ))
  def getFlatTableByAllRoute: Route = path(routeName) {
    get {
      parameter('active.as[Boolean].?) { active =>
        authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
          session =>
            if (session.admin) {
              onComplete(flatTableService.getAllFlatTbls(active.getOrElse(true))) {
                case Success(flatTableSeq) =>
                  val queryResult = flatTableSeq.map(biz => QueryFlatTable(biz._1, biz._2, biz._3, biz._4, biz._5, biz._6.getOrElse(""), biz._7, biz._8, biz._9, biz._10))
                  complete(OK, ResponseSeqJson[QueryFlatTable](getHeader(200, session), queryResult))
                case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
              }
            } else complete(Forbidden, ResponseJson[String](getHeader(403, "user is not admin", session), ""))
        }
      }
    }
  }


  @ApiOperation(value = "Add new flattables to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "flattables", value = "FlatTable objects to be added", required = true, dataType = "edp.davinci.rest.PostFlatTableInfoSeq", paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 405, message = "unspecified error")
  ))
  def postFlatTableRoute: Route = path(routeName) {
    post {
      entity(as[PostFlatTableInfoSeq]) {
        flatTableSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
            session => postFlatTable(session, flatTableSeq.payload)
          }
      }
    }
  }

  private def postFlatTable(session: SessionClass, flatTableSeq: Seq[PostFlatTableInfo]): Route = {
    if (session.admin) {
      val uniqueTableName = adHocTable + java.util.UUID.randomUUID().toString
      val bizEntitySeq = flatTableSeq.map(biz => FlatTable(0, biz.source_id, biz.name, biz.sql_tmpl, uniqueTableName, Some(biz.desc), biz.trigger_type, biz.frequency, biz.`catch`, active = true, null, session.userId, null, session.userId))
      onComplete(modules.flatTableDal.insert(bizEntitySeq)) {
        case Success(bizSeq) =>
          val queryBiz = bizSeq.map(biz => QueryFlatTable(biz.id, biz.source_id, biz.name, biz.sql_tmpl, biz.result_table, biz.desc.getOrElse(""), biz.trigger_type, biz.frequency, biz.`catch`, biz.active))
          val relSeq = for {biz <- bizSeq
                            rel <- flatTableSeq.head.relBG
          } yield RelGroupFlatTable(0, rel.group_id, biz.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
          onComplete(modules.relGroupFlatTableDal.insert(relSeq)) {
            case Success(_) => complete(OK, ResponseSeqJson[QueryFlatTable](getHeader(200, session), queryBiz))
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }


  @ApiOperation(value = "update flattables in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "flatTable", value = "FlatTable objects to be updated", required = true, dataType = "edp.davinci.rest.PutFlatTableInfoSeq", paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 405, message = "put flatTable error")
  ))
  def putFlatTableRoute: Route = path(routeName) {
    put {
      entity(as[PutFlatTableInfoSeq]) {
        flatTableSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
            session => putFlatTableComplete(session, flatTableSeq.payload)
          }
      }
    }
  }

  private def putFlatTableComplete(session: SessionClass, flatTableSeq: Seq[PutFlatTableInfo]): Route = {
    val operation = for {
      updateOP <- flatTableService.updateFlatTbl(flatTableSeq, session)
      deleteOp <- flatTableService.deleteByFlatId(flatTableSeq.map(_.id))
    } yield (updateOP, deleteOp)
    onComplete(operation) {
      case Success(_) => val relSeq = for {rel <- flatTableSeq.head.relBG
      } yield RelGroupFlatTable(0, rel.group_id, flatTableSeq.head.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
        onComplete(modules.relGroupFlatTableDal.insert(relSeq)) {
          case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
          case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
        }
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete flat table by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "id", value = "flat table id", required = true, dataType = "integer", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteFlatTableByIdRoute: Route = path(routeName / LongNumber) { flatTableId =>
    delete {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          if (session.admin) {
            val operation = for {
              deleteFlatTable <- flatTableService.deleteByFlatId(Seq(flatTableId))
              deleteRel <- flatTableService.deleteRelId(flatTableId)
              updateWidget <- flatTableService.updateWidget(flatTableId)
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
  @ApiOperation(value = "delete flattable from group by rel id", notes = "", nickname = "", httpMethod = "DELETE")
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
        session => modules.relGroupFlatTableRoutes.deleteByIdComplete(relId, session)
      }
    }
  }

  @Path("/{id}/groups")
  @ApiOperation(value = "get groups by flat table id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "id", value = "flat table id", required = true, dataType = "integer", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 405, message = "internal get error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getGroupsByFlatIdRoute: Route = path(routeName / LongNumber / "groups") { bizId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          val future = flatTableService.getGroups(bizId)
          onComplete(future) {
            case Success(relSeq) => complete(OK, ResponseSeqJson[PutRelGroupFlatTable](getHeader(200, session), relSeq))
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
  def getCalculationResRoute: Route = path(routeName / LongNumber / "resultset") { bizId =>
    post {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          entity(as[ManualInfo]) { manualInfo =>
            parameters('offset.as[Int] ? 0, 'limit.as[Int] ? 1000, 'sortby.as[String] ? "") { (offset, limit, sortBy) =>
              val paginationInfo = s" limit $limit offset $offset"
              val sortInfo = if (sortBy != "") "ORDER BY " + sortBy.map(ch => if (ch == ':') ' ' else ch) else ""
              val paginateAndSort = sortInfo + paginationInfo
              getResultSetComplete(
                session,
                bizId,
                manualInfo.adHoc.orNull,
                manualInfo.manualFilters.orNull,
                manualInfo.params.orNull,
                paginateAndSort)
            }
          }
      }
    }
  }


  private def getResultSetComplete(session: SessionClass,
                                   flatTableId: Long,
                                   adHocSql: String,
                                   manualFilters: String,
                                   paramSeq: Seq[KV],
                                   paginateAndSort: String) = {
    onComplete(flatTableService.getSourceInfo(flatTableId, session)) {
      case Success(info) =>
        if (info.nonEmpty) {
          try {
            val (sqlTemp, tableName, connectionUrl, _) = info.head
            val groupVars: Seq[KV] = info.map(_._4).map(json2caseClass[KV])
            val flatTablesFilters = {
              val filterList = info.map(_._4).filter(_.trim != "").map(_.mkString("(", "", ")"))
              if (filterList.nonEmpty) filterList.mkString("(", "OR", ")") else null
            }
            val fullFilters = if (null != manualFilters) if (null != flatTablesFilters) flatTablesFilters + s"AND ($manualFilters)" else manualFilters else flatTablesFilters
            if (sqlTemp.trim != "") {
              val (resultList, totalCount) = SqlUtils.sqlExecute(fullFilters, sqlTemp, tableName, adHocSql, paginateAndSort, connectionUrl, groupVars, paramSeq)
              val CSVResult = resultList.map(SqlUtils.covert2CSV)
              complete(OK, ResponseJson[FlatTableResult](getHeader(200, session), FlatTableResult(CSVResult, totalCount)))
            }
            else complete(BadRequest, ResponseJson[String](getHeader(400, "there is no valid sql", session), ""))
          } catch {
            case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
        }
        else
          complete(OK, ResponseJson[String](getHeader(200, "source info is empty", session), ""))
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
    }
  }


}

