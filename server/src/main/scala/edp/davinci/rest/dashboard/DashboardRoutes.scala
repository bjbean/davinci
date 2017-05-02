package edp.davinci.rest.dashboard

import javax.ws.rs.Path
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import edp.davinci.module._
import edp.davinci.persistence.entities._
import edp.davinci.rest._
import edp.davinci.util.AuthorizationProvider
import edp.davinci.util.CommonUtils._
import edp.davinci.util.JsonProtocol._
import io.swagger.annotations._
import scala.util.{Failure, Success}

@Api(value = "/dashboards", consumes = "application/json", produces = "application/json")
@Path("/dashboards")
class DashboardRoutes(modules: ConfigurationModule with PersistenceModule with BusinessModule with RoutesModuleImpl) extends Directives {

  val routes: Route = getWidgetByDashboardIdRoute ~ postDashboardRoute ~ putDashboardRoute ~ postWidget2DashboardRoute ~ getDashboardByAllRoute ~ deleteDashboardByIdRoute ~ deleteWidgetFromDashboardRoute ~ postWidget2DashboardRoute ~ putWidgetInDashboardRoute
  private lazy val dashboardService = new DashboardService(modules)

  @Path("/{dashboard_id}")
  @ApiOperation(value = "get one dashboard from system by id", notes = "", nickname = "", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard_id", value = "dashboard id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getWidgetByDashboardIdRoute: Route = path("dashboards" / LongNumber) { dashboard_id =>
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => getDashboardById(dashboard_id, session)
      }
    }
  }

  def getDashboardById(dashboardId: Long, session: SessionClass): Route = {
    onComplete(dashboardService.getBizIds(session)) {
      case Success(bizIds) =>
        onComplete(dashboardService.getInsideInfo(session, dashboardId, bizIds)) {
          case Success(dashboardInfoSeq) =>
            val infoSeq = dashboardInfoSeq.map(r => {
              val widgetInfo = PutWidgetInfo(r._7, r._8, r._9, r._10, r._11.getOrElse(""), r._12, r._13, r._14, r._15)
              DashboardInfo(r._1, r._2, r._3, r._4, r._5, r._6, widgetInfo)
            })
            complete(OK, ResponseSeqJson[DashboardInfo](getHeader(200, session), infoSeq))
          case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
        }
      case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
    }
  }

  @ApiOperation(value = "get all dashboards with the same domain", notes = "", nickname = "", httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def getDashboardByAllRoute: Route = path("dashboards") {
    get {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session =>
          onComplete(dashboardService.getAll(session)) {
            case Success(dashboardSeq) => complete(OK, ResponseSeqJson[PutDashboardInfo](getHeader(200, session), dashboardSeq))
            case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
          }
      }
    }
  }

  @ApiOperation(value = "Add new dashboards to the system", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboards", value = "Dashboard objects to be added", required = true, dataType = "edp.davinci.rest.PostDashboardInfoSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "post success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postDashboardRoute: Route = path("dashboards") {
    post {
      entity(as[PostDashboardInfoSeq]) {
        dashboardSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postDashBoard(session, dashboardSeq.payload)
          }
      }
    }
  }

  @ApiOperation(value = "update dashboards in the system", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "dashboard", value = "Dashboard objects to be updated", required = true, dataType = "edp.davinci.rest.PutDashboardSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "put success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 404, message = "dashboards not found"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putDashboardRoute: Route = path("dashboards") {
    put {
      entity(as[PutDashboardSeq]) {
        dashboardSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => putDashboardComplete(session, dashboardSeq.payload)
          }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "delete dashboard by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", value = "dashboard id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "dashboard is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteDashboardByIdRoute: Route = modules.dashboardRoutes.deleteByIdRoute("dashboards")

  @Path("/widgets")
  @ApiOperation(value = "add widgets to a dashboard", notes = "", nickname = "", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "relDashboardWidget", value = "RelDashboardWidget objects to be added", required = true, dataType = "edp.davinci.rest.PostRelDashboardWidgetSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def postWidget2DashboardRoute: Route = path("dashboards" / "widgets") {
    post {
      entity(as[PostRelDashboardWidgetSeq]) {
        relDashboardWidgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => postWidget2Dashboard(session, relDashboardWidgetSeq.payload)
          }
      }
    }
  }

  @Path("/widgets")
  @ApiOperation(value = "update widgets in the dashboard", notes = "", nickname = "", httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "relDashboardWidget", value = "RelDashboardWidget objects to be added", required = true, dataType = "edp.davinci.rest.PutRelDashboardWidgetSeq", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "OK"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 404, message = "dashboard not found"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def putWidgetInDashboardRoute: Route = path("dashboards" / "widgets") {
    put {
      entity(as[PutRelDashboardWidgetSeq]) {
        relDashboardWidgetSeq =>
          authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
            session => updateWidgetInDashboard(session, relDashboardWidgetSeq.payload)
          }
      }
    }
  }


  @Path("/widgets/{rel_id}")
  @ApiOperation(value = "delete widget from dashboard by id", notes = "", nickname = "", httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "rel_id", value = "relation id", required = true, dataType = "integer", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "delete success"),
    new ApiResponse(code = 403, message = "user is not admin"),
    new ApiResponse(code = 401, message = "authorization error"),
    new ApiResponse(code = 500, message = "internal server error")
  ))
  def deleteWidgetFromDashboardRoute: Route = path("dashboards" / "widgets" / LongNumber) { relId =>
    delete {
      authenticateOAuth2Async[SessionClass]("davinci", AuthorizationProvider.authorize) {
        session => modules.relDashboardWidgetRoutes.deleteByIdComplete(relId, session)
      }
    }
  }

  def putDashboardComplete(session: SessionClass, dashboardSeq: Seq[PutDashboardInfo]): Route = {
    if (session.admin) {
      onComplete(dashboardService.update(session, dashboardSeq)) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }
    else complete(Forbidden, getHeader(403, session))
  }

  def updateWidgetInDashboard(session: SessionClass, relSeq: Seq[PutRelDashboardWidget]): Route = {
    if (session.admin) {
      onComplete(dashboardService.updateRelDashboardWidget(session, relSeq)) {
        case Success(_) => complete(OK, getHeader(200, session))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    }
    else complete(Forbidden, getHeader(403, session))
  }

  def postDashBoard(session: SessionClass, postDashboardSeq: Seq[PostDashboardInfo]): Route = {
    if (session.admin) {
      val dashboardSeq = postDashboardSeq.map(post => Dashboard(0, post.name, post.desc, post.publish, active = true, null, session.userId, null, session.userId))
      onComplete(modules.dashboardDal.insert(dashboardSeq)) {
        case Success(dashWithIdSeq) =>
          val responseDashSeq = dashWithIdSeq.map(dashboard => PutDashboardInfo(dashboard.id, dashboard.name, dashboard.desc, dashboard.publish))
          complete(OK, ResponseSeqJson[PutDashboardInfo](getHeader(200, session), responseDashSeq))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }


  def postWidget2Dashboard(session: SessionClass, postRelDWSeq: Seq[PostRelDashboardWidget]): Route = {
    if (session.admin) {
      val relDWSeq = postRelDWSeq.map(post => RelDashboardWidget(0, post.dashboard_id, post.widget_id, post.position_x, post.position_y, post.length, post.width, active = true, null, session.userId, null, session.userId))
      onComplete(modules.relDashboardWidgetDal.insert(relDWSeq)) {
        case Success(relDWWithIdSeq) =>
          val responseRelDWSeq = relDWWithIdSeq.map(rel => PutRelDashboardWidget(rel.id, rel.dashboard_id, rel.widget_id, rel.position_x, rel.position_y, rel.length, rel.width))
          complete(OK, ResponseSeqJson[PutRelDashboardWidget](getHeader(200, session), responseRelDWSeq))
        case Failure(ex) => complete(InternalServerError, getHeader(500, ex.getMessage, session))
      }
    } else complete(Forbidden, getHeader(403, session))
  }

}