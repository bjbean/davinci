package edp.davinci.rest.bizlogic

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModule, PersistenceModule, _}
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.rest.widget.WidgetService
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import edp.endurance.db.DbConnection
import io.swagger.annotations._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

@Api(value = "/bizlogics", consumes = "application/json", produces = "application/json")
@Path("/bizlogics")
class BizlogicRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = postBizlogicRoute ~ putBizlogicRoute ~ getBizlogicByAllRoute ~ deleteBizlogicByIdRoute ~ getGroupsByBizIdRoute ~ getCalculationResRoute ~ deleteRelGBById
  private lazy val bizlogicService = new BizlogicService(modules)
  private lazy val widgetService = new WidgetService(modules)

  @ApiOperation(value = "get all bizlogics", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getBizlogicByAllRoute: Route = path("bizlogics") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          if (session.admin) {
            onComplete(bizlogicService.getAllBiz) {
              case Success(bizlogicSeq) =>
                if (bizlogicSeq.nonEmpty) complete(OK, ResponseSeqJson[QueryBizlogic](getHeader(200, session), bizlogicSeq))
                else complete(NotFound, getHeader(404, session))
              case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
            }
          } else complete(Forbidden, getHeader(403, session))
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
    new ApiResponse(code = 500, message = "internal server error")
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
      val bizEntitySeq = bizlogicSeq.map(biz => Bizlogic(0, biz.source_id, biz.name, biz.sql_tmpl, uniqueTableName, biz.desc, active = true, null, session.userId, null, session.userId))
      onComplete(modules.bizlogicDal.insert(bizEntitySeq)) {
        case Success(bizSeq) =>
          val queryBiz = bizSeq.map(biz => QueryBizlogic(biz.id, biz.source_id, biz.name, biz.sql_tmpl, biz.result_table, biz.desc))
          val relSeq = for {biz <- bizSeq
                            rel <- bizlogicSeq.head.relBG
          } yield RelGroupBizlogic(0, rel.group_id, biz.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
          onComplete(modules.relGroupBizlogicDal.insert(relSeq)) {
            case Success(_) => complete(OK, ResponseSeqJson[QueryBizlogic](getHeader(200, session), queryBiz))
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }


  @ApiOperation(value = "update bizlogics in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "bizlogic", value = "Bizlogic objects to be updated", required = true, dataType = "edp.davinci.rest.PutBizlogicInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "bizlogics not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
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
    val future = bizlogicService.updateBiz(bizlogicSeq, session)
    onComplete(future) {
      case Success(_) =>
        onComplete(bizlogicService.deleteByBizId(bizlogicSeq)) {
          case Success(_) =>
            val relSeq = for {rel <- bizlogicSeq.head.relBG
            } yield RelGroupBizlogic(0, rel.group_id, bizlogicSeq.head.id, rel.sql_params, active = true, null, session.userId, null, session.userId)
            onComplete(modules.relGroupBizlogicDal.insert(relSeq)) {
              case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
              case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
            }
          case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
        }
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
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
    new ApiResponse(code = 500, message = "internal server error")
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
    new ApiResponse(code = 500, message = "internal server error")
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
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getGroupsByBizIdRoute: Route = path("bizlogics" / LongNumber / "groups") { bizId =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          val future = bizlogicService.getGroups(bizId)
          onComplete(future) {
            case Success(relSeq) =>
              if (relSeq.nonEmpty) complete(OK, ResponseSeqJson[PutRelGroupBizlogic](getHeader(200, session), relSeq))
              else complete(NotFound, getHeader(404, session))
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
      }
    }
  }


  @Path("/{id}/resultset")
  @ApiOperation(value = "get calculation results by biz id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "ok"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getCalculationResRoute: Route = path("bizlogics" / LongNumber / "resultset") { bizId =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getResultSetComplete(session, bizId)
      }
    }
  }

  private def getResultSetComplete(session: SessionClass, bizId: Long) = {
    val operation = for {
      a <- bizlogicService.getSourceInfo(bizId)
      b <- widgetService.getSql(bizId)
    } yield (a, b)
    onComplete(operation) {
      case Success(info) =>
        val (connectionUrl, _) = info._1.head
        val resultSql = formatSql(info._2.head)
        val result = getResult(connectionUrl, resultSql)
        println("get result~~~~~~~~~~~~~~~~~~~~~~~~~~~")
        complete(OK, ResponseJson[BizlogicResult](getHeader(200, session), BizlogicResult(result)))
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }


  private def getResult(connectionUrl: String, sqls: String): List[Seq[String]] = {
    val resultList = new ListBuffer[Seq[String]]
    val columnList = new ListBuffer[String]
    if (connectionUrl != null) {
      val connectionInfo = connectionUrl.split("""<:>""")
      if (connectionInfo.size != 3)
        List(Seq(""))
      else {
        val dbConnection = DbConnection.getConnection(connectionInfo(0), connectionInfo(1), connectionInfo(2))
        val statement = dbConnection.createStatement()
        val resultSet = statement.executeQuery(sqls)
        val meta = resultSet.getMetaData
        for (i <- 1 to meta.getColumnCount)
          columnList.append(meta.getColumnName(i))
        resultList.append(columnList)
        while (resultSet.next())
          resultList.append(getRow(resultSet))
        resultList.toList
      }
    } else {
      List(Seq(""))
    }
  }


}

