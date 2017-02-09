//package edp.davinci.persistence.oauth2
//
//import edp.davinci.rest.SessionClass
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//trait GrantHandler {
//  /**
//    * Controls whether client credentials are required.  Defaults to true but can be overridden to be false when needed.
//    * Per the OAuth2 specification, client credentials are required for all grant types except password, where it is up
//    * to the authorization provider whether to make them required or not.
//    */
//  def handleRequest(request: AuthorizationRequest, authorizationHandler: AuthorizationHandler): Future[SessionClass]
//
//}
//
//class Password extends GrantHandler {
//
//  override def handleRequest(request: AuthorizationRequest, handler: AuthorizationHandler): Future[SessionClass] = {
//    val passwordRequest = new PasswordRequest(request)
//
//    handler.findUser(passwordRequest).flatMap {
//      case Some(user) =>
//        println("user: " + user)
//        val authInfo = handler.createAuthInfo(passwordRequest, user)
//        println("authInfo: " + authInfo)
//        authInfo
//      case None => Future.successful(throw new AccessDenied("username or password is incorrect"))
//    }
//  }
//}
