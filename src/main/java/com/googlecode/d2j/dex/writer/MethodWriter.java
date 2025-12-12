package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetRefListItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.CodeItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

class MethodWriter extends DexMethodVisitor {
   public final ConstPool cp;
   private final ClassDataItem.EncodedMethod encodedMethod;
   final boolean isStatic;
   final Method method;
   private final int parameterSize;

   public MethodWriter(ClassDataItem.EncodedMethod encodedMethod, Method m, boolean isStatic, ConstPool cp) {
      this.encodedMethod = encodedMethod;
      this.parameterSize = m.getParameterTypes().length;
      this.cp = cp;
      this.method = m;
      this.isStatic = isStatic;
   }

   public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
      AnnotationItem annItem = new AnnotationItem(this.cp.uniqType(name), visibility);
      AnnotationSetItem asi = this.encodedMethod.annotationSetItem;
      if (asi == null) {
         asi = new AnnotationSetItem();
         this.encodedMethod.annotationSetItem = asi;
      }

      asi.annotations.add(annItem);
      return new AnnotationWriter(annItem.annotation.elements, this.cp);
   }

   public DexCodeVisitor visitCode() {
      this.encodedMethod.code = new CodeItem();
      return new CodeWriter(this.encodedMethod, this.encodedMethod.code, this.method, this.isStatic, this.cp);
   }

   public DexAnnotationAble visitParameterAnnotation(final int index) {
      return new DexAnnotationAble() {
         public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
            AnnotationSetRefListItem asrl = MethodWriter.this.encodedMethod.parameterAnnotation;
            if (asrl == null) {
               asrl = new AnnotationSetRefListItem(MethodWriter.this.parameterSize);
               MethodWriter.this.encodedMethod.parameterAnnotation = asrl;
            }

            AnnotationSetItem asi = asrl.annotationSets[index];
            if (asi == null) {
               asi = new AnnotationSetItem();
               asrl.annotationSets[index] = asi;
            }

            AnnotationItem annItem = new AnnotationItem(MethodWriter.this.cp.uniqType(name), visibility);
            asi.annotations.add(annItem);
            return new AnnotationWriter(annItem.annotation.elements, MethodWriter.this.cp);
         }
      };
   }
}
