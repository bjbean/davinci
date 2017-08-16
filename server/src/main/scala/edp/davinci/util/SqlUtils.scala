package edp.davinci.util

import java.io.ByteArrayOutputStream
import java.sql.{Connection, ResultSet, Statement}

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import edp.davinci.DavinciConstants._
import edp.davinci.KV
import edp.davinci.csv.CSVWriter
import org.apache.log4j.Logger
import org.clapper.scalasti.{Constants, STGroupFile}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object SqlUtils extends SqlUtils

trait SqlUtils extends Serializable {
  lazy val dataSourceMap: mutable.HashMap[String, HikariDataSource] = new mutable.HashMap[String, HikariDataSource]
  lazy val sqlRegex = "\\([^\\$]*\\$\\w+\\$\\s?\\)"
  private lazy val logger = Logger.getLogger(this.getClass)

  def getConnection(jdbcUrl: String, username: String, password: String, maxPoolSize: Int = 5): Connection = {
    val tmpJdbcUrl = jdbcUrl.toLowerCase
    if (!dataSourceMap.contains(tmpJdbcUrl) || dataSourceMap(tmpJdbcUrl) == null) {
      synchronized {
        if (!dataSourceMap.contains(tmpJdbcUrl) || dataSourceMap(tmpJdbcUrl) == null) {
          initJdbc(jdbcUrl, username, password, maxPoolSize)
        }
      }
    }
    dataSourceMap(tmpJdbcUrl).getConnection
  }

  private def initJdbc(jdbcUrl: String, username: String, password: String, muxPoolSize: Int = 5): Unit = {
    println(jdbcUrl)
    val config = new HikariConfig()
    val tmpJdbcUrl = jdbcUrl.toLowerCase
    if (tmpJdbcUrl.indexOf("mysql") > -1) {
      println("mysql")
      config.setConnectionTestQuery("SELECT 1")
      config.setDriverClassName("com.mysql.jdbc.Driver")
    } else if (tmpJdbcUrl.indexOf("oracle") > -1) {
      println("oracle")
      config.setConnectionTestQuery("SELECT 1 from dual ")
      config.setDriverClassName("oracle.jdbc.driver.OracleDriver")
    } else if (tmpJdbcUrl.indexOf("sqlserver") > -1) {
      println("sqlserver")
      config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
    } else if (tmpJdbcUrl.indexOf("h2") > -1) {
      println("h2")
      config.setDriverClassName("org.h2.Driver")
    } else if (tmpJdbcUrl.indexOf("phoenix") > -1) {
      println("hbase phoenix")
      config.setDriverClassName("org.apache.phoenix.jdbc.PhoenixDriver")
    } else if (tmpJdbcUrl.indexOf("cassandra") > -1) {
      println("cassandra")
      config.setDriverClassName("com.github.adejanovski.cassandra.jdbc.CassandraDriver")
    } else if (tmpJdbcUrl.indexOf("mongodb") > -1) {
      println("mongodb")
      config.setDriverClassName("mongodb.jdbc.MongoDriver")
    } else if (tmpJdbcUrl.indexOf("sql4es") > -1) {
      println("elasticSearch")
      config.setDriverClassName("nl.anchormen.sql4es.jdbc.ESDriver")
    }

    config.setUsername(username)
    config.setPassword(password)
    config.setJdbcUrl(jdbcUrl)
    config.setMaximumPoolSize(muxPoolSize)
    config.setMinimumIdle(1)

    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

    val ds: HikariDataSource = new HikariDataSource(config)
    println(tmpJdbcUrl + "$$$$$$$$$$$$$$$$$" + ds.getUsername + " " + ds.getPassword)
    dataSourceMap(tmpJdbcUrl) = ds
  }

  def resetConnection(jdbcUrl: String, username: String, password: String): Unit = {
    shutdownConnection(jdbcUrl.toLowerCase)
    getConnection(jdbcUrl, username, password).close()
  }

  def shutdownConnection(jdbcUrl: String): dataSourceMap.type = {
    val tmpJdbcUrl = jdbcUrl.toLowerCase
    dataSourceMap(tmpJdbcUrl).close()
    dataSourceMap -= tmpJdbcUrl
  }


