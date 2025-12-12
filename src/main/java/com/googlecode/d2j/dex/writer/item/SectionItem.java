package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SectionItem<T extends BaseItem> extends BaseItem {
   public final SectionItem.SectionType sectionType;
   public final List<T> items = new ArrayList();
   int size = 0;

   public SectionItem(SectionItem.SectionType typeCode) {
      this.sectionType = typeCode;
   }

   public SectionItem(SectionItem.SectionType typeCode, Collection<T> itms) {
      this.sectionType = typeCode;
      this.items.addAll(itms);
   }

   public static void main(String... strings) throws IllegalArgumentException, IllegalAccessException {
      Field[] var1 = SectionItem.class.getFields();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Field f = var1[var3];
         if (f.getType().equals(Integer.TYPE) && 0 != (f.getModifiers() & 8)) {
            System.out.printf("%s(0x%04x,0,0),//\n", f.getName(), f.get((Object)null));
         }
      }

   }

   public int size() {
      return this.size;
   }

   public int place(int offset) {
      int startOffset = offset;
      int index = 0;

      BaseItem t;
      for(Iterator var4 = this.items.iterator(); var4.hasNext(); offset = t.place(offset)) {
         t = (BaseItem)var4.next();
         offset = padding(offset, this.sectionType.alignment);
         t.offset = offset;
         t.index = index++;
      }

      this.size = offset - startOffset;
      return offset;
   }

   public void write(DataOut out) {
      out.begin("Section:" + this.sectionType);
      List<T> items = this.items;
      if (this.sectionType == SectionItem.SectionType.TYPE_STRING_DATA_ITEM) {
         StringDataItem.Buffer buff = new StringDataItem.Buffer();

         for(int i = 0; i < items.size(); ++i) {
            T t = (BaseItem)items.get(i);
            items.set(i, (Object)null);
            addPadding(out, this.sectionType.alignment);
            if (out.offset() != t.offset) {
               throw new RuntimeException();
            }

            StringDataItem stringDataItem = (StringDataItem)t;
            stringDataItem.write(out, buff);
            buff.reset();
         }
      } else {
         for(int i = 0; i < items.size(); ++i) {
            T t = (BaseItem)items.get(i);
            items.set(i, (Object)null);
            addPadding(out, this.sectionType.alignment);
            if (out.offset() != t.offset) {
               System.err.println("Error for type:" + this.sectionType + ", " + t.index);
               throw new RuntimeException();
            }

            t.write(out);
         }
      }

      out.end();
   }

   public static enum SectionType {
      TYPE_HEADER_ITEM(0, 1, 0),
      TYPE_STRING_ID_ITEM(1, 4, 0),
      TYPE_TYPE_ID_ITEM(2, 4, 0),
      TYPE_PROTO_ID_ITEM(3, 4, 0),
      TYPE_FIELD_ID_ITEM(4, 4, 0),
      TYPE_METHOD_ID_ITEM(5, 1, 0),
      TYPE_CLASS_DEF_ITEM(6, 4, 0),
      TYPE_MAP_LIST(4096, 4, 0),
      TYPE_TYPE_LIST(4097, 4, 0),
      TYPE_ANNOTATION_SET_REF_LIST(4098, 4, 0),
      TYPE_ANNOTATION_SET_ITEM(4099, 4, 0),
      TYPE_CLASS_DATA_ITEM(8192, 1, 0),
      TYPE_CODE_ITEM(8193, 4, 0),
      TYPE_STRING_DATA_ITEM(8194, 1, 0),
      TYPE_DEBUG_INFO_ITEM(8195, 1, 0),
      TYPE_ANNOTATION_ITEM(8196, 1, 0),
      TYPE_ENCODED_ARRAY_ITEM(8197, 1, 0),
      TYPE_ANNOTATIONS_DIRECTORY_ITEM(8198, 4, 0);

      public int code;
      public int alignment;

      private SectionType(int typeCode, int alignment, int size) {
         this.code = typeCode;
         this.alignment = alignment;
      }
   }
}
