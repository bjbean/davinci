package edp.davinci.scheduler

import akka.actor.{ActorSystem, Props}
import com.markatta.akron._
object ESScheduler extends App{


  val system = ActorSystem("system")

  val crontab = system.actorOf(CronTab.props, "crontab")

  val someOtherActor = system.actorOf(Props[HelloActor], "etc")

  // send woo to someOtherActor once every minute
  crontab ! CronTab.Schedule(someOtherActor, "hello", CronExpression("* * * * *"))

  // there is also a type safe DSL for the expressions
//  import DSL._
//  crontab ! CronTab.Schedule(
//    someOtherActor,
//    "wee",
//    CronExpression(20, *, (mon, tue, wed), (feb, oct), *))


}
