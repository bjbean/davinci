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



  def matchAndReplace(sqlList: Array[String], REGEX: String, kvMap: mutable.HashMap[String, List[String]]): Array[String] = {
    sqlList.map(sql => {
      val exprList = RegexMatcher.getMatchedItemList(sql, REGEX)
      val parsedMap = SqlParser.getParsedMap(exprList)
      val replaceMap = getReplaceStr(parsedMap, kvMap)
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~sql before replace")
      println(sql)
      var resultSql = sql
      replaceMap.foreach(tuple => resultSql = resultSql.replace(tuple._1, tuple._2))
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~sql after replace")
      println(resultSql)
      resultSql
    })

  }


  private def getReplaceStr(parsedMap: mutable.Map[String, (SqlOperators, List[String])], kvMap: mutable.HashMap[String, List[String]]): mutable.Map[String, String] = {
    val replaceMap = mutable.Map.empty[String, String]
    parsedMap.foreach(tuple => {
      val (expr, (op, expressionList)) = tuple
      val (left, right) = (expressionList.head, expressionList.last)
      val davinciVar = right.substring(right.indexOf('$') + 1, right.lastIndexOf('$')).trim
      if (kvMap.contains(davinciVar)) {
        val values = kvMap(davinciVar).map(v => s"'$v'")
        val refactorExprWithOr =
          if (values.size > 1) values.map(v => s"$left ${op.toString} '$v'").mkString("(", "OR", ")")
          else s"$left ${op.toString} ${values.mkString("")}"
        val replaceStr = op match {
          case EQUALSTO =>
            if (values.size > 1) s"$left ${IN.toString} ${values.mkString("(", ",", ")")}"
            else s"$left ${op.toString} ${values.mkString("")}"
          case NOTEQUALSTO =>
            if (values.size > 1) s"$left ${NoTIN.toString} ${values.mkString("(", ",", ")")}"
            else s"$left ${op.toString} ${values.mkString("")}"
          case BETWEEN =>
            if (values.size > 1) s"$left ${IN.toString} ${values.mkString("(", ",", ")")}"
            else s"$left ${op.toString} ${values.mkString("")}"
          case IN => s"$left ${op.toString} ${values.mkString("(", ",", ")")}"
          case GREATERTHAN => refactorExprWithOr
          case GREATERTHANEQUALS => refactorExprWithOr
          case MINORTHAN => refactorExprWithOr
          case MINORTHANEQUALS => refactorExprWithOr
          case _ => ""
        }
        replaceMap(expr) = replaceStr
      }
    })
    replaceMap
  }
}
