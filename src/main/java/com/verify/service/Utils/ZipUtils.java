package com.verify.service.Utils;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
   public static void putNextEntry(ZipOutputStream zipOutputStream, String name, byte[] data) throws IOException {
      zipOutputStream.putNextEntry(new ZipEntry(name));
      zipOutputStream.write(data);
      zipOutputStream.closeEntry();
   }
}
