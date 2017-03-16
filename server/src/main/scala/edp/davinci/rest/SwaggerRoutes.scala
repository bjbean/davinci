package edp.davinci.rest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.swagger.akka.model.Info
import com.github.swagger.akka.{HasActorSystem, SwaggerHttpService}
import edp.davinci.Boot

import scala.reflect.runtime.universe._

class SwaggerRoutes extends SwaggerHttpService with HasActorSystem {
  override implicit val actorSystem: ActorSystem = Boot.system
  override implicit val materializer: ActorMaterializer = Boot.materializer
  override val apiTypes = Seq(
    typeOf[LoginRoutes],
    typeOf[UserRoutes],
    typeOf[ChangePwdRoutes],
    typeOf[SourceRoutes],
    typeOf[BizlogicRoutes],
    typeOf[WidgetRoutes],
    typeOf[DashboardRoutes],
    typeOf[WidgetRoutes],
    typeOf[SqlRoutes],
    typeOf[SourceRoutes],
    typeOf[LibWidgetRoutes],
    typeOf[GroupRoutes]

  )

  override val host = Boot.host + ":" + Boot.port
  //the url of your api, not swagger's json endpoint
  override val basePath = "/api/v1"
  //the basePath for the API you are exposing
  override val apiDocsPath = "api-docs"
  //where you want the swagger-json endpoint exposed
  //  override val info = Info("Davinci REST API")
  //  provides license and other description details

  val indexRoute = get {
    pathPrefix("") {
      pathEndOrSingleSlash {
        getFromResource("swagger-ui/index.html")
      }
    } ~ getFromResourceDirectory("swagger-ui")
  }
}
