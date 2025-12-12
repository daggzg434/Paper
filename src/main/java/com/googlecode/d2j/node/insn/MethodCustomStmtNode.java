package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class MethodCustomStmtNode extends AbstractMethodStmtNode {
   public final String name;
   public final Proto proto;
   public final MethodHandle bsm;
   public final Object[] bsmArgs;

   public MethodCustomStmtNode(Op op, int[] args, String name, Proto proto, MethodHandle bsm, Object[] bsmArgs) {
      super(op, args);
      this.proto = proto;
      this.name = name;
      this.bsm = bsm;
      this.bsmArgs = bsmArgs;
   }

   public void accept(DexCodeVisitor cv) {
      cv.visitMethodStmt(this.op, this.args, this.name, this.proto, this.bsm, this.bsmArgs);
   }

   public Proto getProto() {
      return this.proto;
   }
}
