package edp.davinci

object DavinciConstants extends DavinciConstants with SeparatorConstants


trait DavinciConstants {
  lazy val flatTable = "flattable"
  lazy val defaultEncode = "UTF-8"
}


trait SeparatorConstants {
  lazy val conditionSeparator = ","
  lazy val sqlSeparator = ";"
  lazy val sqlUrlSeparator = "&"
  lazy val CSVHeaderSeparator = ':'
}
