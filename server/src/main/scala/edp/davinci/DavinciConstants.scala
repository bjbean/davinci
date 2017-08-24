package edp.davinci

object DavinciConstants extends DavinciConstants with SeparatorConstants


trait DavinciConstants {
  lazy val flatTable = "flattable"
  lazy val defaultEncode = "UTF-8"
  lazy val groupVar = "group@var"
  lazy val queryVar = "query@var"
  lazy val updateVar = "update@var"
}


trait SeparatorConstants {
  lazy val conditionSeparator = ","
  lazy val sqlSeparator = ";"
  lazy val sqlUrlSeparator = "&"
  lazy val CSVHeaderSeparator = ':'
  lazy val delimiterStartChar = '<'
  lazy val delimiterEndChar = '>'
  lazy val assignmentChar = '='
  lazy val dollarDelimiter = '$'
  lazy val STStartChar = '{'
  lazy val STEndChar = '}'
}
