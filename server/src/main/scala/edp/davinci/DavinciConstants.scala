package edp.davinci

object DavinciConstants extends DavinciConstants with SeparatorConstants


trait DavinciConstants {
  lazy val flatTable = "flattable"
  lazy val defaultEncode = "UTF-8"
  lazy val groupVar = "g@var"
  lazy val queryVar = "q@var"
  lazy val updateVar = "u@var"
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
