package edp.davinci.util

import java.io.ByteArrayOutputStream
import java.sql.{Connection, ResultSet, Statement}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import edp.davinci.csv.CSVWriter
import org.slf4j.LoggerFactory
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object SqlUtils extends SqlUtils

trait SqlUtils extends Serializable {
  lazy val datasourceMap: mutable.HashMap[String, HikariDataSource] = new mutable.HashMap[String, HikariDataSource]
  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val adHocTable = "flatTable".toUpperCase
  private lazy val sqlSeparator = ";"
  private lazy val sqlUrlSeparator = "<:>"
  private lazy val defaultEncode = "UTF-8"


  def getConnection(jdbcUrl: String, username: String, password: String, maxPoolSize: Int = 5): Connection = {
    val tmpJdbcUrl = jdbcUrl.toLowerCase
    if (!datasourceMap.contains(tmpJdbcUrl) || datasourceMap(tmpJdbcUrl) == null) {
      synchronized {
        if (!datasourceMap.contains(tmpJdbcUrl) || datasourceMap(tmpJdbcUrl) == null) {
          initJdbc(jdbcUrl, username, password, maxPoolSize)
        }
      }
    }
    datasourceMap(tmpJdbcUrl).getConnection
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
    // config.setConnectionTestQuery("SELECT 1")

    config.addDataSourceProperty("cachePrepStmts", "true")
    config.addDataSourceProperty("prepStmtCacheSize", "250")
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

    val ds: HikariDataSource = new HikariDataSource(config)
    println(tmpJdbcUrl + "$$$$$$$$$$$$$$$$$" + ds.getUsername + " " + ds.getPassword)
    datasourceMap(tmpJdbcUrl) = ds
  }

  def resetConnection(jdbcUrl: String, username: String, password: String): Unit = {
    shutdownConnection(jdbcUrl.toLowerCase)
    //    datasourceMap -= jdbcUrl
    getConnection(jdbcUrl, username, password).close()
  }

  def shutdownConnection(jdbcUrl: String): datasourceMap.type = {
    val tmpJdbcUrl = jdbcUrl.toLowerCase
    datasourceMap(tmpJdbcUrl).close()
    datasourceMap -= tmpJdbcUrl
  }


  def sqlExecute(filters: String, sqlTemp: String, tableName: String, adHocSql: String, paginateStr: String, connectionUrl: String): (ListBuffer[Seq[String]], Long) = {
    val resultList = mutable.ListBuffer.empty[Seq[String]]
    var count = 1
    var totalCount: Long = 0
    val resetSqlBuffer = sqlTemp.split(sqlSeparator).toBuffer
    val projectSql = getProjectSql(resetSqlBuffer.last, filters, tableName, adHocSql, paginateStr)
    resetSqlBuffer.remove(resetSqlBuffer.length - 1)
    resetSqlBuffer.append(projectSql.split(sqlSeparator).head)
    val resultSql = resetSqlBuffer.toArray
    val countNum = getResult(connectionUrl, Array(projectSql.split(sqlSeparator).last))
    if (countNum.size > 1)
      totalCount = countNum.last.last.toLong
    if (null != resultSql) {
      if (count > 1)
        getResult(connectionUrl, resultSql).drop(1).copyToBuffer(resultList)
      else
        getResult(connectionUrl, resultSql).copyToBuffer(resultList)
      count += 1
    }
    (resultList, totalCount)
  }


  def getResultAndTotal(connectionUrl: String, resultSql: Array[String]): (ListBuffer[Seq[String]], Long) = {
    val resultList = mutable.ListBuffer.empty[Seq[String]]
    val countNum = getResult(connectionUrl, Array(resultSql.last))
    var totalCount: Long = 0
    var count = 1
    if (countNum.size > 1)
      totalCount = countNum.last.last.toLong
    if (null != resultSql) {
      if (count > 1)
        getResult(connectionUrl, resultSql.dropRight(1)).drop(1).copyToBuffer(resultList)
      else
        getResult(connectionUrl, resultSql.dropRight(1)).copyToBuffer(resultList)
      count += 1
    }
    (resultList, totalCount)
  }


  def getResult(connectionUrl: String, sql: Array[String]): ListBuffer[Seq[String]] = {
    val resultList = new ListBuffer[Seq[String]]
    val columnList = new ListBuffer[String]
    var dbConnection: Connection = null
    var statement: Statement = null
    if (connectionUrl != null) {
      val connectionInfo = connectionUrl.split(sqlUrlSeparator)
      if (connectionInfo.size != 3) {
        logger.info("connection is not in right format")
        throw new Exception("connection is not in right format:" + connectionUrl)
      }
      else {
        try {
          dbConnection = SqlUtils.getConnection(connectionInfo(0), connectionInfo(1), connectionInfo(2))
          statement = dbConnection.createStatement()
          if (sql.length > 1)
            for (elem <- sql.dropRight(1)) statement.execute(elem)
          val resultSet = statement.executeQuery(sql.last)
          val meta = resultSet.getMetaData
          for (i <- 1 to meta.getColumnCount)
            columnList.append(meta.getColumnName(i) + ":" + meta.getColumnTypeName(i))
          resultList.append(columnList)
          while (resultSet.next())
            resultList.append(getRow(resultSet))
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

  /**
    *
    * @param projectSql a SQL string; eg. SELECT * FROM Table
    * @return SQL string mixing AdHoc SQL
    */

  def getProjectSql(projectSql: String, filters: String, tableName: String, adHocSql: String, paginateStr: String = ""): String = {
    val projectSqlWithFilter = if (null != filters && filters != "")
      s"SELECT * FROM ($projectSql) AS PROFILTER WHERE $filters"
    else
      projectSql
    val mixinSql = if (adHocSql != "{}") {
      try {
        val sqlArr = adHocSql.toUpperCase.split(adHocTable)
        if (sqlArr.size == 2) sqlArr(0) + s" ($projectSqlWithFilter) as `$tableName` ${sqlArr(1)}"
        else sqlArr(0) + s" ($projectSqlWithFilter) as `$tableName`"
      } catch {
        case e: Throwable => logger.error("adHoc sql is not in right format", e)
          throw e
      }
    } else {
      logger.info("adHoc sql is empty")
      projectSqlWithFilter
    }
    if (paginateStr!= "")
    s"SELECT * FROM ($mixinSql) AS PAGINATE $paginateStr" + s";SELECT COUNT(1) FROM ($mixinSql) AS COUNTSQL"
    else
     mixinSql + s";SELECT COUNT(1) FROM ($mixinSql) AS COUNTSQL"
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


}
