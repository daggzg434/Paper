package com.verify.service.Utils.axml.EditXml.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {
   private static final long FILE_COPY_BUFFER_SIZE = 1048576L;

   public static FileInputStream openInputStream(final File file) throws IOException {
      if (file.exists()) {
         if (file.isDirectory()) {
            throw new IOException("File '" + file + "' exists but is a directory");
         } else if (!file.canRead()) {
            throw new IOException("File '" + file + "' cannot be read");
         } else {
            return new FileInputStream(file);
         }
      } else {
         throw new FileNotFoundException("File '" + file + "' does not exist");
      }
   }

   public static void copyFile(final File srcFile, final File destFile) throws IOException {
      checkFileRequirements(srcFile, destFile);
      if (srcFile.isDirectory()) {
         throw new IOException("Source '" + srcFile + "' exists but is a directory");
      } else if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
         throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
      } else {
         File parentFile = destFile.getParentFile();
         if (parentFile != null && !parentFile.mkdirs() && !parentFile.isDirectory()) {
            throw new IOException("Destination '" + parentFile + "' directory cannot be created");
         } else if (destFile.exists() && !destFile.canWrite()) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
         } else {
            doCopyFile(srcFile, destFile);
         }
      }
   }

   private static void doCopyFile(final File srcFile, final File destFile) throws IOException {
      if (destFile.exists() && destFile.isDirectory()) {
         throw new IOException("Destination '" + destFile + "' exists but is a directory");
      } else {
         FileInputStream fis = null;
         FileOutputStream fos = null;
         FileChannel input = null;
         FileChannel output = null;

         long srcLen;
         long dstLen;
         try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            srcLen = input.size();
            dstLen = 0L;

            long bytesCopied;
            for(long count = 0L; dstLen < srcLen; dstLen += bytesCopied) {
               long remain = srcLen - dstLen;
               count = remain > 1048576L ? 1048576L : remain;
               bytesCopied = output.transferFrom(input, dstLen, count);
               if (bytesCopied == 0L) {
                  break;
               }
            }
         } finally {
            IOUtils.closeQuietly(output, fos, input, fis);
         }

         srcLen = srcFile.length();
         dstLen = destFile.length();
         if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
         }
      }
   }

   private static void checkFileRequirements(File src, File dest) throws FileNotFoundException {
      if (src == null) {
         throw new NullPointerException("Source must not be null");
      } else if (dest == null) {
         throw new NullPointerException("Destination must not be null");
      } else if (!src.exists()) {
         throw new FileNotFoundException("Source '" + src + "' does not exist");
      }
   }
}
