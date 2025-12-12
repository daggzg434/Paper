package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Visibility;

public class DexMethodVisitor implements DexAnnotationAble {
   protected DexMethodVisitor visitor;

   public DexMethodVisitor() {
   }

   public DexMethodVisitor(DexMethodVisitor mv) {
      this.visitor = mv;
   }

   public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
      return this.visitor == null ? null : this.visitor.visitAnnotation(name, visibility);
   }

   public DexCodeVisitor visitCode() {
      return this.visitor == null ? null : this.visitor.visitCode();
   }

   public void visitEnd() {
      if (this.visitor != null) {
         this.visitor.visitEnd();
      }
   }

   public DexAnnotationAble visitParameterAnnotation(int index) {
      return this.visitor == null ? null : this.visitor.visitParameterAnnotation(index);
   }
}
