package edp.davinci.common

import java.nio.charset.{Charset, CodingErrorAction}
import java.util.TimeZone

import scala.io.Codec

object EdpDefault extends EdpDefault

trait EdpDefault {
  type JavaByteArray = Array[Byte]
  type JavaShort = java.lang.Short
  type JavaInteger = java.lang.Integer
  type JavaLong = java.lang.Long
  type JavaFloat = java.lang.Float
  type JavaDouble = java.lang.Double
  type JavaBoolean = java.lang.Boolean
  type JavaCharacter = java.lang.Character
  type JavaString = java.lang.String
  type JavaBigInteger = java.math.BigInteger
  type JavaBigDecimal = java.math.BigDecimal

  type SuperComparable[T] = Comparable[_ >: T]

  lazy val javaByteArrayType = classOf[JavaByteArray]
  lazy val javaShortType = classOf[JavaShort]
  lazy val javaIntegerType = classOf[JavaInteger]
  lazy val javaLongType = classOf[JavaLong]
  lazy val javaFloatType = classOf[JavaFloat]
  lazy val javaDoubleType = classOf[JavaDouble]
  lazy val javaBooleanType = classOf[JavaBoolean]
  lazy val javaCharacterType = classOf[JavaCharacter]
  lazy val javaStringType = classOf[JavaString]
  lazy val javaBigIntegerType = classOf[JavaBigInteger]
  lazy val javaBigDecimalType = classOf[JavaBigDecimal]

  implicit lazy val defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai")

  implicit lazy val defaultCodec = Codec.UTF8
  defaultCodec.onMalformedInput(CodingErrorAction.REPLACE)
  defaultCodec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  implicit lazy val defaultCharset = Charset.forName("UTF-8")
}
