package edp.davinci.util;

import java.security.MessageDigest;

public class MD5Utils {
    public static String getMD5(String str) throws Exception{
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");// 获取MD5算法对象
            byte[] digest = instance.digest(str.getBytes());// 对字符串加密,返回字节数组

            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                int i = b & 0xff;// 获取字节的低八位有效值
                String hexString = Integer.toHexString(i);// 将整数转为16进制
                // System.out.println(hexString);

                if (hexString.length() < 2) {
                    hexString = "0" + hexString;// 如果是1位的话,补0
                }

                sb.append(hexString);
            }

            return sb.toString();

        } catch (Exception e) {
            throw e;
        }
    }
}
