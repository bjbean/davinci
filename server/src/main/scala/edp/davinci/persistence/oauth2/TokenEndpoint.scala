//package edp.davinci.persistence.oauth2
//
//import edp.davinci.rest.SessionClass
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
///**
//  * Created by xiaoyanwang18 on 2016/11/18.
//  */
//trait TokenEndpoint {
//  val handlers = Map.empty[String, GrantHandler]
//
//  def handleRequest(request: AuthorizationRequest, handler: AuthorizationHandler): Future[Either[OAuthError, SessionClass]] =
//    try {
//      val grantType = request.grantType
//      val grantHandler = handlers.getOrElse(grantType, throw new UnsupportedGrantType(s"${grantType} is not supported"))
//
//      grantHandler.handleRequest(request, handler).map(Right(_)).recover {
//        case e: OAuthError => Left(e)
//      }
//    } catch {
//      case e: OAuthError => Future.successful(Left(e))
//    }
//}
//
//object TokenEndpoint extends TokenEndpoint