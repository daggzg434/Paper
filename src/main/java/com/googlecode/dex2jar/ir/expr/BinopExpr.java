package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class BinopExpr extends Value.E2Expr {
   public String type;

   public BinopExpr(Value.VT vt, Value op1, Value op2, String type) {
      super(vt, op1, op2);
      this.type = type;
   }

   protected void releaseMemory() {
      this.type = null;
      super.releaseMemory();
   }

   public Value clone() {
      return new BinopExpr(this.vt, this.op1.trim().clone(), this.op2.trim().clone(), this.type);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new BinopExpr(this.vt, this.op1.clone(mapper), this.op2.clone(mapper), this.type);
   }

   public String toString0() {
      return "(" + this.op1 + " " + super.vt + " " + this.op2 + ")";
   }
}
