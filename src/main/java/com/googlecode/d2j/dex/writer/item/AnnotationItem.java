package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.ev.EncodedAnnotation;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class AnnotationItem extends BaseItem {
   public final Visibility visibility;
   public final EncodedAnnotation annotation = new EncodedAnnotation();

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AnnotationItem that = (AnnotationItem)o;
         if (!this.annotation.equals(that.annotation)) {
            return false;
         } else {
            return this.visibility == that.visibility;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.visibility.hashCode();
      result = 31 * result + this.annotation.hashCode();
      return result;
   }

   public AnnotationItem(TypeIdItem type, Visibility visibility) {
      this.visibility = visibility;
      this.annotation.type = type;
   }

   public int place(int offset) {
      ++offset;
      return this.annotation.place(offset);
   }

   public void write(DataOut out) {
      out.ubyte("visibility", this.visibility.value);
      this.annotation.write(out);
   }
}
