package edp.davinci.util

import java.security.SecureRandom
import javax.crypto.spec.DESKeySpec
import javax.crypto.{Cipher, SecretKeyFactory}


trait DES {
  def encrypt(dataSource: String,
              pwd: String = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456"): String = {
    try {
      val random = new SecureRandom()
      val desKey = new DESKeySpec(pwd.getBytes())
      val keyFactory = SecretKeyFactory.getInstance("DES")
      val secureKey = keyFactory.generateSecret(desKey)
      val cipher = Cipher.getInstance("DES")
      cipher.init(Cipher.ENCRYPT_MODE, secureKey, random)
      cipher.doFinal(dataSource.getBytes).toString
    } catch {
      case e: Throwable => e.printStackTrace()
        null.asInstanceOf[String]
    }
  }

  def decrypt(src: String,
              pwd: String = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456"): Array[Byte] = {
    try {
      val random = new SecureRandom()
      val desKey = new DESKeySpec(pwd.getBytes())
      val keyFactory = SecretKeyFactory.getInstance("DES")
      val secureKey = keyFactory.generateSecret(desKey)
      val cipher = Cipher.getInstance("DES")
      cipher.init(Cipher.DECRYPT_MODE, secureKey, random)
      cipher.doFinal(src.getBytes())
    } catch {
      case e: Throwable => e.printStackTrace()
        null.asInstanceOf[Array[Byte]]
    }
  }

}


//object DES {
//  def main(args: Array[String]): Unit = {
//    val UTF8_CHARSET = Charset.forName("UTF-8");
//    val str = "测试内容"
//    val pwd = "9588028820109132570743325311898426347857298773549468758875018579537757772163084478873699447306034466200616411960574122434059469100235892702736860872901247123456"
//    val des = new DES
//    val result = des.encrypt(str.getBytes(), pwd)
//    println("加密后 " + result)
//
//    try {
//      val decryptResult = des.decrypt(result, pwd)
//      println("解密后 " + new String(decryptResult, 0, decryptResult.length, UTF8_CHARSET))
//    }
//    catch {
//      case e:Throwable => e.printStackTrace()
//    }
//  }
//
//}
