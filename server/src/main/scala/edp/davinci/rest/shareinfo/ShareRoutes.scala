package edp.davinci.rest.shareinfo

import javax.ws.rs.Path

import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, MediaTypes}
import akka.http.scaladsl.server.{Directives, Route}
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

import scala.util.{Failure, Success}

@Api(value = "/share", consumes = "application/json", produces = "application/json")
@Path("/share")
class ShareRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives with SqlUtils {
  val routes: Route = getShareURLRoute ~ getHtmlRoute
  private lazy val shareService = new ShareService(modules)
  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val routeName = "share"
  private lazy val aesPassword = modules.config.getString("aes.password")

  @Path("/widget/{widget_id}")
  @ApiOperation(value = "get the widget share url", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "widget_id", value = "widget id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 404, message = "widget not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getShareURLRoute: Route = path(routeName / "widget" / LongNumber) { widgetId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session => getShareURL(session, widgetId)

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
      val jsonShareInfo = AesUtils.decrypt(shareInfoStr, aesPassword)
      val shareInfo: ShareInfo = json2caseClass[ShareInfo](jsonShareInfo)
      val (userId, infoId) = (shareInfo.userId, shareInfo.infoId)
      val MD5Info = MD5Utils.getMD5(caseClass2json(ShareQueryInfo(userId, infoId)))
      if (MD5Info == shareInfo.md5)
        getHtmlComplete(userId, infoId)
      else complete(HttpEntity(MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`, "".getBytes("UTF-8")))
    }
  }

  private def getHtmlComplete(userId: Long, widgetId: Long) = {
    val httpEntity = HttpEntity(MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`, "".getBytes("UTF-8"))
    val operation = for {
      widget <- shareService.getWidgetById(widgetId)
      group <- shareService.getUserGroup(userId)
    } yield (widget, group)
    onComplete(operation) {
      case Success(widgetAndGroup) =>
        val (widgetInfo, groupIds) = widgetAndGroup
        val putWidgetInfo = PutWidgetInfo(widgetInfo._1, widgetInfo._2, widgetInfo._3, widgetInfo._4, widgetInfo._5.getOrElse(""), widgetInfo._6, widgetInfo._7.getOrElse(""), widgetInfo._8, Some(widgetInfo._9))
        onComplete(shareService.getSourceInfo(putWidgetInfo.flatTable_id, groupIds)) {
          case Success(sourceInfo) =>
            if (sourceInfo.nonEmpty) {
              try {
                val (sqlTemp, tableName, connectionUrl, _) = sourceInfo.head
                val sqlParam = sourceInfo.map(_._4)
                val resultList = sqlExecute(sqlParam, sqlTemp, tableName, putWidgetInfo.adhoc_sql, "", connectionUrl)
                resultList._1.prepend(Seq(s"$tableName"))
                val htmlStr = emailHtmlStr(Seq(resultList._1))
                val responseEntity = HttpEntity(MediaTypes.`text/html` withCharset HttpCharsets.`UTF-8`, htmlStr)
                complete(OK, responseEntity)
              }
              catch {
                case _: Throwable => complete(BadRequest, httpEntity)
              }
            }
            else complete(httpEntity)

          case Failure(_) => complete(BadRequest, httpEntity)
        }
      case Failure(_) => complete(BadRequest, httpEntity)
    }
  }

//  private def htmlTableGenerator(result: List[Seq[String]]): String = {
//    if (result.nonEmpty && result.size > 1) {
//      val columns = result.head.map(r => r.split(":")(0))
//      val columnNames = columns.mkString("""<tr><th style="border:1px solid black;">""","""</th><th style="border:1px solid black;">""","""</th></tr>""")
//      val rows = for (i <- 1 until result.size) yield {
//        result(i).mkString("""<tr><td  style="border:1px solid black;" align="left">""","""</td><td  style="border:1px solid black;" align="left">""","""</td></tr>""")
//      }
//      val formatRows = rows.mkString("")
//      val resultHtml: String = s"""<html><body><table style="border:1px solid black;border-collapse:collapse;" cellspacing="10">""" + s"$columnNames $formatRows" +"""</table></body></html>"""
//      //      logInfo(resultHtml)
//      resultHtml
//    } else {
//      logger.info("has no info")
//      ""
//    }
//  }

  def emailHtmlStr(tables: Seq[Seq[Seq[String]]], stgPath: String = "stg/tmpl.stg"): String =
    STGroupFile(stgPath, Constants.DefaultEncoding, '$', '$').instanceOf("email_html")
      .map(_.add("tables", tables).render().get)
      .recover {
        case e: Exception => s"ST Error: $e"
      }.getOrElse("")

}


