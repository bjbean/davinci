package edp.davinci.rest

import akka.http.scaladsl.server.{Directives, Route}

class DavinciRoutes extends Directives {
  //  override implicit val actorSystem: ActorSystem = Boot.system
  //  override implicit val materializer: ActorMaterializer = Boot.materializer


  //  override val host: String = Boot.host + ":" + Boot.port
  //  //the url of your api, not swagger's json endpoint
  //  override val basePath = "/api/v1"
  //  //the basePath for the API you are exposing
  //  override val apiDocsPath = "api-docs"
  //where you want the swagger-json endpoint exposed
  //  override val info = Info("Davinci REST API")
  //  provides license and other description details

  val indexRoute: Route = get {
    pathPrefix("") {
      pathEndOrSingleSlash {
        getFromResource("davinci-ui/index.html")
      }
    } ~ getFromResourceDirectory("davinci-ui")
  }

  val shareRoute: Route = get {
    pathPrefix("share") {
      pathEndOrSingleSlash {
        getFromResource("davinci-ui/share.html")
      }
    } ~ getFromResourceDirectory("davinci-ui")
  }
}
