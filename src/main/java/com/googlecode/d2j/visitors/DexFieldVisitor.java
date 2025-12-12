package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Visibility;

public class DexFieldVisitor implements DexAnnotationAble {
   protected DexFieldVisitor visitor;

   public DexFieldVisitor(DexFieldVisitor visitor) {
      this.visitor = visitor;
   }

   public DexFieldVisitor() {
   }

   public void visitEnd() {
      if (this.visitor != null) {
         this.visitor.visitEnd();
      }
   }

   public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
      return this.visitor == null ? null : this.visitor.visitAnnotation(name, visibility);
   }
}
