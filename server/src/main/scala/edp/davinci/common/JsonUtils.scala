package edp.davinci.common

import org.json4s.JsonAST.JNothing
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._

object JsonUtils extends JsonUtils

trait JsonUtils {
  implicit var json4sFormats: Formats = DefaultFormats

  // Case Class related
  def json2caseClass[T: Manifest](json: String): T = read[T](json)

  def caseClass2json[T <: AnyRef](obj: T): String = write[T](obj)

  def jsonCompact(json: String): String = compact(render(parse(json)))

  def jsonPretty(json: String): String = pretty(render(parse(json)))

  // JValue related
  def json2jValue(json: String): JValue = parse(json) //parseJsonString

  def jValue2json(jValue: JValue): String = compact(render(jValue)) //jsonToString

  def containsName(jValue: JValue, name: String): Boolean = jValue \ name != JNothing

  def getString(jValue: JValue, name: String): String = (jValue \ name).extract[String]

  def getInt(jValue: JValue, name: String): Int = (jValue \ name).extract[Int]

  def getLong(jValue: JValue, name: String): Long = (jValue \ name).extract[Long]

  def getShort(jValue: JValue, name: String): Short = (jValue \ name).extract[Short]

  def getBoolean(jValue: JValue, name: String): Boolean = (jValue \ name).extract[Boolean]

  def getList(jValue: JValue, name: String): List[JValue] = (jValue \ name).extract[List[JValue]]

  def getJValue(jValue: JValue, name: String): JValue = (jValue \ name).extract[JValue]
}
