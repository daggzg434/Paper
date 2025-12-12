package com.verify.service.Utils;

import java.io.Serializable;
import java.security.MessageDigest;

public class SHAUtils implements Serializable {
   private static final long serialVersionUID = 1L;

   public static String SHA1(String str) {
      if (str != null && str.length() != 0) {
         char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

         try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));
            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] buf = new char[j * 2];
            int k = 0;

            for(int i = 0; i < j; ++i) {
               byte byte0 = md[i];
               buf[k++] = hexDigits[byte0 >>> 4 & 15];
               buf[k++] = hexDigits[byte0 & 15];
            }

            return new String(buf);
         } catch (Exception var9) {
            return null;
         }
      } else {
         return null;
      }
   }
}
