package com.googlecode.d2j.visitors;

public class DexAnnotationVisitor {
   protected DexAnnotationVisitor visitor;

   public DexAnnotationVisitor() {
   }

   public DexAnnotationVisitor(DexAnnotationVisitor visitor) {
      this.visitor = visitor;
   }

   public void visit(String name, Object value) {
      if (this.visitor != null) {
         this.visitor.visit(name, value);
      }

   }

   public void visitEnum(String name, String desc, String value) {
      if (this.visitor != null) {
         this.visitor.visitEnum(name, desc, value);
      }

   }

   public DexAnnotationVisitor visitAnnotation(String name, String desc) {
      return this.visitor != null ? this.visitor.visitAnnotation(name, desc) : null;
   }

   public DexAnnotationVisitor visitArray(String name) {
      return this.visitor != null ? this.visitor.visitArray(name) : null;
   }

   public void visitEnd() {
      if (this.visitor != null) {
         this.visitor.visitEnd();
      }

   }
}
