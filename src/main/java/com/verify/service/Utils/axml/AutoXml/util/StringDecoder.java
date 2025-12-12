package com.verify.service.Utils.axml.AutoXml.util;

import com.verify.service.Utils.axml.AutoXml.io.ZInput;
import com.verify.service.Utils.axml.AutoXml.io.ZOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

public class StringDecoder {
   private String[] m_strings;
   private int[] m_styleOffsets;
   private int[] m_styles;
   private boolean m_isUTF8;
   private int styleOffsetCount;
   private int stylesOffset;
   private int flags;
   private int chunkSize;
   private int m_strings_size;
   private static final CharsetDecoder UTF16LE_DECODER = Charset.forName("UTF-16LE").newDecoder();
   private static final CharsetDecoder UTF8_DECODER = Charset.forName("UTF-8").newDecoder();
   private static final int CHUNK_STRINGPOOL_TYPE = 1835009;
   private static final int CHUNK_NULL_TYPE = 0;
   public static final int IS_UTF8 = 256;

   public boolean isUtf8() {
      return this.m_isUTF8;
   }

   public static StringDecoder read(ZInput mIn) throws IOException {
      mIn.skipCheckChunkTypeInt(1835009, 0);
      StringDecoder block = new StringDecoder();
      int chunkSize = block.chunkSize = mIn.readInt();
      int stringCount = mIn.readInt();
      int styleCount = block.styleOffsetCount = mIn.readInt();
      int flags = block.flags = mIn.readInt();
      int stringsOffset = mIn.readInt();
      int stylesOffset = block.stylesOffset = mIn.readInt();
      block.m_isUTF8 = (flags & 256) != 0;
      int[] m_stringOffsets = mIn.readIntArray(stringCount);
      if (styleCount != 0) {
         block.m_styleOffsets = mIn.readIntArray(styleCount);
      }

      int size = (stylesOffset == 0 ? chunkSize : stylesOffset) - stringsOffset;
      byte[] data = new byte[size];
      mIn.readFully(data);
      block.m_strings_size = size;
      int remaining;
      if (stylesOffset != 0) {
         size = chunkSize - stylesOffset;
         block.m_styles = mIn.readIntArray(size / 4);
         remaining = size % 4;
         if (remaining >= 1) {
            while(remaining-- > 0) {
               mIn.readByte();
            }
         }
      }

      remaining = 0;
      block.m_strings = new String[m_stringOffsets.length];
      int[] var12 = m_stringOffsets;
      int var13 = m_stringOffsets.length;

      for(int var14 = 0; var14 < var13; ++var14) {
         int offset = var12[var14];
         int length;
         if (!block.m_isUTF8) {
            length = getShort(data, offset) * 2;
            offset += 2;
         } else {
            offset += getVarint(data, offset)[1];
            int[] varint = getVarint(data, offset);
            offset += varint[1];
            length = varint[0];
         }

         block.m_strings[remaining++] = decodeString(offset, length, block.m_isUTF8, data);
      }

      byte[] data = null;
      return block;
   }

   public int find(String string) {
      if (string == null) {
         return -1;
      } else {
         for(int i = 0; i < this.m_strings.length; ++i) {
            if (this.getString(i).equals(string)) {
               return i;
            }
         }

         return -1;
      }
   }

   public void getStrings(List<String> list) {
      int size = this.getSize();

      for(int i = 0; i < size; ++i) {
         list.add(this.getString(i));
      }

   }

   public void write(ZOutput out) throws IOException {
      this.write(this.m_strings, out);
   }

   public void write(String[] s, ZOutput out) throws IOException {
      ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
      ZOutput led = new ZOutput(outBuf);
      int size = s.length;
      int[] offset = new int[size];
      int len = 0;
      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      ZOutput mStrings = new ZOutput(bOut);
      int m_strings_size;
      String var;
      char[] charBuf;
      int i;
      if (this.m_isUTF8) {
         for(m_strings_size = 0; m_strings_size < size; ++m_strings_size) {
            offset[m_strings_size] = len;
            var = s[m_strings_size];
            charBuf = var.toCharArray();
            byte[] b = getVarBytes(charBuf.length);
            mStrings.writeFully(b);
            len += b.length;
            byte[] buf = var.getBytes("UTF-8");
            b = getVarBytes(buf.length);
            mStrings.writeFully(b);
            len += b.length;
            mStrings.writeFully(buf);
            len += buf.length;
            mStrings.writeByte(0);
            ++len;
         }
      } else {
         for(m_strings_size = 0; m_strings_size < size; ++m_strings_size) {
            offset[m_strings_size] = len;
            var = s[m_strings_size];
            charBuf = var.toCharArray();
            mStrings.writeShort((short)charBuf.length);
            char[] var22 = charBuf;
            i = charBuf.length;

            for(int var15 = 0; var15 < i; ++var15) {
               char c = var22[var15];
               mStrings.writeChar(c);
            }

            mStrings.writeShort((short)0);
            len += charBuf.length * 2 + 4;
         }
      }

      m_strings_size = bOut.size();
      int size_mod = m_strings_size % 4;
      if (size_mod != 0) {
         for(int i = 0; i < 4 - size_mod; ++i) {
            bOut.write(0);
         }

         m_strings_size += 4 - size_mod;
      }

      byte[] m_strings = bOut.toByteArray();
      led.writeInt(size);
      led.writeInt(this.styleOffsetCount);
      led.writeInt(this.flags);
      int stringsOffset = 28 + (size + this.styleOffsetCount) * 4;
      led.writeInt(stringsOffset);
      i = m_strings_size - this.m_strings_size;
      led.writeInt(this.stylesOffset == 0 ? 0 : this.stylesOffset + i * 4);
      led.writeIntArray(offset);
      if (this.styleOffsetCount != 0) {
         int[] var25 = this.m_styleOffsets;
         int var27 = var25.length;

         for(int var17 = 0; var17 < var27; ++var17) {
            int j = var25[var17];
            led.writeInt(j);
         }
      }

      led.writeFully(m_strings);
      if (this.m_styles != null) {
         led.writeIntArray(this.m_styles);
      }

      out.writeInt(1835009);
      byte[] b = outBuf.toByteArray();
      outBuf.close();
      led.close();
      out.writeInt(b.length + 8);
      out.writeFully(b);
   }

   public int getChunkSize() {
      return this.chunkSize;
   }

   public void setString(int index, String s) {
      this.m_strings[index] = s;
   }

   public String getString(int index) {
      return index >= 0 ? this.m_strings[index] : null;
   }

   private static int[] getVarint(byte[] array, int offset) {
      return (array[offset] & 128) == 0 ? new int[]{array[offset] & 127, 1} : new int[]{(array[offset] & 127) << 8 | array[offset + 1] & 255, 2};
   }

   protected static byte[] getVarBytes(int val) {
      if ((val & 127) == val) {
         return new byte[]{(byte)val};
      } else {
         byte[] b = new byte[]{(byte)(val >>> 8 | 128), (byte)(val & 255)};
         return b;
      }
   }

   public int getSize() {
      return this.m_strings.length;
   }

   private static String decodeString(int offset, int length, boolean utf8, byte[] data) {
      try {
         return (utf8 ? UTF8_DECODER : UTF16LE_DECODER).decode(ByteBuffer.wrap(data, offset, length)).toString();
      } catch (CharacterCodingException var5) {
         return null;
      }
   }

   private static int getShort(byte[] array, int offset) {
      return (array[offset + 1] & 255) << 8 | array[offset] & 255;
   }
}
