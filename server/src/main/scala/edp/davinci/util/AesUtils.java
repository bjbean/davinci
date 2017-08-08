package edp.davinci.util;

import java.security.SecureRandom;

import javax.crypto.*;

public class AesUtils {


    public static SecretKey setKey(String strKey) {
        try {
            KeyGenerator _generator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(strKey.getBytes());
            _generator.init(128, secureRandom);
            return _generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(" 初始化密钥出现异常 ");
        }
    }

    public static String encrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, setKey(password));
            byte[] result = cipher.doFinal(byteContent);
            return parseByte2HexStr(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String content, String password) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, setKey(password));
            byte[] result = cipher.doFinal(parseHexStr2Byte(content));
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}
