package com.verify.service.Utils;

import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DesUtils {
   public static String encrypt(String strIn, String secretkey) throws Exception {
      Key key = new SecretKeySpec(secretkey.getBytes(), "DES");
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(1, key);
      return Base64.getEncoder().encodeToString(cipher.doFinal(strIn.getBytes("UTF-8")));
   }

   public static String decrypt(String strIn, String secretkey) throws Exception {
      Key key = new SecretKeySpec(secretkey.getBytes(), "DES");
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(2, key);
      byte[] param = cipher.doFinal(Base64.getDecoder().decode(strIn));
      return new String(param, "utf-8");
   }
}
