package edp.davinci.util

import akka.http.scaladsl.server.directives.Credentials
import edp.davinci.module.{ConfigurationModuleImpl, PersistenceModuleImpl}
import edp.davinci.persistence.entities.{QueryUserInfo, User}
import edp.davinci.rest.{LoginClass, SessionClass}
import org.slf4j.LoggerFactory
import slick.jdbc.MySQLProfile.api._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AuthorizationError(val statusCode: Int = 401, val desc: String = "authentication error") extends Exception

class InternalError(statusCode: Int = 500, desc: String = "internal server error") extends AuthorizationError(statusCode, desc)

class UserNotFoundError(statusCode: Int = 404, desc: String = "user not found") extends AuthorizationError(statusCode, desc)

class PassWordError(statusCode: Int = 400, desc: String = "password is wrong") extends AuthorizationError(statusCode, desc)

object AuthorizationProvider extends ConfigurationModuleImpl with PersistenceModuleImpl {
  private val logger = LoggerFactory.getLogger(this.getClass)
  lazy val realm = "davinci"

  def createSessionClass(login: LoginClass): Future[Either[AuthorizationError, (SessionClass, QueryUserInfo)] with Product with Serializable] = {
    try {
      val user = findUser(login)
      user.flatMap {
        user =>
          relUserGroupDal.findByFilter(rel => rel.user_id === user.id && rel.active).map {
            relSeq =>
              val groupIdList = new ListBuffer[Long]
              if (relSeq.nonEmpty) relSeq.foreach(groupIdList += _.group_id)
              val userInfo = QueryUserInfo(user.id, user.email, user.title, user.name, user.admin, user.active)
              val session = SessionClass(user.id, groupIdList.toList, user.admin)
              (session, userInfo)
          }
      }.map(Right(_)).recover {
        case e: AuthorizationError =>
          logger.error("createSessionClass error", e)
          Left(e)
      }
    } catch {
      case e: AuthorizationError =>
        logger.error("createSessionClass error", e)
        Future.successful(Left(e))
    }

  }

  def authorize(credentials: Credentials): Future[Option[SessionClass]] =
    credentials match {
      case p@Credentials.Provided(token) =>
        validateToken(token)
      case _ => Future.successful(None)
    }


  private def findUser(login: LoginClass): Future[User] = {
    userDal.findByFilter(user => user.email === login.username && user.active === true).map[User] {
      userSeq =>
        println(userSeq.headOption)
        userSeq.headOption match {
          case Some(user) =>
            if (verifyPassWord(user.password, login.password)) user
            else throw new PassWordError()
          case None =>
            println("not found")
            logger.info("user not found")
            throw new UserNotFoundError()
        }
    }
  }

  def validateToken(token: String): Future[Option[SessionClass]] = {
    try {
      val session = JwtSupport.decodeToken(token)
      Future.successful(Some(session))
    } catch {
      case e: Exception =>
        logger.error("validateToken error", e)
        Future.successful(None)
    }
  }


  private def verifyPassWord(storePass: String, pass: String): Boolean = {
    //    pass.isBcrypted(storePass)
    if (storePass == pass) true
    else false
  }


}
