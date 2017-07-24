package edp.davinci.rest.shares

import javax.ws.rs.Path
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.model.headers.ContentDispositionTypes.{attachment, inline}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.{KV, URLHelper}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities._
import edp.davinci.rest.{ShareInfo, _}
import edp.davinci.util.JsonProtocol._
import edp.davinci.util.JsonUtils.{caseClass2json, json2caseClass}
import edp.davinci.util.ResponseUtils.getHeader
import edp.davinci.util.{AesUtils, AuthorizationProvider, MD5Utils, SqlUtils}
import io.swagger.annotations._
import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import edp.davinci.DavinciConstants.conditionSeparator


@Api(value = "/shares", consumes = "application/json", produces = "application/json")
@Path("/shares")
class ShareRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with SqlUtils {
  val routes: Route = getWidgetURLRoute ~ getDashboardURLRoute ~ getHtmlRoute ~ getCSVRoute ~ getShareDashboardRoute ~ getShareWidgetRoute
  private lazy val shareService = new ShareService(modules)
  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val routeName = "shares"
  private lazy val aesPassword = modules.config.getString("aes.password")
  private lazy val textHtml = MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`
  private lazy val textCSV = MediaTypes.`text/csv` withCharset HttpCharsets.`UTF-8`
  private lazy val appJson = ContentTypes.`application/json`


  @Path("/widget/{widget_id}")
  @ApiOperation(value = "get the html share url", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget_id", value = "the entity id to share", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getWidgetURLRoute: Route = path(routeName / "widget" / LongNumber) { widgetId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          val shareWidget = ShareWidgetInfo(session.userId, widgetId)
          val aesStr = getShareURL(caseClass2json[ShareWidgetInfo](shareWidget), session.userId, widgetId)
          complete(OK, ResponseJson[String](getHeader(200, "url token", null), aesStr))
      }
    }
  }


  @Path("/dashboard/{dashboard_id}")
  @ApiOperation(value = "get the html share url", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard_id", value = "the entity id to share", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getDashboardURLRoute: Route = path(routeName / "dashboard" / LongNumber) { dashboardId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          val shareDashboard = ShareDashboardInfo(session.userId, dashboardId)
          val aesStr = getShareURL(caseClass2json[ShareDashboardInfo](shareDashboard), session.userId, dashboardId)
          complete(OK, ResponseJson[String](getHeader(200, "url token", null), aesStr))
      }
    }
  }


  private def getShareURL(shareInfo: String, userId: Long, infoId: Long) = {
    val MD5Info = MD5Utils.getMD5(shareInfo)
    val shareQueryInfo = ShareInfo(userId, infoId, MD5Info)
    val password = modules.config.getString("aes.password")
    AesUtils.encrypt(caseClass2json(shareQueryInfo), password)
  }


  @Path("/html/{share_info}")
  @ApiOperation(value = "get html by share info", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "share_info", value = "share info value", required = true, dataType = "string", paramType = "path"),
    new ApiImplicitParam(name = "limit", value = "limit", required = false, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "offset", value = "offset", required = false, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "sortby", value = "sortby", required = false, dataType = "string", paramType = "query")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getHtmlRoute: Route = path(routeName / "html" / Segment) { shareInfoStr =>
    get {
      parameters('offset.as[Int] ? 0, 'limit.as[Int] ? -1, 'sortby.as[String] ? "") { (offset, limit, sortby) =>
        val paginationInfo = if (limit != -1) s" limit $limit offset $offset" else ""
        val sortInfo = if (sortby != "") "ORDER BY " + sortby.map(ch => if (ch == ':') ' ' else ch) else ""
        val paginateAndSort = sortInfo + paginationInfo
        verifyAndGetResult(shareInfoStr, textHtml, paginateAndSort)
      }
    }
  }


  @Path("/csv/{share_info}")
  @ApiOperation(value = "get csv by share info", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "share_info", value = "share info value", required = true, dataType = "string", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getCSVRoute: Route = path(routeName / "csv" / Segment) { shareInfoStr =>
    get {
      verifyAndGetResult(shareInfoStr, textCSV, "")
    }
  }


  @Path("/widget/{share_info}")
  @ApiOperation(value = "get widget by share info", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "share_info", value = "share info value", required = true, dataType = "string", paramType = "path"),
    new ApiImplicitParam(name = "limit", value = "limit", required = false, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "offset", value = "offset", required = false, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "sortby", value = "sortby", required = false, dataType = "string", paramType = "query")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getShareWidgetRoute: Route = path(routeName / "widget" / Segment) { shareInfoStr =>
    get {
      parameters('offset.as[Int] ? 0, 'limit.as[Int] ? -1, 'sortby.as[String] ? "") { (offset, limit, sortby) =>
        val paginationInfo = if (limit != -1) s" limit $limit offset $offset" else ""
        val sortInfo = if (sortby != "") "ORDER BY " + sortby.map(ch => if (ch == ':') ' ' else ch) else ""
        val paginateAndSort = sortInfo + paginationInfo
        verifyAndGetResult(shareInfoStr, appJson, paginateAndSort)
      }
    }
  }


  @Path("/dashboard/{share_info}")
  @ApiOperation(value = "get shared dashboard by share info", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "share_info", value = "share info value", required = true, dataType = "string", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getShareDashboardRoute: Route = path(routeName / "dashboard" / Segment) { shareInfoStr =>
    get {
      try {
        val jsonShareInfo = AesUtils.decrypt(shareInfoStr, aesPassword)
        val shareInfo: ShareInfo = json2caseClass[ShareInfo](jsonShareInfo)
        val (userId, infoId) = (shareInfo.userId, shareInfo.infoId)
        val MD5Info = MD5Utils.getMD5(caseClass2json[ShareDashboardInfo](ShareDashboardInfo(userId, infoId)))
        if (MD5Info == shareInfo.md5) {
          getDashboardComplete(userId, infoId)
        }
        else complete(BadRequest, ResponseJson[String](getHeader(400, "bad request", null), ""))
      } catch {
        case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
      }
    }
  }


  private def getDashboardComplete(userId: Long, infoId: Long) = {
    val operation = for {
      group <- shareService.getUserGroup(userId)
      user <- shareService.getUserInfo(userId)
    } yield (group, user)
    onComplete(operation) {
      case Success(userGroup) =>
        val (groupIds, admin) = userGroup
        val dashboardInfo = for {
          dashboard <- shareService.getDashBoard(infoId)
          widgetInfo <- shareService.getShareDashboard(infoId, groupIds, admin)
        } yield (dashboard, widgetInfo)
        onComplete(dashboardInfo) {
          case Success(shareDashboard) =>
            val (dashboard, widgets) = shareDashboard
            val infoSeq = widgets.map(r => {
              val aesStr = getShareURL(caseClass2json[ShareWidgetInfo](ShareWidgetInfo(userId, r._1)), userId, r._1)
              WidgetInfo(r._1, r._2, r._3, r._4, r._5, r._6, r._7, r._8, r._9, aesStr)
            })
            val dashboardInfo = DashboardInfo(dashboard._1, dashboard._2, dashboard._3.getOrElse(""), dashboard._4, dashboard._5, infoSeq)
            complete(OK, ResponseJson[DashboardInfo](getHeader(200, null), dashboardInfo))
          case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
        }
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
    }
  }


  private def verifyAndGetResult(shareInfoStr: String, contentType: ContentType.NonBinary, paginateAndSort: String): Route = {
    val infoArr: Array[String] = shareInfoStr.split(conditionSeparator.toString)
    if (infoArr.head.trim != "") {
      try {
        val jsonShareInfo = AesUtils.decrypt(infoArr.head.trim, aesPassword)
        val shareInfo: ShareInfo = json2caseClass[ShareInfo](jsonShareInfo)
        val (userId, infoId) = (shareInfo.userId, shareInfo.infoId)
        val MD5Info = MD5Utils.getMD5(caseClass2json(ShareWidgetInfo(userId, infoId)))
        if (MD5Info == shareInfo.md5) {
          if (infoArr.length == 2) {
            val base64decoder = new sun.misc.BASE64Decoder
            val base64decode: String = new String(base64decoder.decodeBuffer(infoArr.last))
            val setParamAndFilter: URLHelper = json2caseClass[URLHelper](base64decode)
            getResultComplete(userId, infoId, contentType, setParamAndFilter.f_get, setParamAndFilter.p_get, paginateAndSort)
          } else
            getResultComplete(userId, infoId, contentType, null, null, paginateAndSort)
        }
        else complete(HttpEntity(contentType, "".getBytes("UTF-8")))
      } catch {
        case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
      }
    } else complete(BadRequest, ResponseJson[String](getHeader(400, "User Authentication Failed", null), ""))
  }


  private def getResultComplete(userId: Long, widgetId: Long, contentType: ContentType.NonBinary, urlfilters: String, paramSeq: Seq[KV], paginateAndSort: String) = {
    val operation = for {
      widget <- shareService.getWidgetById(widgetId)
      group <- shareService.getUserGroup(userId)
      user <- shareService.getUserInfo(userId)
    } yield (widget, group, user)
    onComplete(operation) {
      case Success(widgetAndGroup) =>
        val (widgetInfo, groupIds, admin) = widgetAndGroup
        val putWidgetInfo = PutWidgetInfo(widgetInfo._1, widgetInfo._2, widgetInfo._3, widgetInfo._4, widgetInfo._5.orNull, widgetInfo._6, widgetInfo._7.getOrElse(""), widgetInfo._8, Some(widgetInfo._9))
        val sourceFuture = shareService.getSourceInfo(putWidgetInfo.flatTable_id, groupIds, admin)
        HtmlOrCSVComplete(sourceFuture, putWidgetInfo, contentType, urlfilters, paramSeq, paginateAndSort)
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
    }
  }


  private def HtmlOrCSVComplete(sourceFuture: Future[Seq[(String, String, String, String)]],
                                putWidgetInfo: PutWidgetInfo,
                                contentType: ContentType.NonBinary,
                                urlfilters: String,
                                paramSeq: Seq[KV],
                                paginateAndSort: String) = {
    onComplete(sourceFuture) {
      case Success(sourceInfo) =>
        if (sourceInfo.nonEmpty) {
          try {
            val (sqlTemp, tableName, connectionUrl, _) = sourceInfo.head
            val flatTablesFilters = {
              val filterList = sourceInfo.map(_._4).filter(_.trim != "").map(_.mkString("(", "", ")"))
              if (filterList.nonEmpty) filterList.mkString("(", "OR", ")") else null
            }
            val fullFilters = if (urlfilters != null) if (flatTablesFilters != null) flatTablesFilters + s"AND ($urlfilters)" else urlfilters else flatTablesFilters
            logger.info(fullFilters + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ fullFilters")
            if (sqlTemp.trim != "") {
              val resultList = sqlExecute(fullFilters, sqlTemp, tableName, putWidgetInfo.adhoc_sql, paginateAndSort, connectionUrl,null, paramSeq)
              contentTypeMatch(resultList, contentType, putWidgetInfo)
            } else complete(BadRequest, ResponseJson[String](getHeader(400, "flatTable sqls is empty", null), ""))
          }
          catch {
            case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
          }
        } else complete(BadRequest, ResponseJson[String](getHeader(400, "", null), "source info is empty"))
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
    }
  }


  private def contentTypeMatch(resultList: (ListBuffer[Seq[String]], Long), contentType: ContentType.NonBinary, putWidgetInfo: PutWidgetInfo) = {
    val contentDisposition = if (contentType == textHtml) headers.`Content-Disposition`(inline, Map("filename" -> s"share.html")).asInstanceOf[HttpHeader]
    else headers.`Content-Disposition`(attachment, Map("filename" -> s"share.CSV")).asInstanceOf[HttpHeader]
    contentType match {
      case `textHtml` =>
        complete(HttpResponse(headers = List(contentDisposition), entity = HttpEntity(textHtml, getHTMLStr(resultList._1))))
      case `textCSV` =>
        val responseEntity = HttpEntity(textCSV, resultList._1.map(row => covert2CSV(row)).mkString("\n"))
        complete(HttpResponse(headers = List(contentDisposition), entity = responseEntity))
      case `appJson` =>
        val CSVResult = resultList._1.map(covert2CSV)
        complete(OK, ResponseJson[ShareResult](getHeader(200, null), ShareResult(putWidgetInfo, CSVResult, resultList._2)))
      case _ => logger.info("unsupported contentType")
        complete(BadRequest, ResponseJson[String](getHeader(400, "", null), "unsupported contentType"))
    }
  }

}


