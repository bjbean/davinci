package edp.davinci.util

import java.io.ByteArrayOutputStream
import java.sql.{Connection, ResultSet, Statement}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import edp.davinci.csv.CSVWriter
import org.slf4j.LoggerFactory
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


object SqlProcessor extends SqlProcessor

trait SqlProcessor extends Serializable {
  lazy val datasourceMap: mutable.HashMap[String, HikariDataSource] = new mutable.HashMap[String, HikariDataSource]
  private val logger = LoggerFactory.getLogger(this.getClass)
  private lazy val adHocTable = "table"
  private lazy val semiSeparator = ";"
  private lazy val urlSep = "<:>"
  private lazy val defaultEncode = "UTF-8"
  private lazy val paramSep = "\\?"

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


  def sqlExecute(sqlParam:Seq[String],sqlTemp:String,tableName:String,adHocSql:String,paginateStr:String,connectionUrl:String): (ListBuffer[String], Int) ={
    val resultList = mutable.ListBuffer.empty[String]
    var count = 1
    var totalCount = 0
    sqlParam.foreach(param => {
      val resultSql = getSqlArr(sqlTemp, param, tableName, adHocSql, paginateStr)
      val countNum = getResult(connectionUrl, Array(resultSql.last))
      if (countNum.size > 1)
        totalCount = countNum.last.toInt
      if (null != resultSql) {
        if (count > 1)
          getResult(connectionUrl, resultSql.dropRight(1)).drop(1).copyToBuffer(resultList)
        else
          getResult(connectionUrl, resultSql.dropRight(1)).copyToBuffer(resultList)
        count += 1
      }
    })
    (resultList,totalCount)
  }


  def getResult(connectionUrl: String, sql: Array[String]): List[String] = {
    val resultList = new ListBuffer[String]
    val columnList = new ListBuffer[String]
    var dbConnection: Connection = null
    var statement: Statement = null
    if (connectionUrl != null) {
      val connectionInfo = connectionUrl.split(urlSep)
      if (connectionInfo.size != 3) {
        logger.info("connection is not in right format")
        throw new Exception("connection is not in right format:" + connectionUrl)
      }
      else {
        try {
          dbConnection = SqlProcessor.getConnection(connectionInfo(0), connectionInfo(1), connectionInfo(2))
          statement = dbConnection.createStatement()
          if (sql.length > 1)
            for (elem <- sql.dropRight(1)) statement.execute(elem)
          val resultSet = statement.executeQuery(sql.last)
          val meta = resultSet.getMetaData
          for (i <- 1 to meta.getColumnCount)
            columnList.append(meta.getColumnName(i) + ":" + meta.getColumnTypeName(i))
          resultList.append(covert2CSV(columnList))
          while (resultSet.next())
            resultList.append(covert2CSV(getRow(resultSet)))
          resultList.toList
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
      List("")
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

  def getSqlArr(sqlTemp: String, param: String, tableName: String, adHocSql: String, paginateStr: String=""): Array[String] = {
    /**
      *
      * @param projectSql a SQL string; eg. SELECT * FROM Table
      * @return SQL string mixing AdHoc SQL
      */

    def mixinAdHocSql(projectSql: String) = {
      val mixinSql = if (adHocSql != "{}") {
        try {
          val sqlArr = adHocSql.split(adHocTable)
          if (sqlArr.size == 2) sqlArr(0) + s" ($projectSql) as `$tableName` ${sqlArr(1)}"
          else sqlArr(0) + s" ($projectSql) as `$tableName`"
        } catch {
          case e: Throwable => logger.error("adHoc sql is not in right format", e)
            throw e
        }
      } else {
        logger.info("adHoc sql is empty")
        projectSql
      }
      s"SELECT * FROM ($mixinSql) AS PAGINATE $paginateStr" + s";SELECT COUNT(1) FROM ($mixinSql) AS COUNTSQL"
    }


    if (sqlTemp != "") {
      var sql = sqlTemp.trim
      logger.info("the initial sql template:" + sqlTemp)

      val paramArr = param.split(paramSep)
      paramArr.foreach(p => sql = sql.replaceFirst(paramSep, s"'$p'"))
      logger.info("sql template after the replacement:" + sql)

      val semiIndex = sql.lastIndexOf(semiSeparator)
      val allSqlStr: String =
        if (semiIndex < 0)
          mixinAdHocSql(sql)
        else {
          if (semiIndex == sql.length - 1) {
            if (sql.substring(0, semiIndex).lastIndexOf(semiSeparator) < 0) {
              logger.info("only the last char is semicolon")
              mixinAdHocSql(sql.substring(0, semiIndex))
            }
            else {
              val lastIndex = sql.substring(0, semiIndex).lastIndexOf(semiSeparator)
              logger.info("has the second last semicolon")
              val mixinSql = mixinAdHocSql(sql.substring(lastIndex + 1, semiIndex))
              sql.substring(0, semiIndex + 1) + mixinSql
            }
          } else {
            val mixinSql = mixinAdHocSql(sql.substring(semiIndex + 1))
            sql.substring(0, semiIndex + 1) + mixinSql
          }
        }
      logger.info(allSqlStr + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
      allSqlStr.split(semiSeparator)
    } else {
      logger.info("there is no sql template")
      null.asInstanceOf[Array[String]]
    }

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
