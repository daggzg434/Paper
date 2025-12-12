package com.googlecode.d2j.visitors;

public class DexFileVisitor {
   protected DexFileVisitor visitor;

   public DexFileVisitor() {
   }

   public DexFileVisitor(DexFileVisitor visitor) {
      this.visitor = visitor;
   }

   public void visitDexFileVersion(int version) {
      if (this.visitor != null) {
         this.visitor.visitDexFileVersion(version);
      }

   }

   public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
      return this.visitor == null ? null : this.visitor.visit(access_flags, className, superClass, interfaceNames);
   }

   public void visitEnd() {
      if (this.visitor != null) {
         this.visitor.visitEnd();
      }
   }
}
