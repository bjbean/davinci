package edp.davinci.util

import java.util.regex.Pattern

import edp.davinci.util.SqlOperators._
import org.apache.log4j.Logger
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RegexMatcher extends RegexMatcher

trait RegexMatcher {
  lazy val groupRegex = "\\([^\\$,]*\\$\\w+\\$\\s?\\)"
  lazy val queryRegex = "\\$\\s*\\w+\\s*\\$"
  private lazy val logger = Logger.getLogger(this.getClass)

  def getMatchedItemList(sqlStr: String, REGEX: String): List[String] = {
    val listBuffer = ListBuffer.empty[String]
    val pattern = Pattern.compile(REGEX)
    val matcher = pattern.matcher(sqlStr)
    while (matcher.find())
      listBuffer.append(matcher.group())
    listBuffer.toList
  }


  def matchAndReplace(sqlList: Array[String], groupKVMap: mutable.HashMap[String, List[String]], queryKVMap: mutable.HashMap[String, String]): Array[String] = {
    sqlList.map(sql => {
      val exprList = getMatchedItemList(sql, groupRegex)
      val parsedMap = SqlParser.getParsedMap(exprList)
      var resultSql = sql
      if (groupKVMap.nonEmpty) {
        val replaceMap = getGroupReplaceStr(parsedMap, groupKVMap)
        logger.info(s"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~sql before merge\n$sql")
        replaceMap.foreach(tuple => resultSql = resultSql.replace(tuple._1, tuple._2))
        logger.info(s"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~sql after merge\n$resultSql")
      }
      val matchItemList = getMatchedItemList(resultSql, queryRegex)
      val itemTupleList: List[(String, String)] = matchItemList.map(item => (item, item.substring(item.indexOf('$') + 1, item.lastIndexOf('$')).trim))
      if (queryKVMap.nonEmpty) {
        itemTupleList.foreach(tuple => if(queryKVMap.contains(tuple._2)) resultSql = resultSql.replace(tuple._1, queryKVMap(tuple._2)))
        logger.info(s"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~sql after replace\n$resultSql")
      }
      resultSql
    })

  }


  private def getGroupReplaceStr(parsedMap: mutable.Map[String, (SqlOperators, List[String])], kvMap: mutable.HashMap[String, List[String]]): mutable.Map[String, String] = {
    val replaceMap = mutable.Map.empty[String, String]
    parsedMap.foreach(tuple => {
      val (expr, (op, expressionList)) = tuple
      val (left, right) = (expressionList.head, expressionList.last)
      val davinciVar = right.substring(right.indexOf('$') + 1, right.lastIndexOf('$')).trim
      if (kvMap.contains(davinciVar)) {
        val values = kvMap(davinciVar)
        val refactorExprWithOr =
          if (values.size > 1) values.map(v => s"$left ${op.toString} $v").mkString("(", " OR ", ")")
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
