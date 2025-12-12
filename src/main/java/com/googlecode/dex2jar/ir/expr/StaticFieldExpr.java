package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class StaticFieldExpr extends Value.E0Expr {
   public String name;
   public String owner;
   public String type;

   protected void releaseMemory() {
      this.name = null;
      this.owner = this.type = null;
      super.releaseMemory();
   }

   public StaticFieldExpr(String ownerType, String fieldName, String fieldType) {
      super(Value.VT.STATIC_FIELD);
      this.type = fieldType;
      this.name = fieldName;
      this.owner = ownerType;
   }

   public Value clone() {
      return new StaticFieldExpr(this.owner, this.name, this.type);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new StaticFieldExpr(this.owner, this.name, this.type);
   }

   public String toString0() {
      return Util.toShortClassName(this.owner) + "." + this.name;
   }
}
