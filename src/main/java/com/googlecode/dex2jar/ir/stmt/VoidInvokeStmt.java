package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

public class VoidInvokeStmt extends Stmt.E1Stmt {
   public VoidInvokeStmt(Value op) {
      super(Stmt.ST.VOID_INVOKE, op);
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      return new VoidInvokeStmt(this.op.clone(mapper));
   }

   public String toString() {
      return "void " + this.op;
   }
}
