package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class NewMutiArrayExpr extends Value.EnExpr {
   public String baseType;
   public int dimension;

   public NewMutiArrayExpr(String base, int dimension, Value[] sizes) {
      super(Value.VT.NEW_MUTI_ARRAY, sizes);
      this.baseType = base;
      this.dimension = dimension;
   }

   protected void releaseMemory() {
      this.baseType = null;
      super.releaseMemory();
   }

   public Value clone() {
      return new NewMutiArrayExpr(this.baseType, this.dimension, this.cloneOps());
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new NewMutiArrayExpr(this.baseType, this.dimension, this.cloneOps(mapper));
   }

   public String toString0() {
      StringBuilder sb = new StringBuilder();
      sb.append("new ").append(Util.toShortClassName(this.baseType));
      Value[] var2 = this.ops;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Value op = var2[var4];
         sb.append('[').append(op).append(']');
      }

      for(int i = this.ops.length; i < this.dimension; ++i) {
         sb.append("[]");
      }

      return sb.toString();
   }
}
