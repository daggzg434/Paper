package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class NewExpr extends Value.E0Expr {
   public String type;

   public NewExpr(String type) {
      super(Value.VT.NEW);
      this.type = type;
   }

   public Value clone() {
      return new NewExpr(this.type);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new NewExpr(this.type);
   }

   protected void releaseMemory() {
      this.type = null;
      super.releaseMemory();
   }

   public String toString0() {
      return "NEW " + Util.toShortClassName(this.type);
   }
}