  def sqlExecute(filters: String,
                 flatTableSqls: String,
                 tableName: String,
                 adHocSql: String,
                 paginateAndSort: String,
                 connectionUrl: String,
                 paramSeq: Seq[KV] = null,
                 groupParams: Seq[KV] = null): (ListBuffer[Seq[String]], Long) = {
    var totalCount: Long = 0
    val trimSql = flatTableSqls.trim
    logger.info(trimSql + "~~~~~~~~~~~~~~~~~~~~~~~~~sqlTemp")
    val sqls = if (trimSql.lastIndexOf(sqlSeparator) == trimSql.length - 1) trimSql.dropRight(1).split(sqlSeparator) else trimSql.split(sqlSeparator)
    val sqlWithoutVar = sqls.filter(!_.contains("dv_"))
    val groupKVMap = getGroupKVMap(sqls, groupParams)
    val queryKVMap = getQueryKVMap(sqls, paramSeq)
    val resetSqlBuffer = if (groupKVMap.nonEmpty || queryKVMap.nonEmpty)
      RegexMatcher.matchAndReplace(sqlWithoutVar, groupKVMap, queryKVMap).toBuffer else sqlWithoutVar.toBuffer
    resetSqlBuffer.foreach(logger.info)
    val projectSql = getProjectSql(resetSqlBuffer.last, filters, tableName, adHocSql, paginateAndSort)
    logger.info(projectSql + "^^^^^^^^^^^^^^^^^^^^^^projectSql")
    resetSqlBuffer.remove(resetSqlBuffer.length - 1)
    resetSqlBuffer.append(projectSql.split(sqlSeparator).head)
    val resultSql = resetSqlBuffer.toArray
    val countNum = getResult(connectionUrl, Array(projectSql.split(sqlSeparator).last))
    totalCount = countNum.last.last.toLong
    (getResult(connectionUrl, resultSql), totalCount)
  }


  def getGroupKVMap(sqlArr: Array[String], groupParams: Seq[KV]): mutable.HashMap[String, List[String]] = {
    val defaultVars = sqlArr.filter(_.contains("dv_group"))
    val groupKVMap = mutable.HashMap.empty[String, List[String]]
    if (null != groupParams && groupParams.nonEmpty)
      groupParams.foreach(group => {
        val (k, v) = (group.k, group.v)
        if (groupKVMap.contains(k)) groupKVMap(k) = groupKVMap(k) ::: List(v) else groupKVMap(k) = List(v)
      })
    if (defaultVars.nonEmpty)
      defaultVars.foreach(g => {
        val k = g.substring(g.indexOf('$') + 1, g.lastIndexOf('$')).trim
        val v = g.substring(g.indexOf("=") + 1).trim
        if (!groupKVMap.contains(k))
          groupKVMap(k) = List(v)
      })
    groupKVMap
  }

  def getQueryKVMap(sqlArr: Array[String], paramSeq: Seq[KV]): mutable.HashMap[String, String] = {
    val defaultVars = sqlArr.filter(_.contains("dv_query"))
    val queryKVMap = mutable.HashMap.empty[String, String]
    if (null != paramSeq && paramSeq.nonEmpty) paramSeq.foreach(param => queryKVMap(param.k) = param.v)
    if (defaultVars.nonEmpty)
      defaultVars.foreach(g => {
        val k = g.substring(g.indexOf('$') + 1, g.lastIndexOf('$')).trim
        val v = g.substring(g.indexOf("=") + 1).trim
        if (!queryKVMap.contains(k))
          queryKVMap(k) = v
      })
    queryKVMap
  }

