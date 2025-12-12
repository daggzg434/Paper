package com.googlecode.d2j.util.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;

public class AutoSTOREDZipOutputStream extends ZipOutputStream {
   private CRC32 crc = new CRC32();
   private java.util.zip.ZipEntry delayedEntry;
   private AccessBufByteArrayOutputStream delayedOutputStream;

   public AutoSTOREDZipOutputStream(OutputStream out) {
      super(out);
   }

   public void putNextEntry(java.util.zip.ZipEntry e) throws IOException {
      if (e.getMethod() != 0) {
         super.putNextEntry(e);
      } else {
         this.delayedEntry = e;
         if (this.delayedOutputStream == null) {
            this.delayedOutputStream = new AccessBufByteArrayOutputStream();
         }
      }

   }

   public void closeEntry() throws IOException {
      java.util.zip.ZipEntry delayedEntry = this.delayedEntry;
      if (delayedEntry != null) {
         AccessBufByteArrayOutputStream delayedOutputStream = this.delayedOutputStream;
         byte[] buf = delayedOutputStream.getBuf();
         int size = delayedOutputStream.size();
         delayedEntry.setSize((long)size);
         delayedEntry.setCompressedSize((long)size);
         this.crc.reset();
         this.crc.update(buf, 0, size);
         delayedEntry.setCrc(this.crc.getValue());
         super.putNextEntry(delayedEntry);
         super.write(buf, 0, size);
         this.delayedEntry = null;
         delayedOutputStream.reset();
      }

      super.closeEntry();
   }

   public synchronized void write(byte[] b, int off, int len) throws IOException {
      if (this.delayedEntry != null) {
         this.delayedOutputStream.write(b, off, len);
      } else {
         super.write(b, off, len);
      }

   }

   public void write(int b) throws IOException {
      if (this.delayedEntry != null) {
         this.delayedOutputStream.write(b);
      } else {
         super.write(b);
      }

   }

   public void write(byte[] b) throws IOException {
      if (this.delayedEntry != null) {
         this.delayedOutputStream.write(b);
      } else {
         super.write(b);
      }

   }

   public void close() throws IOException {
      this.delayedOutputStream = null;
      super.close();
   }
}
