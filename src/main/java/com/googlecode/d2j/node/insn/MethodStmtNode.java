package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class MethodStmtNode extends AbstractMethodStmtNode {
   public final Method method;

   public MethodStmtNode(Op op, int[] args, Method method) {
      super(op, args);
      this.method = method;
   }

   public void accept(DexCodeVisitor cv) {
      cv.visitMethodStmt(this.op, this.args, this.method);
   }

   public Proto getProto() {
      return this.method.getProto();
   }
}
