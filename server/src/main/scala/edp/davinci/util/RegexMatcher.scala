package edp.davinci.util

import java.util.regex.Pattern

import edp.davinci.util.SqlOperators._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RegexMatcher extends RegexMatcher

trait RegexMatcher {
  def getMatchedItemList(sqlStr: String, REGEX: String): List[String] = {
    val listBuffer = ListBuffer.empty[String]
    val pattern = Pattern.compile(REGEX)
    val matcher = pattern.matcher(sqlStr)
    while (matcher.find())
      listBuffer.append(matcher.group())
    listBuffer.toList
  }
}
