package com.verify.service.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputToByte {
   public static byte[] toByte(InputStream inputStream) throws IOException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      byte[] bs = new byte[1024];
      boolean var3 = false;

      int len;
      while((len = inputStream.read(bs)) != -1) {
         os.write(bs, 0, len);
         os.flush();
      }

      inputStream.close();
      os.close();
      return os.toByteArray();
   }
}
