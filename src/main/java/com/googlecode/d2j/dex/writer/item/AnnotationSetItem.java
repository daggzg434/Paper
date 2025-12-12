package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AnnotationSetItem extends BaseItem {
   public List<AnnotationItem> annotations = new ArrayList(3);
   private static final Comparator<AnnotationItem> cmp = new Comparator<AnnotationItem>() {
      public int compare(AnnotationItem o1, AnnotationItem o2) {
         return o1.annotation.type.compareTo(o2.annotation.type);
      }
   };

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AnnotationSetItem that = (AnnotationSetItem)o;
         return this.annotations.equals(that.annotations);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.annotations.hashCode();
   }

   public int place(int offset) {
      return offset + 4 + this.annotations.size() * 4;
   }

   public void write(DataOut out) {
      Collections.sort(this.annotations, cmp);
      out.uint("size", this.annotations.size());
      Iterator var2 = this.annotations.iterator();

      while(var2.hasNext()) {
         AnnotationItem item = (AnnotationItem)var2.next();
         out.uint("annotation_off", item.offset);
      }

   }
}
