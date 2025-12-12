package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class AnnotationsDirectoryItem extends BaseItem {
   public AnnotationSetItem classAnnotations;
   public Map<FieldIdItem, AnnotationSetItem> fieldAnnotations;
   public Map<MethodIdItem, AnnotationSetItem> methodAnnotations;
   public Map<MethodIdItem, AnnotationSetRefListItem> parameterAnnotations;

   public int place(int offset) {
      offset += 16;
      if (this.fieldAnnotations != null) {
         offset += this.fieldAnnotations.size() * 8;
      }

      if (this.methodAnnotations != null) {
         offset += this.methodAnnotations.size() * 8;
      }

      if (this.parameterAnnotations != null) {
         offset += this.parameterAnnotations.size() * 8;
      }

      return offset;
   }

   public void write(DataOut out) {
      out.uint("class_annotations_off", this.classAnnotations == null ? 0 : this.classAnnotations.offset);
      out.uint("fields_size", this.fieldAnnotations == null ? 0 : this.fieldAnnotations.size());
      out.uint("annotated_methods_size", this.methodAnnotations == null ? 0 : this.methodAnnotations.size());
      out.uint("annotated_parameter_size", this.parameterAnnotations == null ? 0 : this.parameterAnnotations.size());
      Iterator var2;
      Entry fe;
      if (this.fieldAnnotations != null) {
         var2 = this.fieldAnnotations.entrySet().iterator();

         while(var2.hasNext()) {
            fe = (Entry)var2.next();
            out.uint("field_idx", ((FieldIdItem)fe.getKey()).index);
            out.uint("annotations_off", ((AnnotationSetItem)fe.getValue()).offset);
         }
      }

      if (this.methodAnnotations != null) {
         var2 = this.methodAnnotations.entrySet().iterator();

         while(var2.hasNext()) {
            fe = (Entry)var2.next();
            out.uint("method_idx", ((MethodIdItem)fe.getKey()).index);
            out.uint("annotations_off", ((AnnotationSetItem)fe.getValue()).offset);
         }
      }

      if (this.parameterAnnotations != null) {
         var2 = this.parameterAnnotations.entrySet().iterator();

         while(var2.hasNext()) {
            fe = (Entry)var2.next();
            out.uint("method_idx", ((MethodIdItem)fe.getKey()).index);
            out.uint("annotations_off", ((AnnotationSetRefListItem)fe.getValue()).offset);
         }
      }

   }
}
