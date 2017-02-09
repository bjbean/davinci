package edp.davinci.module

//import org.apache.spark.SparkConf
//import org.apache.spark.sql.{MbSession, SparkSession}

trait CalculationModule {
//  val mb: SparkSession
}

trait CalculationModuleImpl extends CalculationModule {
//  this: ConfigurationModule =>
//
//  override implicit val mb: MbSession = {
//    val sparkConf = {
//      val sc = new SparkConf()
//      sc.setMaster(config.getString("sparklocal.master"))
//      sc.setAppName(config.getString("sparklocal.appName"))
//      sc
//    }
//
//    sparkConf.set("spark.sql.warehouse.dir", "file:///edp/wormhole/target/tmp/spark/warehouse/")
//
//    MbSession.builder.config(sparkConf).getOrCreate()
//  }
}
