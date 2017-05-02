package edp.davinci.rest.bizlogic

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes.{Forbidden, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{ConfigurationModule, PersistenceModule, _}
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import scala.util.{Failure, Success}

@Api(value = "/bizlogics", consumes = "application/json", produces = "application/json")
@Path("/bizlogics")
class BizlogicRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = postBizlogicRoute ~ putBizlogicRoute ~ getBizlogicByAllRoute ~ deleteBizlogicByIdRoute ~ getGroupsByBizIdRoute
  private lazy val bizlogicService = new BizlogicService(modules)


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
              case Success(_) => complete(OK, getHeader(200, session))
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

  @Path("/{id}/groups")
  @ApiOperation(value = "get groups by biz id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "bizlogic id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
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


}

