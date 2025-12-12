package com.googlecode.d2j.visitors;

import com.googlecode.d2j.DexLabel;

public class DexDebugVisitor {
   protected DexDebugVisitor visitor;

   public DexDebugVisitor() {
   }

   public DexDebugVisitor(DexDebugVisitor visitor) {
      this.visitor = visitor;
   }

   public void visitParameterName(int parameterIndex, String name) {
      if (this.visitor != null) {
         this.visitor.visitParameterName(parameterIndex, name);
      }

   }

   public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
      if (this.visitor != null) {
         this.visitor.visitStartLocal(reg, label, name, type, signature);
      }

   }

   public void visitLineNumber(int line, DexLabel label) {
      if (this.visitor != null) {
         this.visitor.visitLineNumber(line, label);
      }

   }

   public void visitEndLocal(int reg, DexLabel label) {
      if (this.visitor != null) {
         this.visitor.visitEndLocal(reg, label);
      }

   }

   public void visitSetFile(String file) {
      if (this.visitor != null) {
         this.visitor.visitSetFile(file);
      }

   }

   public void visitPrologue(DexLabel dexLabel) {
      if (this.visitor != null) {
         this.visitor.visitPrologue(dexLabel);
      }

   }

   public void visitEpiogue(DexLabel dexLabel) {
      if (this.visitor != null) {
         this.visitor.visitEpiogue(dexLabel);
      }

   }

   public void visitRestartLocal(int reg, DexLabel label) {
      if (this.visitor != null) {
         this.visitor.visitRestartLocal(reg, label);
      }

   }

   public void visitEnd() {
      if (this.visitor != null) {
         this.visitor.visitEnd();
      }

   }
}
