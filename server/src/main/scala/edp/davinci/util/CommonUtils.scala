package edp.davinci.util

import java.sql.ResultSet

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive0
import edp.davinci.rest.{ResponseHeader, SessionClass}
import akka.http.scaladsl.server.directives.RespondWithDirectives._
import edp.common.util.DateUtils._
import edp.common.util.DtFormat
import edp.davinci.util.JwtSupport._

object CommonUtils {

  def responseHeaderWithToken(session: SessionClass): Directive0 = {
    respondWithHeader(RawHeader("token", JwtSupport.generateToken(session)))
  }

  def currentTime: String = yyyyMMddHHmmssToString(currentyyyyMMddHHmmss, DtFormat.TS_DASH_SEC)

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

  def getRow(rs: ResultSet): Seq[String] = {
    val meta = rs.getMetaData
    val columnNum = meta.getColumnCount
    (1 to columnNum).map(columnIndex => {
      val fieldValue = meta.getColumnType(columnIndex) match {
        case java.sql.Types.VARCHAR => rs.getString(columnIndex)
        case java.sql.Types.INTEGER => rs.getInt(columnIndex)
        case java.sql.Types.BIGINT => rs.getLong(columnIndex)
        case java.sql.Types.FLOAT => rs.getFloat(columnIndex)
        case java.sql.Types.DOUBLE => rs.getDouble(columnIndex)
        case java.sql.Types.BOOLEAN => rs.getBoolean(columnIndex)
        case java.sql.Types.DATE => rs.getDate(columnIndex)
        case java.sql.Types.TIMESTAMP => rs.getTimestamp(columnIndex)
        case java.sql.Types.DECIMAL => rs.getBigDecimal(columnIndex)
        case _ => println("not supported java sql type")
      }
      if (fieldValue == null) null.asInstanceOf[String]
      else fieldValue.toString
    })
  }

  def formatSql(sqlInfo: (String, String, String)): Array[String] = {
    val (olapSql, sqlTmpl, _) = sqlInfo
    (sqlTmpl + ";" + olapSql).split(";")
  }
}
