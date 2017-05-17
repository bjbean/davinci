package edp.davinci.util

import edp.common.util.JsonUtils._
import edp.davinci.module.ConfigurationModuleImpl
import edp.davinci.rest.SessionClass
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtHeader}

import scala.util.Try

object JwtSupport extends ConfigurationModuleImpl {

  private val typ = Option(config.getString("jwtToken.typ")).getOrElse("JWT")
  private val secret = config.getString("jwtToken.secret")
  private val timeout = Option(config.getLong("jwtToken.timeout")).getOrElse(60L)
  private val algorithm = Option(config.getString("jwtToken.algorithm")).map(JwtAlgorithm.fromString)
    .flatMap {
      case alg: JwtHmacAlgorithm => Option(alg)
      case _ => throw new RuntimeException("The algorithm is not support")
    }.getOrElse(JwtAlgorithm.HS256)
  private val header = JwtHeader(algorithm, typ)

  def generateToken(session: SessionClass): String = {
    val claim = JwtClaim(caseClass2json(session)).expiresIn(timeout)
    Jwt.encode(header, claim, secret)
  }

  def decodeToken(token: String): SessionClass = {
    val decodeToken: Try[(String, String, String)] = Jwt.decodeRawAll(token, secret, Seq(algorithm))
    val session = json2caseClass[SessionClass](decodeToken.get._2)
    session
  }

}
