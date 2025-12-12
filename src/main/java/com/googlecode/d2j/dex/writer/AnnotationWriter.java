package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.dex.writer.ev.EncodedAnnotation;
import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import java.util.List;

class AnnotationWriter extends DexAnnotationVisitor {
   ConstPool cp;
   List<EncodedAnnotation.AnnotationElement> elements;

   public AnnotationWriter(List<EncodedAnnotation.AnnotationElement> elements, ConstPool cp) {
      this.elements = elements;
      this.cp = cp;
   }

   EncodedAnnotation.AnnotationElement newAnnotationElement(String name) {
      EncodedAnnotation.AnnotationElement ae = new EncodedAnnotation.AnnotationElement();
      ae.name = this.cp.uniqString(name);
      this.elements.add(ae);
      return ae;
   }

   public void visit(String name, Object value) {
      if (value instanceof Object[]) {
         DexAnnotationVisitor s = this.visitArray(name);
         if (s != null) {
            Object[] var4 = (Object[])((Object[])value);
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               Object v = var4[var6];
               s.visit((String)null, v);
            }

            s.visitEnd();
         }
      } else {
         EncodedAnnotation.AnnotationElement ae = this.newAnnotationElement(name);
         ae.value = EncodedValue.wrap(this.cp.wrapEncodedItem(value));
      }

   }

   public DexAnnotationVisitor visitAnnotation(String name, String desc) {
      EncodedAnnotation encodedAnnotation = new EncodedAnnotation();
      encodedAnnotation.type = this.cp.uniqType(desc);
      EncodedValue encodedValue = new EncodedValue(29, encodedAnnotation);
      EncodedAnnotation.AnnotationElement ae = this.newAnnotationElement(name);
      ae.value = encodedValue;
      return new AnnotationWriter(encodedAnnotation.elements, this.cp);
   }

   public DexAnnotationVisitor visitArray(String name) {
      EncodedAnnotation.AnnotationElement ae = this.newAnnotationElement(name);
      EncodedArray encodedArray = new EncodedArray();
      ae.value = new EncodedValue(28, encodedArray);
      return new AnnotationWriter.EncodedArrayAnnWriter(encodedArray);
   }

   public void visitEnum(String name, String fower, String fname) {
      EncodedAnnotation.AnnotationElement ae = this.newAnnotationElement(name);
      ae.value = new EncodedValue(27, this.cp.uniqField(fower, fname, fower));
   }

   class EncodedArrayAnnWriter extends DexAnnotationVisitor {
      final EncodedArray encodedArray;

      public EncodedArrayAnnWriter(EncodedArray encodedArray) {
         this.encodedArray = encodedArray;
      }

      public void visit(String name, Object value) {
         EncodedValue encodedValue;
         if (value instanceof String) {
            encodedValue = new EncodedValue(23, AnnotationWriter.this.cp.uniqString((String)value));
         } else if (value instanceof DexType) {
            encodedValue = new EncodedValue(24, AnnotationWriter.this.cp.uniqType(((DexType)value).desc));
         } else {
            encodedValue = EncodedValue.wrap(value);
         }

         this.encodedArray.values.add(encodedValue);
      }

      public DexAnnotationVisitor visitAnnotation(String name, String desc) {
         EncodedAnnotation encodedAnnotation = new EncodedAnnotation();
         encodedAnnotation.type = AnnotationWriter.this.cp.uniqType(desc);
         EncodedValue encodedValue = new EncodedValue(29, encodedAnnotation);
         this.encodedArray.values.add(encodedValue);
         return new AnnotationWriter(encodedAnnotation.elements, AnnotationWriter.this.cp);
      }

      public DexAnnotationVisitor visitArray(String name) {
         EncodedValue encodedValue = new EncodedValue(28, this.encodedArray);
         this.encodedArray.values.add(encodedValue);
         return AnnotationWriter.this.new EncodedArrayAnnWriter(this.encodedArray);
      }

      public void visitEnum(String name, String fower, String fname) {
         EncodedValue encodedValue = new EncodedValue(27, AnnotationWriter.this.cp.uniqField(fower, fname, fower));
         this.encodedArray.values.add(encodedValue);
      }
   }
}
