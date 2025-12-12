package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class RefExpr extends Value.E0Expr {
   public int parameterIndex;
   public String type;

   protected void releaseMemory() {
      this.type = null;
      super.releaseMemory();
   }

   public RefExpr(Value.VT vt, String refType, int index) {
      super(vt);
      this.type = refType;
      this.parameterIndex = index;
   }

   public Value clone() {
      return new RefExpr(this.vt, this.type, this.parameterIndex);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new RefExpr(this.vt, this.type, this.parameterIndex);
   }

   public String toString0() {
      switch(this.vt) {
      case THIS_REF:
         return "@this";
      case PARAMETER_REF:
         return "@parameter_" + this.parameterIndex;
      case EXCEPTION_REF:
         return "@Exception";
      default:
         return super.toString();
      }
   }
}
