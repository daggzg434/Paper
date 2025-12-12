package com.googlecode.d2j.util.zip;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;

public final class ZipEntry implements ZipConstants, Cloneable {
   String name;
   String comment;
   long crc = -1L;
   long compressedSize = -1L;
   long size = -1L;
   int compressionMethod = -1;
   int time = -1;
   int modDate = -1;
   byte[] extra;
   int nameLength = -1;
   long localHeaderRelOffset = -1L;
   public static final int DEFLATED = 8;
   public static final int STORED = 0;

   public String getComment() {
      return this.comment;
   }

   public long getCompressedSize() {
      return this.compressedSize;
   }

   public long getCrc() {
      return this.crc;
   }

   public byte[] getExtra() {
      return this.extra;
   }

   public int getMethod() {
      return this.compressionMethod;
   }

   public String getName() {
      return this.name;
   }

   public long getSize() {
      return this.size;
   }

   public long getTime() {
      if (this.time != -1) {
         GregorianCalendar cal = new GregorianCalendar();
         cal.set(14, 0);
         cal.set(1980 + (this.modDate >> 9 & 127), (this.modDate >> 5 & 15) - 1, this.modDate & 31, this.time >> 11 & 31, this.time >> 5 & 63, (this.time & 31) << 1);
         return cal.getTime().getTime();
      } else {
         return -1L;
      }
   }

   public boolean isDirectory() {
      return this.name.charAt(this.name.length() - 1) == '/';
   }

   public String toString() {
      return this.name;
   }

   public Object clone() {
      try {
         ZipEntry result = (ZipEntry)super.clone();
         result.extra = this.extra != null ? (byte[])this.extra.clone() : null;
         return result;
      } catch (CloneNotSupportedException var2) {
         throw new AssertionError(var2);
      }
   }

   ZipEntry(ByteBuffer it0, boolean skipCommentsAndExtra) throws IOException {
      ByteBuffer it = (ByteBuffer)it0.slice().order(ByteOrder.LITTLE_ENDIAN).limit(46);
      ZipFile.skip(it0, 46);
      int sig = it.getInt();
      if ((long)sig != 33639248L) {
         ZipFile.throwZipException("Central Directory Entry", sig);
      }

      it.position(8);
      int gpbf = it.getShort() & '\uffff';
      this.compressionMethod = it.getShort() & '\uffff';
      this.time = it.getShort() & '\uffff';
      this.modDate = it.getShort() & '\uffff';
      this.crc = (long)it.getInt() & 4294967295L;
      this.compressedSize = (long)it.getInt() & 4294967295L;
      this.size = (long)it.getInt() & 4294967295L;
      this.nameLength = it.getShort() & '\uffff';
      int extraLength = it.getShort() & '\uffff';
      int commentByteCount = it.getShort() & '\uffff';
      it.position(42);
      this.localHeaderRelOffset = (long)it.getInt() & 4294967295L;
      byte[] nameBytes = new byte[this.nameLength];
      it0.get(nameBytes);
      this.name = new String(nameBytes, 0, nameBytes.length, StandardCharsets.UTF_8);
      if (extraLength > 0) {
         if (skipCommentsAndExtra) {
            ZipFile.skip(it0, extraLength);
         } else {
            this.extra = new byte[extraLength];
            it.get(this.extra);
         }
      }

      if (commentByteCount > 0) {
         if (skipCommentsAndExtra) {
            ZipFile.skip(it0, commentByteCount);
         } else {
            byte[] commentBytes = new byte[commentByteCount];
            it0.get(commentBytes);
            this.comment = new String(commentBytes, 0, commentBytes.length, StandardCharsets.UTF_8);
         }
      }

   }

   private static boolean containsNulByte(byte[] bytes) {
      byte[] var1 = bytes;
      int var2 = bytes.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         byte b = var1[var3];
         if (b == 0) {
            return true;
         }
      }

      return false;
   }
}
