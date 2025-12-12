package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class AnnotationSetRefListItem extends BaseItem {
   public final AnnotationSetItem[] annotationSets;

   public AnnotationSetRefListItem(int size) {
      this.annotationSets = new AnnotationSetItem[size];
   }

   public int place(int offset) {
      return offset + 4 + this.annotationSets.length * 4;
   }

   public void write(DataOut out) {
      out.uint("size", this.annotationSets.length);
      AnnotationSetItem[] var2 = this.annotationSets;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         AnnotationSetItem item = var2[var4];
         out.uint("annotations_off", item == null ? 0 : item.offset);
      }

   }
}
