package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;

public class DexClassVisitor implements DexAnnotationAble {
   protected DexClassVisitor visitor;

   public DexClassVisitor() {
   }

   public DexClassVisitor(DexClassVisitor dcv) {
      this.visitor = dcv;
   }

   public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
      return this.visitor == null ? null : this.visitor.visitAnnotation(name, visibility);
   }

   public void visitEnd() {
      if (this.visitor != null) {
         this.visitor.visitEnd();
      }
   }

   public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
      return this.visitor == null ? null : this.visitor.visitField(accessFlags, field, value);
   }

   public DexMethodVisitor visitMethod(int accessFlags, Method method) {
      return this.visitor == null ? null : this.visitor.visitMethod(accessFlags, method);
   }

   public void visitSource(String file) {
      if (this.visitor != null) {
         this.visitor.visitSource(file);
      }
   }
}
