package edp.davinci.rest.downloads

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.rest.{ResponseJson, SessionClass}
import edp.davinci.util.JsonProtocol._
import edp.davinci.util.ResponseUtils.getHeader
import edp.davinci.util.{AuthorizationProvider, SqlUtils}
import io.swagger.annotations._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Api(value = "/downloads", consumes = "application/json", produces = "application/json")
@Path("/downloads")
class DownloadRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with SqlUtils {
  private lazy val routeName = "downloads"
  private lazy val fDal = modules.flatTableDal
  private lazy val flatTableTQ = fDal.getTableQuery
  private lazy val relGFTQ = modules.relGroupFlatTableDal.getTableQuery
  private lazy val sourceTQ = modules.sourceDal.getTableQuery
  private lazy val widgetTQ = modules.widgetDal.getTableQuery
  private lazy val db = fDal.getDB
  private lazy val textCSV = MediaTypes.`text/csv` withCharset HttpCharsets.`UTF-8`
  val routes: Route = getWidgetURLRoute

  @Path("/csv/{widget_id}")
  @ApiOperation(value = "download csv", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget_id", value = "the entity id to downlaod", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getWidgetURLRoute: Route = path(routeName / "csv" / LongNumber) { widgetId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          onComplete(getFlatTableId(widgetId)) {
            case Success(widgetInfo) =>
              onComplete(getSourceInfo(widgetInfo._1, session)) {
                case Success(info) =>
                  if (info.nonEmpty) {
                    try {
                      val (sqlTemp, tableName, connectionUrl, _) = info.head
                      val flatTablesFilters = {
                        val filterList = info.map(_._4).filter(_.trim != "").map(_.mkString("(", "", ")"))
                        if (filterList.nonEmpty) filterList.mkString("(", "OR", ")") else null
                      }
                      val contentDisposition = headers.`Content-Disposition`(attachment, Map("filename" -> s"share.CSV")).asInstanceOf[HttpHeader]
                      val (resultList, _) = SqlUtils.sqlExecute(flatTablesFilters, sqlTemp, tableName, widgetInfo._2.getOrElse(""), "", connectionUrl)
                      val CSVResult = resultList.map(r => r.map(str => if (null != str) str.split(":").head else str)).map(row => covert2CSV(row)).mkString("\n")
                      val responseEntity = HttpEntity(textCSV, CSVResult)
                      complete(HttpResponse(headers = List(contentDisposition), entity = responseEntity))
                    } catch {
                      case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
                    }
                  }
                  else
                    complete(OK, ResponseJson[String](getHeader(200, "source info is empty", session), ""))
                case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
              }
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
          }
      }
    }
  }

  def getFlatTableId(widgetId: Long): Future[(Long, Option[String])] = {
    db.run(widgetTQ.filter(_.id === widgetId).map(w => (w.flatTable_id, w.adhoc_sql)).result.head)
  }

  def getSourceInfo(flatTableId: Long, session: SessionClass): Future[Seq[(String, String, String, String)]] = {
    val rel = if (session.admin) relGFTQ.filter(_.flatTable_id === flatTableId) else relGFTQ.filter(_.flatTable_id === flatTableId).filter(_.group_id inSet session.groupIdList)
    val query = (flatTableTQ.filter(obj => obj.id === flatTableId) join sourceTQ on (_.source_id === _.id) join
      rel on (_._1.id === _.flatTable_id))
      .map {
        case (fs, r) => (fs._1.sql_tmpl, fs._1.result_table, fs._2.connection_url, r.sql_params)
      }.result
    db.run(query)
  }

}
