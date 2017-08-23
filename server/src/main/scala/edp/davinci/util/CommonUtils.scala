package edp.davinci.util

import java.io.ByteArrayOutputStream

import edp.davinci.DavinciConstants.defaultEncode
import edp.davinci.csv.CSVWriter

object CommonUtils extends CommonUtils

trait CommonUtils {
  def covert2CSV(row: Seq[String]): String = {
    val byteArrOS = new ByteArrayOutputStream()
    val writer = CSVWriter.open(byteArrOS)
    writer.writeRow(row)
    val CSVStr = byteArrOS.toString(defaultEncode)
    byteArrOS.close()
    writer.close()
    CSVStr
  }


}
