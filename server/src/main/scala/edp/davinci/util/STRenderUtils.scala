package edp.davinci.util

import edp.davinci.DavinciConstants.CSVHeaderSeparator
import org.apache.log4j.Logger
import org.clapper.scalasti.{Constants, STGroupFile}
import org.stringtemplate.v4.STGroupString
import edp.davinci.DavinciConstants._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object STRenderUtils extends STRenderUtils

trait STRenderUtils {
  private lazy val logger = Logger.getLogger(this.getClass)

  def getHTMLStr(resultList: ListBuffer[Seq[String]], stgPath: String = "stg/tmpl.stg"): String = {
    val columns = resultList.head.map(c => c.split(CSVHeaderSeparator).head)
    resultList.remove(0)
    resultList.prepend(columns)
    resultList.prepend(Seq(""))
    val noNullResult = resultList.map(seq => seq.map(s => if (null == s) "" else s))
    val tables = Seq(noNullResult)
    STGroupFile(stgPath, Constants.DefaultEncoding, dollarDelimiter, dollarDelimiter).instanceOf("email_html")
      .map(_.add("tables", tables).render().get)
      .recover { case e: Exception => logger.info("render exception ", e)
        s"ST Error: $e"
      }.getOrElse("")
  }

  def renderSql(sqlWithoutVars: String, queryKVMap: mutable.HashMap[String, String]): String = {
    val queryVars = queryKVMap.keySet.mkString("(", ",", ")")
    val sqlToRender = sqlWithoutVars.replaceAll("<>", "!=")
    val sqlTemplate =
      """delimiters "$", "$"""" +
        s"""sqlTemplate$queryVars ::= <<
           |$sqlToRender
           |>>""".stripMargin
    logger.info("~~~~~~~~~~~~~~~~~~~~~~~~sql template after merge:\n" + sqlTemplate)
    val stg: STGroupString = new STGroupString(sqlTemplate)
    val sqlST = stg.getInstanceOf("sqlTemplate")
    queryKVMap.foreach(kv => sqlST.add(kv._1, kv._2))
    sqlST.render()
  }

}
