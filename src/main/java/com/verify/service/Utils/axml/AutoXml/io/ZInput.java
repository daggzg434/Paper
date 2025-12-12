package com.verify.service.Utils.axml.AutoXml.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ZInput {
   protected final DataInputStream dis;
   protected final byte[] work;
   private int size;

   public ZInput(InputStream in) throws IOException {
      this.dis = new DataInputStream(in);
      this.work = new byte[8];
      this.size = 0;
   }

   public int getOffset() throws IOException {
      return this.size;
   }

   public void skipByOffset(int offset) throws IOException {
      offset -= this.getOffset();
      if (offset > 0) {
         this.skipBytes(offset);
      }

   }

   public void close() throws IOException {
      this.dis.close();
   }

   public int available() throws IOException {
      return this.dis.available();
   }

   public final boolean readBoolean() throws IOException {
      ++this.size;
      return this.dis.readBoolean();
   }

   public final byte readByte() throws IOException {
      ++this.size;
      return this.dis.readByte();
   }

   public final char readChar() throws IOException {
      this.dis.readFully(this.work, 0, 2);
      this.size += 2;
      return (char)((this.work[1] & 255) << 8 | this.work[0] & 255);
   }

   public final double readDouble() throws IOException {
      return Double.longBitsToDouble(this.readLong());
   }

   public int[] readIntArray(int length) throws IOException {
      int[] array = new int[length];

      for(int i = 0; i < length; ++i) {
         array[i] = this.readInt();
      }

      return array;
   }

   public void skipInt() throws IOException {
      this.skipBytes(4);
   }

   public void skipCheckChunkTypeInt(int expected, int possible) throws IOException {
      int got = this.readInt();
      if (got == possible) {
         this.skipCheckChunkTypeInt(expected, -1);
      } else if (got != expected) {
         throw new IOException(String.format("Expected: 0x%08x, got: 0x%08x", expected, got));
      }

   }

   public void skipCheckInt(int expected) throws IOException {
      int got = this.readInt();
      if (got != expected) {
         throw new IOException(String.format("Expected: 0x%08x, got: 0x%08x", expected, got));
      }
   }

   public void skipCheckShort(short expected) throws IOException {
      short got = this.readShort();
      if (got != expected) {
         throw new IOException(String.format("Expected: 0x%08x, got: 0x%08x", expected, got));
      }
   }

   public void skipCheckByte(byte expected) throws IOException {
      byte got = this.readByte();
      if (got != expected) {
         throw new IOException(String.format("Expected: 0x%08x, got: 0x%08x", expected, got));
      }
   }

   public int read(byte[] b, int a, int len) throws IOException {
      int r = this.dis.read(b, a, len);
      this.size += r;
      return r;
   }

   public final float readFloat() throws IOException {
      return Float.intBitsToFloat(this.readInt());
   }

   public final void readFully(byte[] ba) throws IOException {
      this.dis.readFully(ba, 0, ba.length);
      this.size += ba.length;
   }

   public final void readFully(byte[] ba, int off, int len) throws IOException {
      this.dis.readFully(ba, off, len);
      this.size += len;
   }

   public final int readInt() throws IOException {
      this.dis.readFully(this.work, 0, 4);
      this.size += 4;
      return this.work[3] << 24 | (this.work[2] & 255) << 16 | (this.work[1] & 255) << 8 | this.work[0] & 255;
   }

   public final long readLong() throws IOException {
      this.dis.readFully(this.work, 0, 8);
      this.size += 8;
      return (long)this.work[7] << 56 | ((long)this.work[6] & 255L) << 48 | ((long)this.work[5] & 255L) << 40 | ((long)this.work[4] & 255L) << 32 | ((long)this.work[3] & 255L) << 24 | ((long)this.work[2] & 255L) << 16 | ((long)this.work[1] & 255L) << 8 | (long)this.work[0] & 255L;
   }

   public final short readShort() throws IOException {
      this.dis.readFully(this.work, 0, 2);
      this.size += 2;
      return (short)((this.work[1] & 255) << 8 | this.work[0] & 255);
   }

   public final int readUnsignedShort() throws IOException {
      this.dis.readFully(this.work, 0, 2);
      this.size += 2;
      return (this.work[1] & 255) << 8 | this.work[0] & 255;
   }

   public final int skipBytes(int n) throws IOException {
      this.size += n;
      return this.dis.skipBytes(n);
   }

   public String readNullEndedString(int length, boolean fixed) throws IOException {
      StringBuilder string = new StringBuilder(16);

      while(length-- != 0) {
         short ch = this.readShort();
         if (ch == 0) {
            break;
         }

         string.append((char)ch);
      }

      if (fixed) {
         this.skipBytes(length * 2);
      }

      return string.toString();
   }
}
