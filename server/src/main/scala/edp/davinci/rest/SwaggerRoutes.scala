package edp.davinci.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.github.swagger.akka.{HasActorSystem, SwaggerHttpService}
import edp.davinci.Boot
import edp.davinci.rest.flattable.FlatTableRoutes
import edp.davinci.rest.dashboard.DashboardRoutes
import edp.davinci.rest.group.GroupRoutes
import edp.davinci.rest.libwidget.LibWidgetRoutes
import edp.davinci.rest.source.SourceRoutes
import edp.davinci.rest.sqllog.SqlLogRoutes
import edp.davinci.rest.user.UserRoutes
import edp.davinci.rest.widget.WidgetRoutes

import scala.reflect.runtime.universe._

class SwaggerRoutes extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = Boot.system
  override implicit val materializer: ActorMaterializer = Boot.materializer
  override val apiTypes = Seq(
    typeOf[LoginRoutes],
    typeOf[UserRoutes],
    typeOf[ChangePwdRoutes],
    typeOf[FlatTableRoutes],
    typeOf[DashboardRoutes],
    typeOf[WidgetRoutes],
    typeOf[SourceRoutes],
    typeOf[LibWidgetRoutes],
    typeOf[GroupRoutes],
    typeOf[SqlLogRoutes]
  )

  override val host: String = Boot.host + ":" + Boot.port
  //the url of your api, not swagger's json endpoint
  override val basePath = "/api/v1"
  //the basePath for the API you are exposing
  override val apiDocsPath = "api-docs"
  //where you want the swagger-json endpoint exposed
  //  override val info = Info("Davinci REST API")
  //  provides license and other description details

  val indexRoute: Route = get {
    pathPrefix("") {
      pathEndOrSingleSlash {
        getFromResource("swagger-ui/index.html")
      }
    } ~ getFromResourceDirectory("swagger-ui")
  }
}
