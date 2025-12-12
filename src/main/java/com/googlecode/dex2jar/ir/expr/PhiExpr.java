package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class PhiExpr extends Value.EnExpr {
   public PhiExpr(Value[] ops) {
      super(Value.VT.PHI, ops);
   }

   public Value clone() {
      return new PhiExpr(this.cloneOps());
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new PhiExpr(this.cloneOps(mapper));
   }

   public String toString0() {
      StringBuilder sb = new StringBuilder("φ(");
      boolean first = true;
      Value[] var3 = this.ops;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Value vb = var3[var5];
         if (first) {
            first = false;
         } else {
            sb.append(", ");
         }

         sb.append(vb);
      }

      sb.append(")");
      return sb.toString();
   }
}
