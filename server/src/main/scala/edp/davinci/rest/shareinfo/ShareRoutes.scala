package edp.davinci.rest.shareinfo

import javax.ws.rs.Path

import org.apache.commons.lang3.StringEscapeUtils
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentType, HttpCharsets, HttpEntity, MediaTypes}
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
import org.clapper.scalasti.{Constants, STGroupFile}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Api(value = "/share", consumes = "application/json", produces = "application/json")
@Path("/share")
class ShareRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with SqlUtils {
  val routes: Route = getShareURLRoute ~ getHtmlRoute ~ getCSVRoute
  private lazy val shareService = new ShareService(modules)
  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val routeName = "share"
  private lazy val aesPassword = modules.config.getString("aes.password")
  private lazy val textHtml = MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`
  private lazy val textCSV = MediaTypes.`text/csv` withCharset HttpCharsets.`UTF-8`
  private lazy val conditionSeparator = ","
  private lazy val sqlSeparator = ";"

  @Path("/url/{id}")
  @ApiOperation(value = "get the share url", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "the entity id to share", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getShareURLRoute: Route = path(routeName / "url" / LongNumber) { id =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session => getShareURL(session, id)

      }
    }
  }

  private def getShareURL(session: SessionClass, widgetId: Long) = {
    val shareInfo = ShareQueryInfo(session.userId, widgetId)
    val MD5Info = MD5Utils.getMD5(caseClass2json(shareInfo))
    val shareQueryInfo = ShareInfo(session.userId, widgetId, MD5Info)
    val password = modules.config.getString("aes.password")
    val aesStr = AesUtils.encrypt(caseClass2json(shareQueryInfo), password)
    complete(OK, ResponseJson[String](getHeader(200, "aes str", session), aesStr))
  }


  @Path("/html/{share_info}")
  @ApiOperation(value = "get html by share info", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "share_info", value = "share info value", required = true, dataType = "string", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getHtmlRoute: Route = path(routeName / "html" / Segment) { shareInfoStr =>
    get {
      verifyAndGetResult(shareInfoStr, textHtml)
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
      verifyAndGetResult(shareInfoStr, textCSV)
    }
  }


  @Path("/widget/{share_info}")
  @ApiOperation(value = "get widget by share info", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "share_info", value = "share info value", required = true, dataType = "string", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getShareWidgetRoute: Route = path(routeName / "widget" / Segment) { shareInfoStr =>
    get {
      verifyAndGetResult(shareInfoStr, textCSV)
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
      verifyAndGetResult(shareInfoStr, textCSV)
    }
  }


  private def verifyAndGetResult(shareInfoStr: String, contentType: ContentType.NonBinary): Route = {
    val infoArr: Array[String] = shareInfoStr.split(conditionSeparator.toString)
    if (infoArr.head.trim != "") {
      val jsonShareInfo = AesUtils.decrypt(infoArr.head.trim, aesPassword)
      val shareInfo: ShareInfo = json2caseClass[ShareInfo](jsonShareInfo)
      val (userId, infoId) = (shareInfo.userId, shareInfo.infoId)
      val MD5Info = MD5Utils.getMD5(caseClass2json(ShareQueryInfo(userId, infoId)))
      if (MD5Info == shareInfo.md5) {
        if (infoArr.length == 2) {
          val base64decoder = new sun.misc.BASE64Decoder
          val base64decode: String = new String(base64decoder.decodeBuffer(infoArr.last))
          val setParamAndFilter: URLHelper = json2caseClass[URLHelper](base64decode)
          getResultComplete(userId, infoId, contentType, setParamAndFilter.f, setParamAndFilter.p)
        } else
          getResultComplete(userId, infoId, contentType, null, null)
      }
      else complete(HttpEntity(contentType, "".getBytes("UTF-8")))
    } else
      complete(HttpEntity(contentType, "User Authentication Failed".getBytes("UTF-8")))
  }


  private def getHtmlStr(resultList: ListBuffer[Seq[String]]) = {
    val columns = resultList.head.map(c => c.split(":").head)
    resultList.remove(0)
    resultList.prepend(columns)
    resultList.prepend(Seq(""))
    emailHtmlStr(Seq(resultList))
  }


  private def getResultComplete(userId: Long, widgetId: Long, contentType: ContentType.NonBinary, filters: String, paramSeq: Seq[KV]) = {
    val httpEntity = HttpEntity(contentType, "".getBytes("UTF-8"))
    val operation = for {
      widget <- shareService.getWidgetById(widgetId)
      group <- shareService.getUserGroup(userId)
      user <- shareService.getUserInfo(userId)
    } yield (widget, group, user)
    onComplete(operation) {
      case Success(widgetAndGroup) =>
        val (widgetInfo, groupIds, admin) = widgetAndGroup
        val putWidgetInfo = PutWidgetInfo(widgetInfo._1, widgetInfo._2, widgetInfo._3, widgetInfo._4, widgetInfo._5.getOrElse(""), widgetInfo._6, widgetInfo._7.getOrElse(""), widgetInfo._8, Some(widgetInfo._9))
        onComplete(shareService.getSourceInfo(putWidgetInfo.flatTable_id, groupIds, admin)) {
          case Success(sourceInfo) =>
            if (sourceInfo.nonEmpty) {
              try {
                val (sqlTemp, tableName, connectionUrl, _) = sourceInfo.head
                val flatTablesFilters = {
                  val filterList = sourceInfo.map(_._4).filter(_.trim != "").map(_.mkString("(", "", ")"))
                  if(filterList.nonEmpty) filterList.mkString("(","OR",")") else null
                }
                if (sqlTemp != "") {
                  val resetSqlBuffer = if (paramSeq != null) resetSql(sqlTemp.split(sqlSeparator), paramSeq) else sqlTemp.split(sqlSeparator).toBuffer
                  val fullFilters =
                    if (filters != null) {
                      if(flatTablesFilters != null) flatTablesFilters + s"AND ($filters)"  else filters}
                  else flatTablesFilters
                  resetSqlBuffer.foreach(println)
                  val projectSql = getProjectSql(resetSqlBuffer.last, fullFilters, tableName, putWidgetInfo.adhoc_sql)
                  logger.info("project sql ^^^^^^^^^^^^^:" + projectSql)
                  resetSqlBuffer.remove(resetSqlBuffer.length - 1)
                  resetSqlBuffer.append(projectSql.split(sqlSeparator).head)
                  val resultList = getResult(connectionUrl, resetSqlBuffer.toArray)
                  val htmlORCSVStr = if (contentType == textHtml)
                    getHtmlStr(resultList) else resultList.map(row => covert2CSV(row)).mkString("\n")
                  val responseEntity = HttpEntity(contentType, htmlORCSVStr)
                  complete(OK, responseEntity)
                } else
                  complete(BadRequest, httpEntity)
              }
              catch {
                case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, null), ""))
              }
            } else complete(BadRequest, httpEntity)
          case Failure(_) => complete(BadRequest, httpEntity)
        }
      case Failure(_) => complete(BadRequest, httpEntity)
    }
  }


  private def resetSql(sqlArr: Array[String], paramSeq: Seq[KV]) = {
    val paramMap = mutable.HashMap.empty[String, String]
    paramSeq.foreach(param => paramMap(param.k) = param.v)
    val resetSql: mutable.Buffer[String] = sqlArr.map(sql => {
      val lowerCaseSql = sql.trim.toLowerCase
      if (lowerCaseSql.startsWith("set")) {
        val setKey = lowerCaseSql.substring(lowerCaseSql.indexOf('@')+1, lowerCaseSql.indexOf('=')).trim
        if (paramMap.contains(setKey)) s"SET @$setKey = '${paramMap(setKey)}'" else sql
      } else sql
    }).toBuffer
    resetSql
  }


  def emailHtmlStr(tables: Seq[Seq[Seq[String]]], stgPath: String = "stg/tmpl.stg"): String =
    STGroupFile(stgPath, Constants.DefaultEncoding, '$', '$').instanceOf("email_html")
      .map(_.add("tables", tables).render().get)
      .recover {
        case e: Exception => s"ST Error: $e"
      }.getOrElse("")

}

