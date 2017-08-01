package davinci

import edp.davinci.DavinciConstants.sqlSeparator
import edp.davinci.KV
import edp.davinci.util.{RegexMatcher, SqlParser}
import org.scalatest.FunSuite
import edp.davinci.util.JsonUtils.json2caseClass
import edp.davinci.util.SqlUtils._
import scala.collection.mutable


class MatchAndReplace extends FunSuite {
  ignore("expression map") {
    val expressionList = List("name = $v1$", "city in ('beijing','shanghai')", "age >=10", "sex != '男'", "age < 20")
    val expressionMap = SqlParser.getParsedMap(expressionList)
    expressionMap.foreach(e => {
      println(e._2._1)
      println(e._1)
      e._2._2.foreach(println)
      println("~~~~~~~~~~~~~~~~~~~~~~~~~~~")
    })
    assert("" == "")
  }

  test("get expression list") {
    val str = "Is is the cost of of gasoline going up up where ((name_) = $v1_$) and (city =$v2$) and (age > $v3$) or sex != '男'"
    val regex = "\\([^\\$]*\\$\\w+\\$\\s?\\)"
    val expressionList = RegexMatcher.getMatchedItemList(str, regex)
    expressionList.foreach(println)
    val exprList = List("((name_) = $v1_$)", "(city =$v2$)", "(age > $v3$)")
    assert(exprList == expressionList, "this is right what i want")
  }

  test("match all") {
    val groupStr = "[{\"k\":\"v2\",\"v\":\"北京\"},{\"k\":\"v3\",\"v\":\"20\"}]"
    val queryStr = "[{\"k\":\"v2\",\"v\":\"shanghai\"},{\"k\":\"v3\",\"v\":\"30\"}]"
    val flatTableSqls = "dv_groupvar @$v1_$ = mary;" +
      "dv_groupvar @$v2$ = beijing;" +
      "dv_groupvar @$v3$ = 20;" +
      "select name,age,city from davinci where ((name_) =  $v1_$) and (city =$v2$) and (age > $v3$) or sex != '男' "
    val groupParams = json2caseClass[Seq[KV]](groupStr)
    val queryParams = json2caseClass[Seq[KV]](queryStr)

    val trimSql = flatTableSqls.trim
    val sqls = if (trimSql.lastIndexOf(sqlSeparator) == trimSql.length - 1) trimSql.dropRight(1).split(sqlSeparator) else trimSql.split(sqlSeparator)
    val sqlWithoutVar = sqls.filter(!_.contains("dv_"))
    val groupKVMap = getGroupKVMap(sqls, groupParams)
    val queryKVMap = getQueryKVMap(sqls, queryParams)
     RegexMatcher.matchAndReplace(sqlWithoutVar, groupKVMap, queryKVMap).toBuffer.foreach(println)
  }

  test("get all the parameters") {
    val sqls = "dv_groupvar @$v1_$ = mary;" +
      "dv_groupvar @$v2$ = beijing;" +
      "dv_groupvar @$v3$ = 20;" +
      "select name,age,city from davinci where ((name_) =  $v1_$) and (city =$v2$) and (age > $v3$) or sex != '男' "
    val sqlList = sqls.split(";")
    val groupVars = sqlList.filter(_.contains("dv_groupvar"))
    val groupMap = mutable.HashMap.empty[String, List[String]]
    groupVars.foreach(g => {
      val k = g.substring(g.indexOf('$') + 1, g.lastIndexOf('$')).trim
      val v = g.substring(g.indexOf("=") + 1).trim
      groupMap(k) = List(v)
    })
    groupMap.foreach(println)
    val toMatchSql = sqlList.filter(!_.contains("dv_groupvar"))
    val sqlRegex = "\\([^\\$]*\\$\\w+\\$\\s?\\)"
    //    RegexMatcher.matchAndReplace(toMatchSql,,groupMap)
  }


}
