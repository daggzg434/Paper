package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class FieldExpr extends Value.E1Expr {
   public String name;
   public String owner;
   public String type;

   public FieldExpr(Value object, String ownerType, String fieldName, String fieldType) {
      super(Value.VT.FIELD, object);
      this.type = fieldType;
      this.name = fieldName;
      this.owner = ownerType;
   }

   protected void releaseMemory() {
      this.name = null;
      this.owner = this.type = null;
      super.releaseMemory();
   }

   public Value clone() {
      return new FieldExpr(this.op.trim().clone(), this.owner, this.name, this.type);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new FieldExpr(this.op.clone(mapper), this.owner, this.name, this.type);
   }

   public String toString0() {
      return this.op + "." + this.name;
   }
}
