package edp.davinci.rest.bizlogic

import java.io.ByteArrayOutputStream
import java.sql.{Connection, Statement}
import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.csv.CSVWriter
import edp.davinci.module.{ConfigurationModule, PersistenceModule, _}
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import edp.endurance.db.DbConnection
import io.swagger.annotations._
import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

@Api(value = "/bizlogics", consumes = "application/json", produces = "application/json")
@Path("/bizlogics")
class BizlogicRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = postBizlogicRoute ~ putBizlogicRoute ~ getBizlogicByAllRoute ~ deleteBizlogicByIdRoute ~ getGroupsByBizIdRoute ~ getCalculationResRoute ~ deleteRelGBById
  private lazy val bizlogicService = new BizlogicService(modules)
  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  private lazy val adHocTable = "table"
  private lazy val semiSeparator = ";"
  private lazy val urlSep = "<:>"
  private lazy val defaultEncode = "UTF-8"
  private lazy val paramSep = "\\?"


  @ApiOperation(value = "get all bizlogics", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "active", value = "true or false", required = false, dataType = "boolean", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 404, message = "not found")
  ))
  def getBizlogicByAllRoute: Route = path("bizlogics") {
    get {
      parameter('active.as[Boolean].?) { active =>
        authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
          session =>
            if (session.admin) {
              onComplete(bizlogicService.getAllBiz(active.getOrElse(true))) {
                case Success(bizlogicSeq) =>
                  val queryResult = bizlogicSeq.map(biz => QueryBizlogic(biz._1, biz._2, biz._3, biz._4, biz._5, biz._6.getOrElse(""), biz._7, biz._8, biz._9, biz._10))
                  complete(OK, ResponseSeqJson[QueryBizlogic](getHeader(200, session), queryResult))
                case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
              }
            } else complete(Forbidden, ResponseJson[String](getHeader(403, "user is not admin", session), ""))
        }
      }
    }
  }


  @ApiOperation(value = "Add new bizlogics to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "bizlogics", value = "Bizlogic objects to be added", required = true, dataType = "edp.davinci.rest.PostBizlogicInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 405, message = "unspecified error")
  ))
  def postBizlogicRoute: Route = path("bizlogics") {
    post {
      entity(as[PostBizlogicInfoSeq]) {
        bizlogicSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
            session => postBizlogic(session, bizlogicSeq.payload)
          }
      }
    }
  }

  private def postBizlogic(session: SessionClass, bizlogicSeq: Seq[PostBizlogicInfo]): Route = {
    if (session.admin) {
      val uniqueTableName = adHocTable + java.util.UUID.randomUUID().toString
      val bizEntitySeq = bizlogicSeq.map(biz => Bizlogic(0, biz.source_id, biz.name, biz.sql_tmpl, uniqueTableName, Some(biz.desc), biz.trigger_type, biz.frequency, biz.`catch`, active = true, null, session.userId, null, session.userId))
      onComplete(modules.bizlogicDal.insert(bizEntitySeq)) {
        case Success(bizSeq) =>
          val queryBiz = bizSeq.map(biz => QueryBizlogic(biz.id, biz.source_id, biz.name, biz.sql_tmpl, biz.result_table, biz.desc.getOrElse(""), biz.trigger_type, biz.frequency, biz.`catch`, biz.active))
          val relSeq = for {biz <- bizSeq
                            rel <- bizlogicSeq.head.relBG
          } yield RelGroupBizlogic(0, rel.group_id, biz.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
          onComplete(modules.relGroupBizlogicDal.insert(relSeq)) {
            case Success(_) => complete(OK, ResponseSeqJson[QueryBizlogic](getHeader(200, session), queryBiz))
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }


  @ApiOperation(value = "update bizlogics in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "bizlogic", value = "Bizlogic objects to be updated", required = true, dataType = "edp.davinci.rest.PutBizlogicInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 405, message = "put bizlogic error")
  ))
  def putBizlogicRoute: Route = path("bizlogics") {
    put {
      entity(as[PutBizlogicInfoSeq]) {
        bizlogicSeq =>
          authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
            session => putBizlogicComplete(session, bizlogicSeq.payload)
          }
      }
    }
  }

  private def putBizlogicComplete(session: SessionClass, bizlogicSeq: Seq[PutBizlogicInfo]): Route = {
    try {
      val updateBizFuture = bizlogicService.updateBiz(bizlogicSeq, session)
      Await.result(updateBizFuture, Duration.Inf)
      val deleteRelFuture = bizlogicService.deleteByBizId(bizlogicSeq)
      Await.result(deleteRelFuture, Duration.Inf)
      val relSeq = for {rel <- bizlogicSeq.head.relBG
      } yield RelGroupBizlogic(0, rel.group_id, bizlogicSeq.head.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
      onComplete(modules.relGroupBizlogicDal.insert(relSeq)) {
        case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } catch {
      case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
    }
  }


  @Path("/{id}")
  @ApiOperation(value = "delete bizlogic by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteBizlogicByIdRoute: Route = modules.bizlogicRoutes.deleteByIdRoute("bizlogics")

  @Path("/groups/{rel_id}")
  @ApiOperation(value = "delete bizlogic from group by rel id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "rel_id", value = "rel_id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteRelGBById: Route = path("bizlogics" / "groups" / LongNumber) { relId =>
    delete {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session => modules.relGroupBizlogicRoutes.deleteByIdComplete(relId, session)
      }
    }
  }

  @Path("/{id}/groups")
  @ApiOperation(value = "get groups by biz id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 405, message = "internal get error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getGroupsByBizIdRoute: Route = path("bizlogics" / LongNumber / "groups") { bizId =>
    get {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          val future = bizlogicService.getGroups(bizId)
          onComplete(future) {
            case Success(relSeq) => complete(OK, ResponseSeqJson[PutRelGroupBizlogic](getHeader(200, session), relSeq))
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
      }
    }
  }


  @Path("/{id}/resultset")
  @ApiOperation(value = "get calculation results by biz id", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "adhoc_sql", value = "adhoc_sql", required = false, dataType = "string", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getCalculationResRoute: Route = path("bizlogics" / LongNumber / "resultset") { bizId =>
    post {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          entity(as[String]) { adHocSql =>
            getResultSetComplete(session, bizId, adHocSql)
          }
      }
    }
  }


  private def getResultSetComplete(session: SessionClass, bizId: Long, adHocSql: String): Route = {
    val operation = for {
      a <- bizlogicService.getSourceInfo(bizId)
      b <- bizlogicService.getSqlTmpl(bizId)
      c <- bizlogicService.getSqlParam(bizId, session)
    } yield (a, b, c)
    onComplete(operation) {
      case Success(info) =>
        if (info._1.nonEmpty) {
          val (connectionUrl, _) = info._1.head
          val (sqlTemp, tableName) = info._2.getOrElse(("", ""))
          val sqlParam = info._3.getOrElse("")
          val resultSql = getSqlArr(sqlTemp, sqlParam, tableName, adHocSql)
          val result = getResult(connectionUrl, resultSql)
          complete(OK, ResponseJson[BizlogicResult](getHeader(200, session), BizlogicResult(result)))
        } else
          complete(OK, ResponseJson[String](getHeader(200, "source info is empty", session), ""))
      case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
    }
  }


  private def getResult(connectionUrl: String, sql: Array[String]): List[String] = {
    val resultList = new ListBuffer[String]
    val columnList = new ListBuffer[String]
    var dbConnection: Connection = null
    var statement: Statement = null
    if (connectionUrl != null) {
      val connectionInfo = connectionUrl.split(urlSep)
      if (connectionInfo.size != 3) {
        logger.info("connection is not in right format")
        List("")
      }
      else {
        try {
          dbConnection = DbConnection.getConnection(connectionInfo(0), connectionInfo(1), connectionInfo(2))
          statement = dbConnection.createStatement()
          for (elem <- sql.dropRight(1)) statement.execute(elem)
          val resultSet = statement.executeQuery(sql.last)
          val meta = resultSet.getMetaData
          for (i <- 1 to meta.getColumnCount)
            columnList.append(meta.getColumnName(i))
          resultList.append(covert2CSV(columnList))
          while (resultSet.next())
            resultList.append(covert2CSV(getRow(resultSet)))
          resultList.toList
        } catch {
          case e: Throwable => logger.error("get result exception", e)
        } finally {
          if (statement != null) statement.close()
          if (dbConnection != null) dbConnection.close()
        }
        resultList.toList
      }
    } else {
      logger.info("connection is not given or is null")
      List("")
    }
  }

  private def covert2CSV(row: Seq[String]): String = {
    val byteArrOS = new ByteArrayOutputStream()
    val writer = CSVWriter.open(byteArrOS)
    writer.writeRow(row)
    val CSVStr = byteArrOS.toString(defaultEncode)
    byteArrOS.close()
    writer.close()
    CSVStr
  }

  private def getSqlArr(sqlTemp: String, param: String, tableName: String, adHocSql: String): Array[String] = {
    if (sqlTemp != "") {
      var sql = sqlTemp.trim
      logger.info("the initial sql template:" + sqlTemp)

      val paramArr = param.split(paramSep)
      paramArr.foreach(p => sql = sql.replaceFirst(paramSep, p))
      logger.info("sql template after the replacement:" + sql)

      val projectSql: String = getProjectSql(sql)
      val lastResultSql = mixinAdHocSql(projectSql, adHocSql, tableName)
      logger.info("the lastResult sql:" + lastResultSql)

      val resultSqlArr: Array[String] =
        if (sql.lastIndexOf(semiSeparator) < 0)
          lastResultSql.split(semiSeparator)
        else
          (sql.substring(0, sql.lastIndexOf(semiSeparator)) + semiSeparator + lastResultSql).split(semiSeparator)
      resultSqlArr
    } else {
      logger.info("there is no sql template")
      null.asInstanceOf[Array[String]]
    }

  }

  private def getProjectSql(sql: String): String = {
    val semiIndex = sql.lastIndexOf(semiSeparator)
    val subSql: String =
      if (semiIndex < 0) sql
      else {
        if (semiIndex == sql.length - 1) {
          if (sql.substring(0, semiIndex).lastIndexOf(semiSeparator) < 0) {
            logger.info("only the last char is semicolon")

            sql.substring(0, semiIndex)
          }
          else {
            val lastIndex = sql.substring(0, semiIndex).lastIndexOf(semiSeparator)
            logger.info("has the second last semicolon")

            sql.substring(lastIndex, semiIndex)
          }
        } else sql.substring(semiIndex)
      }
    subSql
  }

  private def mixinAdHocSql(projectSql: String, adHocSql: String, tableName: String): String = {
    if (adHocSql != "{}") {
      try {
        val sqlArr = adHocSql.split(adHocTable)
        if (sqlArr.size == 2)
          sqlArr(0) + s" ($projectSql) as `$tableName` ${sqlArr(1)}"
        else sqlArr(0) + s" ($projectSql) as `$tableName`"
      } catch {
        case e: Throwable => logger.error("adHoc sql is not in right format", e)
          projectSql
      }
    } else {
      logger.info("adHoc sql is empty")
      projectSql
    }
  }

}

