package com.verify.service.Utils.axml.AutoXml.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
   public static byte[] readBytes(InputStream is) throws IOException {
      byte[] buf = new byte[10240];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      int num;
      while((num = is.read(buf)) != -1) {
         baos.write(buf, 0, num);
      }

      byte[] b = baos.toByteArray();
      baos.close();
      return b;
   }
}
