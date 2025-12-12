package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class CastExpr extends Value.E1Expr {
   public String from;
   public String to;

   public CastExpr(Value value, String from, String to) {
      super(Value.VT.CAST, value);
      this.from = from;
      this.to = to;
   }

   protected void releaseMemory() {
      this.from = this.to = null;
      super.releaseMemory();
   }

   public Value clone() {
      return new CastExpr(this.op.trim().clone(), this.from, this.to);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new CastExpr(this.op.clone(mapper), this.from, this.to);
   }

   public String toString0() {
      return "((" + Util.toShortClassName(this.to) + ")" + this.op + ")";
   }
}
