package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class ArrayExpr extends Value.E2Expr {
   public String elementType;

   public ArrayExpr() {
      super(Value.VT.ARRAY, (Value)null, (Value)null);
   }

   public ArrayExpr(Value base, Value index, String elementType) {
      super(Value.VT.ARRAY, base, index);
      this.elementType = elementType;
   }

   public Value clone() {
      return new ArrayExpr(this.op1.trim().clone(), this.op2.trim().clone(), this.elementType);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new ArrayExpr(this.op1.clone(mapper), this.op2.clone(mapper), this.elementType);
   }

   public String toString0() {
      return this.op1 + "[" + this.op2 + "]";
   }
}
