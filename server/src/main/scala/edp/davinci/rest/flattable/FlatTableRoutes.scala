package edp.davinci.rest.flattable

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
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

@Api(value = "/flattables", consumes = "application/json", produces = "application/json")
@Path("/flattables")
class FlatTableRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = postFlatTableRoute ~ putFlatTableRoute ~ getFlatTableByAllRoute ~ deleteFlatTableByIdRoute ~ getGroupsByFlatIdRoute ~ getCalculationResRoute ~ deleteRelGFById
  private lazy val flatTableService = new FlatTableService(modules)
  private lazy val logger = LoggerFactory.getLogger(this.getClass)

  private lazy val adHocTable = "table"
  private lazy val semiSeparator = ";"
  private lazy val urlSep = "<:>"
  private lazy val defaultEncode = "UTF-8"
  private lazy val paramSep = "\\?"


  @ApiOperation(value = "get all flattables", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "active", value = "true or false", required = false, dataType = "boolean", paramType = "query")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 404, message = "not found")
  ))
  def getFlatTableByAllRoute: Route = path("flattables") {
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
  def postFlatTableRoute: Route = path("flattables") {
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
  def putFlatTableRoute: Route = path("flattables") {
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
    try {
      val updateBizFuture = flatTableService.updateFlatTbl(flatTableSeq, session)
      Await.result(updateBizFuture, Duration.Inf)
      val deleteRelFuture = flatTableService.deleteByFlatId(flatTableSeq)
      Await.result(deleteRelFuture, Duration.Inf)
      val relSeq = for {rel <- flatTableSeq.head.relBG
      } yield RelGroupFlatTable(0, rel.group_id, flatTableSeq.head.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
      onComplete(modules.relGroupFlatTableDal.insert(relSeq)) {
        case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } catch {
      case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
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
  def deleteFlatTableByIdRoute: Route = modules.flatTableRoutes.deleteByIdRoute("flattables")

  @Path("/groups/{rel_id}")
  @ApiOperation(value = "delete flattable from group by rel id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(new ApiImplicitParam(name = "rel_id", value = "rel_id", required = true, dataType = "integer", paramType = "path")))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteRelGFById: Route = path("flattables" / "groups" / LongNumber) { relId =>
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
  def getGroupsByFlatIdRoute: Route = path("flattables" / LongNumber / "groups") { bizId =>
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
    new ApiImplicitParam(name = "adhoc_sql", value = "adhoc_sql", required = false, dataType = "string", paramType = "body"),
    new ApiImplicitParam(name = "offset", value = "offset", required = false, dataType = "integer", paramType = "query"),
    new ApiImplicitParam(name = "limit", value = "limit", required = false, dataType = "integer", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getCalculationResRoute: Route = path("flattables" / LongNumber / "resultset") { bizId =>
    post {
      authenticateOAuth2Async[SessionClass](AuthorizationProvider.realm, AuthorizationProvider.authorize) {
        session =>
          entity(as[String]) { adHocSql =>
            parameters('offset.as[Int] ? 0, 'limit.as[Int] ? 20) { (offset, limit) =>
              getResultSetComplete(session, bizId, adHocSql, offset, limit)
            }
          }
      }
    }
  }


  private def getResultSetComplete(session: SessionClass, bizId: Long, adHocSql: String, offset: Long, limit: Long) = {
    val paginateStr = s" limit $limit offset $offset"
    logger.info(paginateStr + "<<<<<<<<<<<<<<<<<<<<<<<<<")
    val operation = for {
      a <- flatTableService.getSourceInfo(bizId)
      b <- flatTableService.getSqlTmpl(bizId)
      c <- flatTableService.getSqlParam(bizId, session)
    } yield (a, b, c)
    onComplete(operation) {
      case Success(info) =>
        if (info._1.nonEmpty) {
          try {
            val (connectionUrl, _) = info._1.head
            val (sqlTemp, tableName) = info._2.getOrElse(("", ""))
            val sqlParam = info._3
            val resultList = mutable.ListBuffer.empty[String]
            var count = 1
            var totalCount = 0
            sqlParam.foreach(param => {
              val resultSql = getSqlArr(sqlTemp, param, tableName, adHocSql, paginateStr)
              val countNum = getResult(connectionUrl, Array(resultSql.last))
              if (countNum.size > 1)
                totalCount = countNum.last.toInt
              if (null != resultSql) {
                if (count > 1)
                  getResult(connectionUrl, resultSql.dropRight(1)).drop(1).copyToBuffer(resultList)
                else
                  getResult(connectionUrl, resultSql.dropRight(1)).copyToBuffer(resultList)
                count += 1
              }
            })
            complete(OK, ResponseJson[FlatTableResult](getHeader(200, session), FlatTableResult(resultList, offset, limit, totalCount)))
          } catch {
            case ex: Throwable => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
        }
        else
          complete(OK, ResponseJson[String](getHeader(200, "source info is empty", session), ""))
      case Failure(ex)
      => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
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
        throw new Exception("connection is not in right format:" + connectionUrl)
      }
      else {
        try {
          dbConnection = DbConnection.getConnection(connectionInfo(0), connectionInfo(1), connectionInfo(2))
          statement = dbConnection.createStatement()
          if (sql.length > 1)
            for (elem <- sql.dropRight(1)) statement.execute(elem)
          val resultSet = statement.executeQuery(sql.last)
          val meta = resultSet.getMetaData
          for (i <- 1 to meta.getColumnCount)
            columnList.append(meta.getColumnName(i) + ":" + meta.getColumnTypeName(i))
          resultList.append(covert2CSV(columnList))
          while (resultSet.next())
            resultList.append(covert2CSV(getRow(resultSet)))
          resultList.toList
        } catch {
          case e: Throwable => logger.error("get result exception", e)
            throw e
        } finally {
          if (statement != null) statement.close()
          if (dbConnection != null) dbConnection.close()
        }
      }
    } else {
      logger.info("connection is not given or is null")
      List("")
    }
  }

  /**
    *
    * @param row a row in DB represent by string
    * @return a CSV String
    */
  private def covert2CSV(row: Seq[String]): String = {
    val byteArrOS = new ByteArrayOutputStream()
    val writer = CSVWriter.open(byteArrOS)
    writer.writeRow(row)
    val CSVStr = byteArrOS.toString(defaultEncode)
    byteArrOS.close()
    writer.close()
    CSVStr
  }

  private def getSqlArr(sqlTemp: String, param: String, tableName: String, adHocSql: String, paginateStr: String) = {
    /**
      *
      * @param projectSql a SQL string; eg. SELECT * FROM Table
      * @return SQL string mixing AdHoc SQL
      */

    def mixinAdHocSql(projectSql: String) = {
      val mixinSql = if (adHocSql != "{}") {
        try {
          val sqlArr = adHocSql.split(adHocTable)
          if (sqlArr.size == 2) sqlArr(0) + s" ($projectSql) as `$tableName` ${sqlArr(1)}"
          else sqlArr(0) + s" ($projectSql) as `$tableName`"
        } catch {
          case e: Throwable => logger.error("adHoc sql is not in right format", e)
            throw e
        }
      } else {
        logger.info("adHoc sql is empty")
        projectSql
      }
      s"SELECT * FROM ($mixinSql) AS PAGINATE $paginateStr" + s";SELECT COUNT(1) FROM ($mixinSql) AS COUNTSQL"
    }


    if (sqlTemp != "") {
      var sql = sqlTemp.trim
      logger.info("the initial sql template:" + sqlTemp)

      val paramArr = param.split(paramSep)
      paramArr.foreach(p => sql = sql.replaceFirst(paramSep, s"'$p'"))
      logger.info("sql template after the replacement:" + sql)

      val semiIndex = sql.lastIndexOf(semiSeparator)
      val allSqlStr: String =
        if (semiIndex < 0)
          mixinAdHocSql(sql)
        else {
          if (semiIndex == sql.length - 1) {
            if (sql.substring(0, semiIndex).lastIndexOf(semiSeparator) < 0) {
              logger.info("only the last char is semicolon")
              mixinAdHocSql(sql.substring(0, semiIndex))
            }
            else {
              val lastIndex = sql.substring(0, semiIndex).lastIndexOf(semiSeparator)
              logger.info("has the second last semicolon")
              val mixinSql = mixinAdHocSql(sql.substring(lastIndex + 1, semiIndex))
              sql.substring(0, semiIndex + 1) + mixinSql
            }
          } else {
            val mixinSql = mixinAdHocSql(sql.substring(semiIndex + 1))
            sql.substring(0, semiIndex + 1) + mixinSql
          }
        }
      logger.info(allSqlStr + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      allSqlStr.split(semiSeparator)
    } else {
      logger.info("there is no sql template")
      null.asInstanceOf[Array[String]]
    }

  }

}

