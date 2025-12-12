package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public abstract class BaseItem {
   public static final int NO_INDEX = -1;
   public int index;
   public int offset;

   protected static void addPadding(DataOut out, int alignment) {
      int x = out.offset() % alignment;
      if (x != 0) {
         out.skip("padding", alignment - x);
      }

   }

   public static void addPadding(DataOut out, int offset, int alignment) {
      int x = offset % alignment;
      if (x != 0) {
         out.skip("padding", alignment - x);
      }

   }

   public static int padding(int offset, int alignment) {
      int x = offset % alignment;
      if (x != 0) {
         offset += alignment - x;
      }

      return offset;
   }

   public static int lengthOfSleb128(int value) {
      int remaining = value >> 7;
      boolean hasMore = true;
      int end = (value & Integer.MIN_VALUE) == 0 ? 0 : -1;

      int count;
      for(count = 0; hasMore; remaining >>= 7) {
         hasMore = remaining != end || (remaining & 1) != (value >> 6 & 1);
         ++count;
         value = remaining;
      }

      return count;
   }

   public static int lengthOfUleb128(int value) {
      int remaining = value >>> 7;

      int length;
      for(length = 1; remaining != 0; remaining >>>= 7) {
         ++length;
      }

      return length;
   }

   public abstract void write(DataOut out);

   public abstract int place(int offset);
}
