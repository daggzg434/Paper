package com.googlecode.d2j.reader.zip;

import com.googlecode.d2j.util.zip.AccessBufByteArrayOutputStream;
import com.googlecode.d2j.util.zip.ZipEntry;
import com.googlecode.d2j.util.zip.ZipFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ZipUtil {
   public static byte[] toByteArray(InputStream is) throws IOException {
      AccessBufByteArrayOutputStream out = new AccessBufByteArrayOutputStream();
      byte[] buff = new byte[1024];

      for(int c = is.read(buff); c > 0; c = is.read(buff)) {
         out.write(buff, 0, c);
      }

      return out.getBuf();
   }

   public static byte[] readDex(InputStream in) throws IOException {
      return readDex(toByteArray(in));
   }

   public static byte[] readDex(byte[] data) throws IOException {
      if (data.length < 3) {
         throw new IOException("File too small to be a dex/zip");
      } else if ("dex".equals(new String(data, 0, 3, StandardCharsets.ISO_8859_1))) {
         return data;
      } else if ("PK".equals(new String(data, 0, 2, StandardCharsets.ISO_8859_1))) {
         ZipFile zipFile = new ZipFile(data);
         Throwable var2 = null;

         byte[] var4;
         try {
            ZipEntry classes = zipFile.findFirstEntry("classes.dex");
            if (classes == null) {
               throw new IOException("Can not find classes.dex in zip file");
            }

            var4 = toByteArray(zipFile.getInputStream(classes));
         } catch (Throwable var13) {
            var2 = var13;
            throw var13;
         } finally {
            if (zipFile != null) {
               if (var2 != null) {
                  try {
                     zipFile.close();
                  } catch (Throwable var12) {
                     var2.addSuppressed(var12);
                  }
               } else {
                  zipFile.close();
               }
            }

         }

         return var4;
      } else {
         throw new IOException("the src file not a .dex or zip file");
      }
   }
}
