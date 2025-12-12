package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ClassDataItem extends BaseItem {
   public final List<ClassDataItem.EncodedField> staticFields = new ArrayList(5);
   public final List<ClassDataItem.EncodedField> instanceFields = new ArrayList(5);
   public final List<ClassDataItem.EncodedMethod> directMethods = new ArrayList(5);
   public final List<ClassDataItem.EncodedMethod> virtualMethods = new ArrayList(5);

   public int place(int offset) {
      offset += lengthOfUleb128(this.staticFields.size());
      offset += lengthOfUleb128(this.instanceFields.size());
      offset += lengthOfUleb128(this.directMethods.size());
      offset += lengthOfUleb128(this.virtualMethods.size());
      offset = this.placeField(offset, this.staticFields);
      offset = this.placeField(offset, this.instanceFields);
      offset = this.placeMethod(offset, this.directMethods);
      offset = this.placeMethod(offset, this.virtualMethods);
      return offset;
   }

   private int placeMethod(int offset, List<ClassDataItem.EncodedMethod> methods) {
      if (methods.size() == 0) {
         return offset;
      } else {
         int lastIdx = 0;

         ClassDataItem.EncodedMethod f;
         for(Iterator var4 = methods.iterator(); var4.hasNext(); lastIdx = f.method.index) {
            f = (ClassDataItem.EncodedMethod)var4.next();
            offset += lengthOfUleb128(f.method.index - lastIdx);
            offset += lengthOfUleb128(f.accessFlags);
            offset += lengthOfUleb128(f.code == null ? 0 : f.code.offset);
         }

         return offset;
      }
   }

   private int placeField(int offset, List<ClassDataItem.EncodedField> fields) {
      if (fields.size() == 0) {
         return offset;
      } else {
         int lastIdx = 0;

         ClassDataItem.EncodedField f;
         for(Iterator var4 = fields.iterator(); var4.hasNext(); lastIdx = f.field.index) {
            f = (ClassDataItem.EncodedField)var4.next();
            offset += lengthOfUleb128(f.field.index - lastIdx);
            offset += lengthOfUleb128(f.accessFlags);
         }

         return offset;
      }
   }

   public void write(DataOut out) {
      out.uleb128("static_fields_size", this.staticFields.size());
      out.uleb128("instance_fields_size", this.instanceFields.size());
      out.uleb128("ditect_methods_size", this.directMethods.size());
      out.uleb128("virtual_methods_size", this.virtualMethods.size());
      this.writeField(out, this.staticFields);
      this.writeField(out, this.instanceFields);
      this.writeMethod(out, this.directMethods);
      this.writeMethod(out, this.virtualMethods);
   }

   private void writeMethod(DataOut out, List<ClassDataItem.EncodedMethod> methods) {
      if (methods != null && methods.size() != 0) {
         int lastIdx = 0;

         ClassDataItem.EncodedMethod f;
         for(Iterator var4 = methods.iterator(); var4.hasNext(); lastIdx = f.method.index) {
            f = (ClassDataItem.EncodedMethod)var4.next();
            out.uleb128("method_idx_diff", f.method.index - lastIdx);
            out.uleb128("access_flags", f.accessFlags);
            out.uleb128("code_off", f.code == null ? 0 : f.code.offset);
         }

      }
   }

   private void writeField(DataOut out, List<ClassDataItem.EncodedField> fields) {
      if (fields != null && fields.size() != 0) {
         int lastIdx = 0;

         ClassDataItem.EncodedField f;
         for(Iterator var4 = fields.iterator(); var4.hasNext(); lastIdx = f.field.index) {
            f = (ClassDataItem.EncodedField)var4.next();
            out.uleb128("field_idx_diff", f.field.index - lastIdx);
            out.uleb128("access_flags", f.accessFlags);
         }

      }
   }

   public int getMemberSize() {
      return this.instanceFields.size() + this.staticFields.size() + this.directMethods.size() + this.virtualMethods.size();
   }

   public void prepare(ConstPool cp) {
      Comparator<ClassDataItem.EncodedField> fc = new Comparator<ClassDataItem.EncodedField>() {
         public int compare(ClassDataItem.EncodedField arg0, ClassDataItem.EncodedField arg1) {
            return arg0.field.compareTo(arg1.field);
         }
      };
      Comparator<ClassDataItem.EncodedMethod> mc = new Comparator<ClassDataItem.EncodedMethod>() {
         public int compare(ClassDataItem.EncodedMethod arg0, ClassDataItem.EncodedMethod arg1) {
            return arg0.method.compareTo(arg1.method);
         }
      };
      Collections.sort(this.instanceFields, fc);
      Collections.sort(this.staticFields, fc);
      Collections.sort(this.directMethods, mc);
      Collections.sort(this.virtualMethods, mc);
   }

   public static class EncodedMethod {
      public int accessFlags;
      public MethodIdItem method;
      public CodeItem code;
      public AnnotationSetItem annotationSetItem;
      public AnnotationSetRefListItem parameterAnnotation;
   }

   public static class EncodedField {
      public int accessFlags;
      public FieldIdItem field;
      public EncodedValue staticValue;
      public AnnotationSetItem annotationSetItem;
   }
}
