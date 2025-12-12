package com.googlecode.d2j.dex.writer.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferOut implements DataOut {
   public final ByteBuffer buffer;

   public ByteBufferOut(ByteBuffer buffer) {
      this.buffer = buffer;
      buffer.order(ByteOrder.LITTLE_ENDIAN);
   }

   public void begin(String s) {
   }

   public void bytes(String s, byte[] bs) {
      this.buffer.put(bs);
   }

   public void bytes(String string, byte[] buf, int offset, int size) {
      this.buffer.put(buf, offset, size);
   }

   public void doUleb128(int value) {
      for(int remaining = value >>> 7; remaining != 0; remaining >>>= 7) {
         this.buffer.put((byte)(value & 127 | 128));
         value = remaining;
      }

      this.buffer.put((byte)(value & 127));
   }

   public void end() {
   }

   public int offset() {
      return this.buffer.position();
   }

   public void sbyte(String s, int b) {
      this.buffer.put((byte)b);
   }

   public void sint(String s, int i) {
      this.buffer.putInt(i);
   }

   public void skip(String s, int n) {
      this.buffer.position(this.buffer.position() + n);
   }

   public void skip4(String s) {
      this.buffer.putInt(0);
   }

   public void sleb128(String s, int value) {
      int remaining = value >> 7;
      boolean hasMore = true;

      for(int end = (value & Integer.MIN_VALUE) == 0 ? 0 : -1; hasMore; remaining >>= 7) {
         hasMore = remaining != end || (remaining & 1) != (value >> 6 & 1);
         this.buffer.put((byte)(value & 127 | (hasMore ? 128 : 0)));
         value = remaining;
      }

   }

   public void sshort(String s, int i) {
      this.buffer.putShort((short)i);
   }

   public void ubyte(String s, int b) {
      this.buffer.put((byte)b);
   }

   public void uint(String s, int i) {
      this.buffer.putInt(i);
   }

   public void uleb128(String s, int value) {
      this.doUleb128(value);
   }

   public void uleb128p1(String s, int i) {
      this.doUleb128(i + 1);
   }

   public void ushort(String s, int i) {
      this.buffer.putShort((short)i);
   }
}
