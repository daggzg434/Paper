package com.googlecode.d2j.visitors;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.reader.Op;

public class DexCodeVisitor {
   protected DexCodeVisitor visitor;

   public DexCodeVisitor() {
   }

   public DexCodeVisitor(DexCodeVisitor visitor) {
      this.visitor = visitor;
   }

   public void visitRegister(int total) {
      if (this.visitor != null) {
         this.visitor.visitRegister(total);
      }

   }

   public void visitStmt2R1N(Op op, int distReg, int srcReg, int content) {
      if (this.visitor != null) {
         this.visitor.visitStmt2R1N(op, distReg, srcReg, content);
      }

   }

   public void visitStmt3R(Op op, int a, int b, int c) {
      if (this.visitor != null) {
         this.visitor.visitStmt3R(op, a, b, c);
      }

   }

   public void visitTypeStmt(Op op, int a, int b, String type) {
      if (this.visitor != null) {
         this.visitor.visitTypeStmt(op, a, b, type);
      }

   }

   public void visitConstStmt(Op op, int ra, Object value) {
      if (this.visitor != null) {
         this.visitor.visitConstStmt(op, ra, value);
      }

   }

   public void visitFillArrayDataStmt(Op op, int ra, Object array) {
      if (this.visitor != null) {
         this.visitor.visitFillArrayDataStmt(op, ra, array);
      }

   }

   public void visitEnd() {
      if (this.visitor != null) {
         this.visitor.visitEnd();
      }

   }

   public void visitFieldStmt(Op op, int a, int b, Field field) {
      if (this.visitor != null) {
         this.visitor.visitFieldStmt(op, a, b, field);
      }

   }

   public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
      if (this.visitor != null) {
         this.visitor.visitFilledNewArrayStmt(op, args, type);
      }

   }

   public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
      if (this.visitor != null) {
         this.visitor.visitJumpStmt(op, a, b, label);
      }

   }

   public void visitLabel(DexLabel label) {
      if (this.visitor != null) {
         this.visitor.visitLabel(label);
      }

   }

   public void visitSparseSwitchStmt(Op op, int ra, int[] cases, DexLabel[] labels) {
      if (this.visitor != null) {
         this.visitor.visitSparseSwitchStmt(op, ra, cases, labels);
      }

   }

   public void visitMethodStmt(Op op, int[] args, Method method) {
      if (this.visitor != null) {
         this.visitor.visitMethodStmt(op, args, method);
      }

   }

   public void visitMethodStmt(Op op, int[] args, String name, Proto proto, MethodHandle bsm, Object... bsmArgs) {
      if (this.visitor != null) {
         this.visitor.visitMethodStmt(op, args, name, proto, bsm, bsmArgs);
      }

   }

   public void visitMethodStmt(Op op, int[] args, Method bsm, Proto proto) {
      if (this.visitor != null) {
         this.visitor.visitMethodStmt(op, args, bsm, proto);
      }

   }

   public void visitStmt2R(Op op, int a, int b) {
      if (this.visitor != null) {
         this.visitor.visitStmt2R(op, a, b);
      }

   }

   public void visitStmt0R(Op op) {
      if (this.visitor != null) {
         this.visitor.visitStmt0R(op);
      }

   }

   public void visitStmt1R(Op op, int reg) {
      if (this.visitor != null) {
         this.visitor.visitStmt1R(op, reg);
      }

   }

   public void visitPackedSwitchStmt(Op op, int aA, int first_case, DexLabel[] labels) {
      if (this.visitor != null) {
         this.visitor.visitPackedSwitchStmt(op, aA, first_case, labels);
      }

   }

   public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handler, String[] type) {
      if (this.visitor != null) {
         this.visitor.visitTryCatch(start, end, handler, type);
      }

   }

   public DexDebugVisitor visitDebug() {
      return this.visitor != null ? this.visitor.visitDebug() : null;
   }
}
