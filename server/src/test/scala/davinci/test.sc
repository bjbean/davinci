import scala.reflect.ClassTag

trait test

case class test1(id: Int) extends test

case class test2[A](payload: A)

//test2[test1]

test2[test1](test1(1))

val clazz = classOf[test1]

test2(test1(1))

