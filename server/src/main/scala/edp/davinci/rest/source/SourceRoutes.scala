package edp.davinci.rest.source

import javax.ws.rs.Path

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module.{BusinessModule, ConfigurationModule, PersistenceModule, RoutesModuleImpl}
import edp.davinci.persistence.entities.{PostSourceInfo, PutSourceInfo, Source}
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils.getHeader
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


@Api(value = "/sources", consumes = "application/json", produces = "application/json")
@Path("/sources")
class SourceRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getSourceByAllRoute ~ postSourceRoute ~ putSourceRoute ~ deleteSourceByIdRoute
  private lazy val sourceService = new SourceService(modules)
  private val logger = LoggerFactory.getLogger(this.getClass)

  @ApiOperation(value = "get all source with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "sources not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def getSourceByAllRoute: Route = path("sources") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getAllSourcesComplete(session)
      }
    }
  }

  private def getAllSourcesComplete(session: SessionClass): Route = {
    if (session.admin) {
      onComplete(sourceService.getAll) {
        case Success(sourceSeq) => complete(OK, ResponseSeqJson[PutSourceInfo](getHeader(200, session), sourceSeq))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }

  //  @Path("{page=\\d+&size=\\d+}")
  //  @ApiOperation(value = "get sources with pagenifation", notes = "", nickname = "", httpMethod = "GET")
  //  @ApiImplicitParams(Array(
  //    new ApiImplicitParam(name = "pagenifation", value = "pagenifation information", required = true, dataType = "String", paramType = "path")
  //  ))
  //  @ApiResponses(Array(
  //    new ApiResponse(code = 200, message = "OK"),
  //    new ApiResponse(code = 403, message = "user is not admin"),
  //    new ApiResponse(code = 401, message = "authorization error"),
  //    new ApiResponse(code = 500, message = "internal server error")
  //  ))
  //  def getSourceByPageRoute: Route = modules.sourceRoutes.paginateRoute("sources","domain_id")


  @ApiOperation(value = "Add new sources to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sources", value = "Source objects to be added", required = true, dataType = "edp.davinci.rest.PostSourceInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def postSourceRoute: Route = path("sources") {
    post {
      entity(as[PostSourceInfoSeq]) {
        sourceSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postSource(session, sourceSeq.payload)
          }
      }
    }
  }


  private def postSource(session: SessionClass, postSourceSeq: Seq[PostSourceInfo]): Route = {
    if (session.admin) {
      val sourceSeq = postSourceSeq.map(post => Source(0, post.name, post.connection_url, post.desc, post.`type`, post.config, active = true, null, session.userId, null, session.userId))
      onComplete(modules.sourceDal.insert(sourceSeq)) {
        case Success(sourceWithIdSeq) =>
          val responseSourceSeq = sourceWithIdSeq.map(source => PutSourceInfo(source.id, source.name, source.connection_url, source.desc, source.`type`, source.config,source.active))
          complete(OK, ResponseSeqJson[PutSourceInfo](getHeader(200, session), responseSourceSeq))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }


  @ApiOperation(value = "update sources in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "sources", value = "Source objects to be updated", required = true, dataType = "edp.davinci.rest.PutSourceInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 404, message = "sources not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def putSourceRoute: Route = path("sources") {
    put {
      entity(as[PutSourceInfoSeq]) {
        sourceSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putSourceComplete(session, sourceSeq.payload)
          }
      }
    }
  }

  private def putSourceComplete(session: SessionClass, sourceSeq: Seq[PutSourceInfo]): Route = {
    if (session.admin) {
      val future = sourceService.update(sourceSeq, session)
      onComplete(future) {
        case Success(_) => complete(OK, ResponseJson[String](getHeader(200, session), ""))
        case Failure(ex) => complete(BadRequest, ResponseJson[String](getHeader(400, ex.getMessage, session), ""))
      }
    } else complete(Forbidden, ResponseJson[String](getHeader(403, session), ""))
  }


  @Path("/{id}")
  @ApiOperation(value = "delete source by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "source id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 400, message = "bad request")
  ))
  def deleteSourceByIdRoute: Route = modules.sourceRoutes.deleteByIdRoute("sources")

}