  def getResult(connectionUrl: String, sql: Array[String]): ListBuffer[Seq[String]] = {
    logger.info("the sql in getResult:")
    sql.foreach(logger.info)
    val resultList = new ListBuffer[Seq[String]]
    val columnList = new ListBuffer[String]
    var dbConnection: Connection = null
    var statement: Statement = null
    if (connectionUrl != null) {
      val connectionInfo = connectionUrl.split(sqlUrlSeparator)
        .filter(u => u.contains("user") || u.contains("password")).map(u => u.substring(u.indexOf('=') + 1))
      if (connectionInfo.length != 2) {
        logger.info("connection is not in right format")
        throw new Exception("connection is not in right format:" + connectionUrl)
      } else {
        try {
          dbConnection = SqlUtils.getConnection(connectionUrl, connectionInfo(0), connectionInfo(1))
          statement = dbConnection.createStatement()
          if (sql.length > 1) for (elem <- sql.dropRight(1)) statement.execute(elem)
          val resultSet = statement.executeQuery(sql.last)
          val meta = resultSet.getMetaData
          for (i <- 1 to meta.getColumnCount) columnList.append(meta.getColumnLabel(i) + ":" + meta.getColumnTypeName(i))
          resultList.append(columnList)
          while (resultSet.next()) resultList.append(getRow(resultSet))
          resultList
        } catch {
          case e: Throwable => logger.error("get result exception", e)
            throw e
        } finally {
          if (statement != null) statement.close()
          if (dbConnection != null) dbConnection.close()
        }
      }
    } else {
      logger.info("connection is not given or is null")
      ListBuffer(Seq(""))
    }
  }

  /**
    *
    * @param row a row in DB represent by string
    * @return a CSV String
    */
  def covert2CSV(row: Seq[String]): String = {
    val byteArrOS = new ByteArrayOutputStream()
    val writer = CSVWriter.open(byteArrOS)
    writer.writeRow(row)
    val CSVStr = byteArrOS.toString(defaultEncode)
    byteArrOS.close()
    writer.close()
    CSVStr
  }


  def getHTMLStr(resultList: ListBuffer[Seq[String]], stgPath: String = "stg/tmpl.stg"): String = {
    println(resultList.head.toBuffer + "~~~~~~~~~~~~~~~~~~~~~table head before map")
    val columns = resultList.head.map(c => c.split(CSVHeaderSeparator).head)
    println(columns.toBuffer + "~~~~~~~~~~~~~~~~~~~~~table head after map")
    resultList.remove(0)
    resultList.prepend(columns)
    resultList.prepend(Seq(""))
    val noNullResult = resultList.map(seq => seq.map(s => if (null == s) "" else s))
    val tables = Seq(noNullResult)
    STGroupFile(stgPath, Constants.DefaultEncoding, '$', '$').instanceOf("email_html")
      .map(_.add("tables", tables).render().get)
      .recover {
        case e: Exception =>
          logger.info("render exception ", e)
          s"ST Error: $e"
      }.getOrElse("")
  }

  /**
    *
    * @param querySql a SQL string; eg. SELECT * FROM Table
    * @return SQL string mixing AdHoc SQL
    */

  def getProjectSql(querySql: String, filters: String, tableName: String, adHocSql: String, paginateStr: String = ""): String = {
    logger.info(querySql + "~~~~~~~~~~~~~~~~~~~~~~~~~the initial project sql")
    val projectSqlWithFilter = if (null != filters && filters != "") s"SELECT * FROM ($querySql) AS PROFILTER WHERE $filters" else querySql
    val mixinSql = if (null != adHocSql && adHocSql.trim != "{}" && adHocSql.trim != "") {
      try {
        val sqlArr = adHocSql.toLowerCase.split(flatTable)
        if (sqlArr.size == 2) sqlArr(0) + s" ($projectSqlWithFilter) as `$tableName` ${sqlArr(1)}" else sqlArr(0) + s" ($projectSqlWithFilter) as `$tableName`"
      } catch {
        case e: Throwable => logger.error("adHoc sql is not in right format", e)
          throw e
      }
    } else {
      logger.info("adHoc sql is empty")
      projectSqlWithFilter
    }
    if (paginateStr != "")
      s"SELECT * FROM ($mixinSql) AS PAGINATE $paginateStr" + s";SELECT COUNT(*) FROM ($mixinSql) AS COUNTSQL"
    else
      mixinSql + s";SELECT COUNT(*) FROM ($mixinSql) AS COUNTSQL"
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
      if (fieldValue == null) null.asInstanceOf[String] else fieldValue.toString
    })
  }

}
