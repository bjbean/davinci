package edp.davinci.csv

import org.apache.commons.io.output.ByteArrayOutputStream


object CSVTest extends App {
  val baos = new ByteArrayOutputStream()
  val writer = CSVWriter.open(baos)
  val a = List(Seq("a", "b b  ", "c/ \"\\cc"), Seq("a", "b b  ", "casfa,", ""))
  val CSVArr = a.map(s => {
    writer.writeRow(s)
    val csv=baos.toString("UTF-8")
    baos.reset()
    csv
  })
  CSVArr.foreach(println)
  writer.close()
  baos.close()

  //  val w = writer.writeRow(a)
  //  println(baos.toString("UTF-8"))
}
