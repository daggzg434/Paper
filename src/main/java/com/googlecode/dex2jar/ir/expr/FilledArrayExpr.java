package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class FilledArrayExpr extends Value.EnExpr {
   public String type;

   protected void releaseMemory() {
      this.type = null;
      super.releaseMemory();
   }

   public FilledArrayExpr(Value[] datas, String type) {
      super(Value.VT.FILLED_ARRAY, datas);
      this.type = type;
   }

   public Value clone() {
      return new FilledArrayExpr(this.cloneOps(), this.type);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new FilledArrayExpr(this.cloneOps(mapper), this.type);
   }

   public String toString0() {
      StringBuilder sb = (new StringBuilder()).append("new ").append(Util.toShortClassName(this.type)).append("[]{");

      for(int i = 0; i < this.ops.length; ++i) {
         sb.append(this.ops[i]).append(", ");
      }

      if (this.ops.length > 0) {
         sb.setLength(sb.length() - 2);
      }

      sb.append('}');
      return sb.toString();
   }
}
