package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.io.ByteArrayOutputStream;

public class StringDataItem extends BaseItem implements Comparable<StringDataItem> {
   public final String string;

   public static void encode(ByteArrayOutputStream out, String s) {
      int length = s.length();

      for(int i = 0; i < length; ++i) {
         char ch = s.charAt(i);
         if (ch != 0 && ch <= 127) {
            out.write(ch);
         } else if (ch <= 2047) {
            out.write(192 | 31 & ch >> 6);
            out.write(128 | 63 & ch);
         } else {
            out.write(224 | 15 & ch >> 12);
            out.write(128 | 63 & ch >> 6);
            out.write(128 | 63 & ch);
         }
      }

   }

   public static int lengthOfMutf8(String s) {
      int result = 0;
      int length = s.length();

      for(int i = 0; i < length; ++i) {
         char ch = s.charAt(i);
         if (ch != 0 && ch <= 127) {
            ++result;
         } else if (ch <= 2047) {
            result += 2;
         } else {
            result += 3;
         }
      }

      return result;
   }

   public StringDataItem(String data) {
      this.string = data;
   }

   public int compareTo(StringDataItem o) {
      return this.string.compareTo(o.string);
   }

   public int place(int offset) {
      int length = lengthOfMutf8(this.string);
      return offset + lengthOfUleb128(this.string.length()) + length + 1;
   }

   public String toString() {
      return "StringDataItem [string=" + this.string + "]";
   }

   public void write(DataOut out) {
      this.write(out, new StringDataItem.Buffer());
   }

   public void write(DataOut out, StringDataItem.Buffer buff) {
      out.uleb128("string_data_length", this.string.length());
      encode(buff, this.string);
      buff.write(0);
      out.bytes("mutf8-string", buff.getBuf(), 0, buff.size());
   }

   public static class Buffer extends ByteArrayOutputStream {
      public byte[] getBuf() {
         return this.buf;
      }
   }
}
