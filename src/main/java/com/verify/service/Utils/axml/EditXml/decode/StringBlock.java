package com.verify.service.Utils.axml.EditXml.decode;

import com.verify.service.Utils.axml.Manifest_ids;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class StringBlock implements IAXMLSerialize {
   private static final int TAG = 1835009;
   private static final int INT_SIZE = 4;
   private int mChunkSize;
   private int mStringsCount;
   private int mStylesCount;
   private int mEncoder;
   private int mStrBlockOffset;
   private int mStyBlockOffset;
   private int[] mPerStrOffset;
   private int[] mPerStyOffset;
   private List<String> mStrings;
   private List<StringBlock.Style> mStyles;

   public List<String> getmStrings() {
      return this.mStrings;
   }

   public int getStringMapping(String str) {
      int size = this.mStrings.size();

      for(int i = 0; i < size; ++i) {
         if (((String)this.mStrings.get(i)).equals(str)) {
            return i;
         }
      }

      return -1;
   }

   public int putString(String str) {
      return this.containsString(str) ? this.getStringMapping(str) : this.addString(str);
   }

   public int addString(String str) {
      int id = Manifest_ids.getInstance().parseids(str);
      if (id > 16840000) {
         this.mStrings.add(this.getStringMapping("android"), str);
         return this.getStringMapping(str);
      } else {
         this.mStrings.add(str);
         return this.mStrings.size() - 1;
      }
   }

   public String setString(int index, String str) {
      return (String)this.mStrings.set(index, str);
   }

   public String removeString(int index) {
      return (String)this.mStrings.remove(index);
   }

   public boolean containsString(String str) {
      return this.mStrings.contains(str.trim());
   }

   public int getStringCount() {
      return this.mStrings.size();
   }

   private static int[] getVarint(byte[] array, int offset) {
      return (array[offset] & 128) == 0 ? new int[]{array[offset] & 127, 1} : new int[]{(array[offset] & 127) << 8 | array[offset + 1] & 255, 2};
   }

   public void read(IntReader reader) throws IOException {
      this.mChunkSize = reader.readInt();
      this.mStringsCount = reader.readInt();
      this.mStylesCount = reader.readInt();
      this.mEncoder = reader.readInt();
      this.mStrBlockOffset = reader.readInt();
      this.mStyBlockOffset = reader.readInt();
      if (this.mStringsCount > 0) {
         this.mPerStrOffset = reader.readIntArray(this.mStringsCount);
         this.mStrings = new ArrayList(this.mStringsCount);
      }

      if (this.mStylesCount > 0) {
         this.mPerStyOffset = reader.readIntArray(this.mStylesCount);
         this.mStyles = new ArrayList();
      }

      int size;
      int i;
      int offset;
      if (this.mStringsCount > 0) {
         size = (this.mStyBlockOffset == 0 ? this.mChunkSize : this.mStyBlockOffset) - this.mStrBlockOffset;
         byte[] rawStrings = reader.readByteArray(size);

         for(i = 0; i < this.mStringsCount; ++i) {
            if ((this.mEncoder & 256) != 0) {
               offset = this.mPerStrOffset[i];
               offset += getVarint(rawStrings, this.mPerStrOffset[i])[1];
               int[] varint = getVarint(rawStrings, offset);
               offset += varint[1];
               int length = varint[0];
               this.mStrings.add(i, new String(rawStrings, offset, length, Charset.forName("UTF-8")));
            } else {
               offset = this.mPerStrOffset[i];
               short len = this.toShort((short)rawStrings[offset], (short)rawStrings[offset + 1]);
               this.mStrings.add(i, new String(rawStrings, offset + 2, len * 2, Charset.forName("UTF-16LE")));
            }
         }
      }

      if (this.mStylesCount > 0) {
         size = this.mChunkSize - this.mStyBlockOffset;
         int[] styles = reader.readIntArray(size / 4);

         for(i = 0; i < this.mStylesCount; ++i) {
            offset = this.mPerStyOffset[i];

            int j;
            for(j = offset; j < styles.length && styles[j] != -1; ++j) {
            }

            int[] array = new int[j - offset];
            System.arraycopy(styles, offset, array, 0, array.length);
            StringBlock.Style d = StringBlock.Style.parse(array);
            this.mStyles.add(d);
         }
      }

   }

   public void write(IntWriter writer) throws IOException {
      int size = 0;
      int size = size + writer.writeInt(1835009);
      size += writer.writeInt(this.mChunkSize);
      size += writer.writeInt(this.mStringsCount);
      size += writer.writeInt(this.mStylesCount);
      size += writer.writeInt(0);
      size += writer.writeInt(this.mStrBlockOffset);
      size += writer.writeInt(this.mStyBlockOffset);
      int[] var3;
      int var4;
      int var5;
      int offset;
      if (this.mPerStrOffset != null) {
         var3 = this.mPerStrOffset;
         var4 = var3.length;

         for(var5 = 0; var5 < var4; ++var5) {
            offset = var3[var5];
            size += writer.writeInt(offset);
         }
      }

      if (this.mPerStyOffset != null) {
         var3 = this.mPerStyOffset;
         var4 = var3.length;

         for(var5 = 0; var5 < var4; ++var5) {
            offset = var3[var5];
            size += writer.writeInt(offset);
         }
      }

      Iterator var8;
      if (this.mStrings != null) {
         for(var8 = this.mStrings.iterator(); var8.hasNext(); size += writer.writeShort((short)0)) {
            String s = (String)var8.next();
            byte[] raw = s.getBytes("UTF-16LE");
            size += writer.writeShort((short)s.length());
            size += writer.writeByteArray(raw);
         }
      }

      StringBlock.Style style;
      if (this.mStyles != null) {
         for(var8 = this.mStyles.iterator(); var8.hasNext(); size += style.write(writer)) {
            style = (StringBlock.Style)var8.next();
         }
      }

      if (this.mChunkSize > size) {
         writer.writeShort((short)0);
      }

   }

   protected static byte[] getVarBytes(int val) {
      if ((val & 127) == val) {
         return new byte[]{(byte)val};
      } else {
         byte[] b = new byte[]{(byte)(val >>> 8 | 128), (byte)(val & 255)};
         return b;
      }
   }

   public void prepare() throws IOException {
      this.mStringsCount = this.mStrings == null ? 0 : this.mStrings.size();
      this.mStylesCount = this.mStyles == null ? 0 : this.mStyles.size();
      int base = 28;
      int strSize = 0;
      int[] perStrSize = null;
      int stySize;
      if (this.mStrings != null) {
         stySize = 0;
         perStrSize = new int[this.mStrings.size()];

         for(int i = 0; i < this.mStrings.size(); ++i) {
            perStrSize[i] = stySize;

            try {
               stySize += 2 + ((String)this.mStrings.get(i)).getBytes("UTF-16LE").length + 2;
            } catch (UnsupportedEncodingException var9) {
               throw new IOException(var9);
            }
         }

         strSize = stySize;
      }

      stySize = 0;
      int[] perStySize = null;
      int string_array_size;
      int style_array_size;
      if (this.mStyles != null) {
         string_array_size = 0;
         perStySize = new int[this.mStyles.size()];

         for(style_array_size = 0; style_array_size < this.mStyles.size(); ++style_array_size) {
            perStySize[style_array_size] = string_array_size;
            string_array_size += ((StringBlock.Style)this.mStyles.get(style_array_size)).getSize();
         }

         stySize = string_array_size;
      }

      string_array_size = perStrSize == null ? 0 : perStrSize.length * 4;
      style_array_size = perStySize == null ? 0 : perStySize.length * 4;
      if (this.mStrings != null && this.mStrings.size() > 0) {
         this.mStrBlockOffset = base + string_array_size + style_array_size;
         this.mPerStrOffset = perStrSize;
      } else {
         this.mStrBlockOffset = 0;
         this.mPerStrOffset = null;
      }

      if (this.mStyles != null && this.mStyles.size() > 0) {
         this.mStyBlockOffset = base + string_array_size + style_array_size + strSize;
         this.mPerStyOffset = perStySize;
      } else {
         this.mStyBlockOffset = 0;
         this.mPerStyOffset = null;
      }

      this.mChunkSize = base + string_array_size + style_array_size + strSize + stySize;
      int align = this.mChunkSize % 4;
      if (align != 0) {
         this.mChunkSize += 4 - align;
      }

   }

   public int getSize() {
      return this.mChunkSize;
   }

   public String getStringFor(int index) {
      if (index == -1) {
         return null;
      } else if (index >= this.mStrings.size()) {
         return null;
      } else {
         return index < 0 ? null : (String)this.mStrings.get(index);
      }
   }

   private short toShort(short byte1, short byte2) {
      return (short)((byte2 << 8) + byte1);
   }

   public StringBlock.Style getStyle(int index) {
      return (StringBlock.Style)this.mStyles.get(index);
   }

   public int getType() {
      return 0;
   }

   public void setSize(int size) {
   }

   public void setType(int type) {
   }

   public String toString() {
      return "StringBlock{mChunkSize=" + this.mChunkSize + ", mStringsCount=" + this.mStringsCount + ", mStylesCount=" + this.mStylesCount + ", mEncoder=" + this.mEncoder + ", mStrBlockOffset=" + this.mStrBlockOffset + ", mStyBlockOffset=" + this.mStyBlockOffset + ", mPerStrOffset=" + Arrays.toString(this.mPerStrOffset) + ", mPerStyOffset=" + Arrays.toString(this.mPerStyOffset) + ", mStrings=" + this.mStrings + ", mStyles=" + this.mStyles + '}';
   }

   public static class Decorator {
      public static final int SIZE = 3;
      public int mTag;
      public int mDoctBegin;
      public int mDoctEnd;

      public Decorator(int[] triplet) {
         this.mTag = triplet[0];
         this.mDoctBegin = triplet[1];
         this.mDoctEnd = triplet[2];
      }

      public Decorator() {
      }
   }

   public static class Style {
      List<StringBlock.Decorator> mDct = new ArrayList();

      public List<StringBlock.Decorator> getDecorator() {
         return this.mDct;
      }

      public void addStyle(StringBlock.Decorator style) {
         this.mDct.add(style);
      }

      public int getSize() {
         int size = 0;
         int size = size + this.getCount() * 3;
         ++size;
         return size;
      }

      public int getCount() {
         return this.mDct.size();
      }

      public static StringBlock.Style parse(int[] muti_triplet) throws IOException {
         if (muti_triplet != null && muti_triplet.length % 3 == 0) {
            StringBlock.Style d = new StringBlock.Style();
            StringBlock.Decorator style = null;

            for(int i = 0; i < muti_triplet.length; ++i) {
               if (i % 3 == 0) {
                  new StringBlock.Decorator();
               }

               switch(i % 3) {
               case 0:
                  style = new StringBlock.Decorator();
                  style.mTag = muti_triplet[i];
                  break;
               case 1:
                  style.mDoctBegin = muti_triplet[i];
                  break;
               case 2:
                  style.mDoctEnd = muti_triplet[i];
                  d.mDct.add(style);
               }
            }

            return d;
         } else {
            throw new IOException("Fail to parse style");
         }
      }

      public int write(IntWriter writer) throws IOException {
         int size = 0;
         if (this.mDct != null && this.mDct.size() > 0) {
            StringBlock.Decorator dct;
            for(Iterator var3 = this.mDct.iterator(); var3.hasNext(); size += writer.writeInt(dct.mDoctEnd)) {
               dct = (StringBlock.Decorator)var3.next();
               size += writer.writeInt(dct.mTag);
               size += writer.writeInt(dct.mDoctBegin);
            }

            size += writer.writeInt(-1);
         }

         return size;
      }
   }
}
