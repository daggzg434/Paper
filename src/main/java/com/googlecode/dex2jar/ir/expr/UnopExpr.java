package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class UnopExpr extends Value.E1Expr {
   public String type;

   protected void releaseMemory() {
      this.type = null;
      super.releaseMemory();
   }

   public UnopExpr(Value.VT vt, Value value, String type) {
      super(vt, value);
      this.type = type;
   }

   public Value clone() {
      return new UnopExpr(this.vt, this.op.trim().clone(), this.type);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new UnopExpr(this.vt, this.op.clone(mapper), this.type);
   }

   public String toString0() {
      switch(this.vt) {
      case LENGTH:
         return this.op + ".length";
      case NEG:
         return "(-" + this.op + ")";
      case NOT:
         return "(!" + this.op + ")";
      default:
         return super.toString();
      }
   }
}
