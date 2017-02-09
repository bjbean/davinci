package edp.davinci.util

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive0
import edp.davinci.rest.{ResponseHeader, SessionClass}
import akka.http.scaladsl.server.directives.RespondWithDirectives._
import edp.common.util.DateUtils._
import edp.common.util.DtFormat
import edp.davinci.util.JwtSupport._

object Utils {

  def responseHeaderWithToken(session: SessionClass): Directive0 = {
    respondWithHeader(RawHeader("token", JwtSupport.generateToken(session)))
  }

  def currentTime = yyyyMMddHHmmssToString(currentyyyyMMddHHmmss, DtFormat.TS_DASH_SEC)

  val msgmap = Map(200 -> "Success", 404 -> "Not found", 401 -> "Unauthorized", 403 -> "User not admin", 500 -> "Internal server error")

  def getHeader(code: Int, session: SessionClass): ResponseHeader = {
    if (session != null)
      ResponseHeader(code, msgmap(code), generateToken(session))
    else
      ResponseHeader(code, msgmap(code))
  }

  def getHeader(code: Int, msg: String, session: SessionClass): ResponseHeader = {
    if (session != null)
      ResponseHeader(code, msg, generateToken(session))
    else
      ResponseHeader(code, msg)
  }

}
