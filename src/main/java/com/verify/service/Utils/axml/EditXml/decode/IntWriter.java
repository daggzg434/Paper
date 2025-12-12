package com.verify.service.Utils.axml.EditXml.decode;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntWriter implements Closeable {
   private OutputStream m_stream;
   private boolean m_bigEndian;
   private int m_position;
   private ByteBuffer shortBB = ByteBuffer.allocate(2);
   private ByteBuffer intBB = ByteBuffer.allocate(4);

   public IntWriter() {
   }

   public IntWriter(OutputStream stream, boolean bigEndian) {
      this.reset(stream, bigEndian);
   }

   public final void reset(OutputStream stream, boolean bigEndian) {
      this.m_stream = stream;
      this.m_bigEndian = bigEndian;
      this.m_position = 0;
      ByteOrder order = this.m_bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
      this.shortBB.order(order);
      this.intBB.order(order);
   }

   public final void close() {
      if (this.m_stream != null) {
         try {
            this.m_stream.flush();
            this.m_stream.close();
         } catch (IOException var2) {
         }

         this.reset((OutputStream)null, false);
      }
   }

   public final OutputStream getStream() {
      return this.m_stream;
   }

   public final boolean isBigEndian() {
      return this.m_bigEndian;
   }

   public final void setBigEndian(boolean bigEndian) {
      this.m_bigEndian = bigEndian;
   }

   public final int writeByte(int b) throws IOException {
      this.m_stream.write(b);
      ++this.m_position;
      return 1;
   }

   public final void writeByte(byte b) throws IOException {
      this.m_stream.write(b);
      ++this.m_position;
   }

   public final int writeShort(short s) throws IOException {
      this.shortBB.clear();
      this.shortBB.putShort(s);
      this.m_stream.write(this.shortBB.array());
      this.m_position += 2;
      return 2;
   }

   public final int writeInt(int i) throws IOException {
      this.intBB.clear();
      this.intBB.putInt(i);
      this.m_stream.write(this.intBB.array());
      this.m_position += 4;
      return 4;
   }

   public final void writeIntArray(int[] array) throws IOException {
      int[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int i = var2[var4];
         this.writeInt(i);
      }

   }

   public final void writeIntArray(int[] array, int offset, int length) throws IOException {
      int limit = offset + length;

      for(int i = offset; i < limit; ++i) {
         this.writeInt(i);
      }

   }

   public final int writeByteArray(byte[] array) throws IOException {
      this.m_stream.write(array);
      this.m_position += array.length;
      return array.length;
   }

   public final void skip(int n, byte def) throws IOException {
      for(int i = 0; i < n; ++i) {
         this.m_stream.write(def);
      }

      this.m_position += n;
   }

   public final void skipIntFFFF() throws IOException {
      this.writeInt(Integer.MAX_VALUE);
   }

   public final void skipInt0000() throws IOException {
      this.writeInt(0);
   }

   public final int getPosition() {
      return this.m_position;
   }
}
