package com.verify.service.Utils.axml.AutoXml.io;

import java.io.IOException;
import java.io.OutputStream;

public final class ZOutput {
   private int written = 0;
   private OutputStream dos;

   public ZOutput(OutputStream out) {
      this.dos = out;
   }

   public final void writeShort(short s) throws IOException {
      this.dos.write(s & 255);
      ++this.written;
      this.dos.write(s >>> 8 & 255);
      ++this.written;
   }

   public void close() throws IOException {
      this.dos.close();
   }

   public int size() {
      return this.written;
   }

   public final void writeChar(char c) throws IOException {
      this.dos.write(c & 255);
      ++this.written;
      this.dos.write(c >>> 8 & 255);
      ++this.written;
   }

   public final void writeCharArray(char[] c) throws IOException {
      char[] var2 = c;
      int var3 = c.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         char element = var2[var4];
         this.writeChar(element);
      }

   }

   public final void write(int i) throws IOException {
      this.dos.write(i);
      ++this.written;
   }

   public final void writeByte(int b) throws IOException {
      this.dos.write(b);
      ++this.written;
   }

   public final void writeFully(byte[] b) throws IOException {
      this.dos.write(b, 0, b.length);
      this.written += b.length;
   }

   public final void writeFully(byte[] b, int a, int len) throws IOException {
      this.dos.write(b, a, len);
      this.written += len;
   }

   public final void writeInt(int i) throws IOException {
      this.dos.write(i & 255);
      ++this.written;
      this.dos.write(i >>> 8 & 255);
      ++this.written;
      this.dos.write(i >>> 16 & 255);
      ++this.written;
      this.dos.write(i >>> 24 & 255);
      ++this.written;
   }

   public final void writeIntArray(int[] buf, int s, int end) throws IOException {
      while(s < end) {
         this.writeInt(buf[s]);
         ++s;
      }

   }

   public final void writeIntArray(int[] buf) throws IOException {
      this.writeIntArray(buf, 0, buf.length);
   }

   public final void writeNulEndedString(String string, int length, boolean fixed) throws IOException {
      char[] ch = string.toCharArray();

      for(int j = 0; j < ch.length && length != 0; --length) {
         this.writeChar(ch[j++]);
      }

      if (fixed) {
         for(int i = 0; i < length * 2; ++i) {
            this.dos.write(0);
            ++this.written;
         }
      }

   }
}
