package com.googlecode.d2j.node;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.node.insn.ConstStmtNode;
import com.googlecode.d2j.node.insn.DexLabelStmtNode;
import com.googlecode.d2j.node.insn.DexStmtNode;
import com.googlecode.d2j.node.insn.FieldStmtNode;
import com.googlecode.d2j.node.insn.FillArrayDataStmtNode;
import com.googlecode.d2j.node.insn.FilledNewArrayStmtNode;
import com.googlecode.d2j.node.insn.JumpStmtNode;
import com.googlecode.d2j.node.insn.MethodCustomStmtNode;
import com.googlecode.d2j.node.insn.MethodPolymorphicStmtNode;
import com.googlecode.d2j.node.insn.MethodStmtNode;
import com.googlecode.d2j.node.insn.PackedSwitchStmtNode;
import com.googlecode.d2j.node.insn.SparseSwitchStmtNode;
import com.googlecode.d2j.node.insn.Stmt0RNode;
import com.googlecode.d2j.node.insn.Stmt1RNode;
import com.googlecode.d2j.node.insn.Stmt2R1NNode;
import com.googlecode.d2j.node.insn.Stmt2RNode;
import com.googlecode.d2j.node.insn.Stmt3RNode;
import com.googlecode.d2j.node.insn.TypeStmtNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexCodeNode extends DexCodeVisitor {
   public List<DexStmtNode> stmts = new ArrayList();
   public List<TryCatchNode> tryStmts = null;
   public DexDebugNode debugNode;
   public int totalRegister = -1;

   public DexCodeNode() {
   }

   public DexCodeNode(DexCodeVisitor visitor) {
      super(visitor);
   }

   public void accept(DexCodeVisitor v) {
      Iterator var2;
      if (this.tryStmts != null) {
         var2 = this.tryStmts.iterator();

         while(var2.hasNext()) {
            TryCatchNode n = (TryCatchNode)var2.next();
            n.accept(v);
         }
      }

      if (this.debugNode != null) {
         DexDebugVisitor ddv = v.visitDebug();
         if (ddv != null) {
            this.debugNode.accept(ddv);
            ddv.visitEnd();
         }
      }

      if (this.totalRegister >= 0) {
         v.visitRegister(this.totalRegister);
      }

      var2 = this.stmts.iterator();

      while(var2.hasNext()) {
         DexStmtNode n = (DexStmtNode)var2.next();
         n.accept(v);
      }

   }

   public void accept(DexMethodVisitor v) {
      DexCodeVisitor cv = v.visitCode();
      if (cv != null) {
         this.accept(cv);
         cv.visitEnd();
      }

   }

   protected void add(DexStmtNode stmt) {
      this.stmts.add(stmt);
   }

   public void visitConstStmt(final Op op, final int ra, final Object value) {
      this.add(new ConstStmtNode(op, ra, value));
   }

   public void visitFillArrayDataStmt(final Op op, final int ra, final Object array) {
      this.add(new FillArrayDataStmtNode(op, ra, array));
   }

   public void visitFieldStmt(final Op op, final int a, final int b, final Field field) {
      this.add(new FieldStmtNode(op, a, b, field));
   }

   public void visitFilledNewArrayStmt(final Op op, final int[] args, final String type) {
      this.add(new FilledNewArrayStmtNode(op, args, type));
   }

   public void visitJumpStmt(final Op op, final int a, final int b, final DexLabel label) {
      this.add(new JumpStmtNode(op, a, b, label));
   }

   public void visitLabel(final DexLabel label) {
      this.add(new DexLabelStmtNode(label));
   }

   public void visitMethodStmt(final Op op, final int[] args, final Method method) {
      this.add(new MethodStmtNode(op, args, method));
   }

   public void visitMethodStmt(Op op, int[] args, String name, Proto proto, MethodHandle bsm, Object... bsmArgs) {
      this.add(new MethodCustomStmtNode(op, args, name, proto, bsm, bsmArgs));
   }

   public void visitMethodStmt(Op op, int[] args, Method bsm, Proto proto) {
      this.add(new MethodPolymorphicStmtNode(op, args, bsm, proto));
   }

   public void visitPackedSwitchStmt(final Op op, final int aA, final int first_case, final DexLabel[] labels) {
      this.add(new PackedSwitchStmtNode(op, aA, first_case, labels));
   }

   public void visitRegister(final int total) {
      this.totalRegister = total;
   }

   public void visitSparseSwitchStmt(final Op op, final int ra, final int[] cases, final DexLabel[] labels) {
      this.add(new SparseSwitchStmtNode(op, ra, cases, labels));
   }

   public void visitStmt0R(final Op op) {
      this.add(new Stmt0RNode(op));
   }

   public void visitStmt1R(final Op op, final int reg) {
      this.add(new Stmt1RNode(op, reg));
   }

   public void visitStmt2R(final Op op, final int a, final int b) {
      this.add(new Stmt2RNode(op, a, b));
   }

   public void visitStmt2R1N(final Op op, final int distReg, final int srcReg, final int content) {
      this.add(new Stmt2R1NNode(op, distReg, srcReg, content));
   }

   public void visitStmt3R(final Op op, final int a, final int b, final int c) {
      this.add(new Stmt3RNode(op, a, b, c));
   }

   public void visitTryCatch(final DexLabel start, final DexLabel end, final DexLabel[] handler, final String[] type) {
      if (this.tryStmts == null) {
         this.tryStmts = new ArrayList(3);
      }

      this.tryStmts.add(new TryCatchNode(start, end, handler, type));
   }

   public void visitTypeStmt(final Op op, final int a, final int b, final String type) {
      this.add(new TypeStmtNode(op, a, b, type));
   }

   public DexDebugVisitor visitDebug() {
      DexDebugNode dexDebugNode = new DexDebugNode();
      this.debugNode = dexDebugNode;
      return dexDebugNode;
   }
}
