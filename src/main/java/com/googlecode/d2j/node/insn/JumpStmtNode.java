package com.googlecode.d2j.node.insn;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;

public class JumpStmtNode extends DexStmtNode {
   public final int a;
   public final int b;
   public final DexLabel label;

   public JumpStmtNode(Op op, int a, int b, DexLabel label) {
      super(op);
      this.a = a;
      this.b = b;
      this.label = label;
   }

   public void accept(DexCodeVisitor cv) {
      cv.visitJumpStmt(this.op, this.a, this.b, this.label);
   }
}
