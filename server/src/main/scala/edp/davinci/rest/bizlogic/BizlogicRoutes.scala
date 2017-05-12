package edp.davinci.rest.bizlogic

import java.sql.{Connection, Statement}
import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directive1, Directives, Route}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
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
  private val logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(value = "get all bizlogics", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request"),
    new ApiResponse(code = 404, message = "not found")
  ))
  def getBizlogicByAllRoute: Route = path("bizlogics") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          if (session.admin) {
            onComplete(bizlogicService.getAllBiz) {
              case Success(bizlogicSeq) =>
                val queryResult = bizlogicSeq.map(biz => QueryBizlogic(biz._1, biz._2, biz._3, biz._4, biz._5, biz._6.getOrElse("")))
                complete(OK, ResponseSeqJson[QueryBizlogic](getHeader(200, session), queryResult))
              case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
            }
          } else complete(Forbidden, ResponseJson[String](getHeader(403, "user is not admin", session), ""))
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
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postBizlogic(session, bizlogicSeq.payload)
          }
      }
    }
  }

  private def postBizlogic(session: SessionClass, bizlogicSeq: Seq[PostBizlogicInfo]): Route = {
    if (session.admin) {
      val uniqueTableName = "table" + java.util.UUID.randomUUID().toString
      val bizEntitySeq = bizlogicSeq.map(biz => Bizlogic(0, biz.source_id, biz.name, biz.sql_tmpl, uniqueTableName, Some(biz.desc), active = true, null, session.userId, null, session.userId))
      onComplete(modules.bizlogicDal.insert(bizEntitySeq)) {
        case Success(bizSeq) =>
          val queryBiz = bizSeq.map(biz => QueryBizlogic(biz.id, biz.source_id, biz.name, biz.sql_tmpl, biz.result_table, biz.desc.getOrElse("")))
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
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
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
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
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
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          val future = bizlogicService.getGroups(bizId)
          onComplete(future) {
            case Success(relSeq) => complete(OK, ResponseSeqJson[PutRelGroupBizlogic](getHeader(200, session), relSeq))
            case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
          }
      }
    }
  }


  @Path("/{id}/resultset/{olap_sql}")
  @ApiOperation(value = "get calculation results by biz id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path"),
    new ApiImplicitParam(name = "olap_sql", value = "olap_sql", required = true, dataType = "string", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getCalculationResRoute: Route = path("bizlogics" / LongNumber / "resultset" / Segment) { (bizId, olapSql) =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getResultSetComplete(session, bizId, olapSql)
      }
    }
  }




  private def getResultSetComplete (session: SessionClass, bizId: Long, olapSql: String = null): Route = {
  val operation = for {
  a <- bizlogicService.getSourceInfo (bizId)
  b <- bizlogicService.getSqlTmpl (bizId)
  c <- bizlogicService.getSqlParam (bizId, session)
} yield (a, b, c)
  onComplete (operation) {
  case Success (info) =>
  val (connectionUrl, _) = info._1.head
  val (sqlTmpl, tableName) = info._2.getOrElse (("", "") )
  val sqlParam = info._3.getOrElse ("")
  val resultSql = fullfilSql (sqlTmpl, sqlParam, tableName, olapSql)
  val result = getResult (connectionUrl, resultSql)
  println ("get result~~~~~~~~~~~~~~~~~~~~~~~~~~~")
  complete (OK, ResponseJson[BizlogicResult] (getHeader (200, session), BizlogicResult (result) ) )
  case Failure (ex) => complete (BadRequest, ResponseJson[String] (getHeader (400, ex.getMessage, session), "") )
}
}


  private def getResult (connectionUrl: String, sql: String): List[Seq[String]] = {
  val resultList = new ListBuffer[Seq[String]]
  val columnList = new ListBuffer[String]
  var dbConnection: Connection = null
  var statement: Statement = null
  if (connectionUrl != null) {
  val connectionInfo = connectionUrl.split ("""<:>""")
  if (connectionInfo.size != 3)
  List (Seq ("") )
  else {
  try {
  dbConnection = DbConnection.getConnection (connectionInfo (0), connectionInfo (1), connectionInfo (2) )
  statement = dbConnection.createStatement ()
  val resultSet = statement.executeQuery (sql)
  val meta = resultSet.getMetaData
  for (i <- 1 to meta.getColumnCount)
  columnList.append (meta.getColumnName (i) )
  resultList.append (columnList)
  while (resultSet.next () )
  resultList.append (getRow (resultSet) )
  resultList.toList
} catch {
  case e: Throwable => logger.error ("get reuslt exception", e)
} finally {
  if (statement != null)
  statement.close ()
  if (dbConnection != null)
  dbConnection.close ()
}
  resultList.toList
}
} else {
  List (Seq ("") )
}
}

  private def fullfilSql (sqlTmpl: String, param: String, tableName: String, olap_sql: String = null) = {
  var sqlTmp = sqlTmpl
  println (sqlTmpl)
  val paramArr = param.split ("\\?")
  paramArr.foreach (p => sqlTmp = sqlTmp.replaceFirst ("\\?", p) )
  println (sqlTmp)
  sqlTmp = if (olap_sql != "") {
  val sqlArr = olap_sql.split ("from")
  sqlArr (0) + s" from ($sqlTmp) as a"
} else sqlTmp
  println (sqlTmp)
  sqlTmp
}

}

