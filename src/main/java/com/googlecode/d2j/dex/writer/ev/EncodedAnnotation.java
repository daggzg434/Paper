package com.googlecode.d2j.dex.writer.ev;

import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import com.googlecode.d2j.dex.writer.item.StringIdItem;
import com.googlecode.d2j.dex.writer.item.TypeIdItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EncodedAnnotation {
   public TypeIdItem type;
   public final List<EncodedAnnotation.AnnotationElement> elements = new ArrayList(5);

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EncodedAnnotation that = (EncodedAnnotation)o;
         if (!this.elements.equals(that.elements)) {
            return false;
         } else {
            return this.type.equals(that.type);
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.type.hashCode();
      result = 31 * result + this.elements.hashCode();
      return result;
   }

   public int place(int offset) {
      offset += BaseItem.lengthOfUleb128(this.type.index);
      offset += BaseItem.lengthOfUleb128(this.elements.size());

      EncodedAnnotation.AnnotationElement ae;
      for(Iterator var2 = this.elements.iterator(); var2.hasNext(); offset = ae.value.place(offset)) {
         ae = (EncodedAnnotation.AnnotationElement)var2.next();
         offset += BaseItem.lengthOfUleb128(ae.name.index);
      }

      return offset;
   }

   public void write(DataOut out) {
      out.uleb128("type_idx", this.type.index);
      out.uleb128("size", this.elements.size());
      Iterator var2 = this.elements.iterator();

      while(var2.hasNext()) {
         EncodedAnnotation.AnnotationElement ae = (EncodedAnnotation.AnnotationElement)var2.next();
         out.uleb128("name_idx", ae.name.index);
         ae.value.write(out);
      }

   }

   public static class AnnotationElement {
      public StringIdItem name;
      public EncodedValue value;

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            EncodedAnnotation.AnnotationElement that = (EncodedAnnotation.AnnotationElement)o;
            if (!this.name.equals(that.name)) {
               return false;
            } else {
               return this.value.equals(that.value);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.name.hashCode();
         result = 31 * result + this.value.hashCode();
         return result;
      }
   }
}
