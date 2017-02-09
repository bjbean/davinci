//package edp.davinci.persistence.oauth2
//
//import akka.http.scaladsl.server.directives.Credentials
//import edp.common.util.JsonUtils._
//import edp.davinci.module.{ConfigurationModule, PersistenceModule}
//import edp.davinci.persistence.entities.User
//import edp.davinci.rest.{SessionClass, TokenClass}
//import edp.davinci.util.JwtSession
//import slick.driver.H2Driver.api._
//
//import scala.collection.mutable.ListBuffer
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
///**
//  * Created by xiaoyanwang18 on 2016/11/18.
//  */
//trait AuthorizationHandler {
//
//  def findUser(request: AuthorizationRequest): Future[Option[User]]
//
//  def createAuthInfo(request: AuthorizationRequest, user: User): Future[SessionClass]
//
//  def validateToken(token: String): Future[Option[SessionClass]]
//
//  def generateToken(authInfo: SessionClass): TokenClass
//
//  def oauth2Authenticator(credentials: Credentials): Future[Option[SessionClass]]
//
//}
//
//class AuthorizationHandlerImpl(modules: ConfigurationModule with PersistenceModule) extends AuthorizationHandler {
//
//  override def findUser(request: AuthorizationRequest): Future[Option[User]] =
//    request match {
//      case request: PasswordRequest =>
//        modules.userDal.findByFilter(user => user.email === request.username && user.password === request.password && user.active === true).map[Option[User]](_.headOption)
//      case _ =>
//        Future.successful(None)
//    }
//
//  override def createAuthInfo(request: AuthorizationRequest, user: User): Future[SessionClass] = {
//    println("request: " + request)
//    request match {
//      case request: PasswordRequest =>
//        println("password")
//        modules.relUserGroupDal.findByFilter(rel => rel.user_id === user.id && rel.active === true).map[SessionClass] {
//          relSeq =>
//            val groupIdList = new ListBuffer[Long]
//            if (relSeq.nonEmpty) relSeq.foreach(groupIdList += _.group_id)
//            val authInfo = SessionClass(user.id, user.domain_id, groupIdList.toList, user.admin)
//            println("createAuthInfo: " + authInfo)
//            authInfo
//        }
//      case _ =>
//        println("other types")
//        Future.successful(throw new InvalidRequest("the request is invalid"))
//    }
//
//  }
//
//  override def validateToken(token: String): Future[Option[SessionClass]] = {
//    try {
//      var jwtmap = JwtSession()
//      jwtmap.refresh()
//      jwtmap = JwtSession.deserialize(token)
//      println("jwtmap decode: " + jwtmap)
//      println("jwtmap decode")
//      println("jetmap claim: " + jwtmap.claim)
//      val authInfo = json2caseClass[SessionClass](jwtmap.claim.content)
//      println("jwtmap authinfo: " + authInfo)
//      Future.successful(Some(authInfo))
//    } catch {
//      case ex: Exception =>
//        println("validate token fail")
//        println(ex)
//        Future.successful(None)
//    }
//  }
//
//  override def generateToken(authInfo: SessionClass): TokenClass = {
//    var jwtmap = JwtSession()
//    jwtmap = jwtmap + ("userId", authInfo.userId) + ("domainId", authInfo.domainId) + ("groupIdList", authInfo.groupIdList) + ("admin", authInfo.admin)
//    println("jwtsession: " + jwtmap)
//    println("token:" + jwtmap.serialize)
//    jwtmap.refresh()
//    println("token decode: " + JwtSession.deserialize(jwtmap.serialize))
//    TokenClass(jwtmap.serialize)
//  }
//
//  override def oauth2Authenticator(credentials: Credentials): Future[Option[SessionClass]] =
//    credentials match {
//      case p@Credentials.Provided(token) =>
//        validateToken(token)
//      case _ => Future.successful(None)
//    }
//
//}