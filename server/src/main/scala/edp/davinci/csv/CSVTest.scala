package edp.davinci.csv

import org.apache.commons.io.output.ByteArrayOutputStream


object CSVTest extends App{
val baos = new ByteArrayOutputStream()
  val writer = CSVWriter.open(baos)
  val a = Seq("a","b b  ","c/ \"\\cc")
  val w = writer.writeRow(a)
  println(baos.toString("UTF-8"))
}
