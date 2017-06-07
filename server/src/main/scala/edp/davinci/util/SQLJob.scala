//package edp.davinci.util
//
//import java.util.{Scanner, UUID}
//
//import edp.moonbox.grid.client.AkkaDriver
//import edp.moonbox.grid.common.{DriverUtils, FailureReason}
//import scala.collection.immutable.List
//import scala.util.{Failure, Success}
//import scala.concurrent.duration._
//
//object SQLJob {
///*
//mount table aaa options(type'mysql', url 'jdbc:mysql://10.100.30.220:3306/test', dbname 'test', dbtable 'test1_10w', user 'root', password '123456')
//
//select count(*) from aaa
// */
//  val driver: AkkaDriver = new AkkaDriver("")
//  val tenantId = UUID.randomUUID().toString
//  val questId = UUID.randomUUID().toString
//  def AdHocSql() = {
//
//
//
//    driver.adhocOpenSession(tenantId, questId)
//    val scan = new Scanner(System.in)
//
//    var end = false
//    while (!end) {
//      print(">>> ")
//      val sql = scan.nextLine()
//      if (sql == "exit" || sql == "quit") end = true
//      else if (null != sql && "" != sql) {
//        val cacheValue = execute(List(sql))
//        if (cacheValue != null) {
//          println(cacheValue._1)
//          val iter = cacheValue._2
//          while (iter.hasNext) {
//            val item: (String, String) = iter.next()
//            println(s"k: ${item._1}  v: ${item._2}")
//          }
//        }
//      }
//      Thread.sleep(100)
//    }
//
//    driver.adhocCloseSession(tenantId, questId)
//    System.exit(0)
//
//  }
//
//  private def execute(sql: List[String]): (String, Iterator[(String, String)]) = {
//    val futures = driver.adhocSql(tenantId, questId, sql)
//    try {
//      println(DriverUtils.getReqId(futures._1))
//      DriverUtils.awaitResult(futures._1, futures._2, 30.seconds)
//    } catch {
//      case e: FailureReason =>
//        println("111===> " + e.toErrorString)
//        null
//      case e: Throwable =>
//        println("222===> " + e.getMessage)
//        null
//    }
//
//  }
//
//
//  def batchSql() = {
//    val driver: AkkaDriver = new AkkaDriver("")
//    while (true) {
//
//      //val ret: (Future[Response], Future[(String, Iterator[(String, String)])]) = driver.batchSql("aaa", "bbb", List("select * from test"))
//      val (f1, f2) = driver.batchSql("234324", "65633", List(args.tail.tail: _*))
//
//      f1 onComplete {
//        case Success(reqId) =>
//          println("job submit succeed, req id:  " + reqId)
//
//          f2 onComplete {
//            case Success(cacheValue) =>
//
//              println("cache key succeed, schema: " + cacheValue._1)
//              val iter: Iterator[(String, String)] = cacheValue._2
//              while (iter.hasNext) {
//                val item: (String, String) = iter.next()
//                println(s"k: ${item._1}  v: ${item._2}")
//
//              }
//            case Failure(throwable) =>
//              println("catch exception, get req id failed " + throwable.printStackTrace())
//          }
//
//        case Failure(throwable) =>
//          println("catch exception when sends query1 " + throwable.printStackTrace())
//      }
//
//      Thread.sleep("".toInt)
//
//    }
//  }
//
//}
