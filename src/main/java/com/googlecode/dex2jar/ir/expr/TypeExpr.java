package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class TypeExpr extends Value.E1Expr {
   public String type;

   protected void releaseMemory() {
      this.type = null;
      super.releaseMemory();
   }

   public TypeExpr(Value.VT vt, Value value, String desc) {
      super(vt, value);
      this.type = desc;
   }

   public Value clone() {
      return new TypeExpr(this.vt, this.op.trim().clone(), this.type);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new TypeExpr(this.vt, this.op.clone(mapper), this.type);
   }

   public String toString0() {
      switch(super.vt) {
      case CHECK_CAST:
         return "((" + Util.toShortClassName(this.type) + ")" + this.op + ")";
      case INSTANCE_OF:
         return "(" + this.op + " instanceof " + Util.toShortClassName(this.type) + ")";
      case NEW_ARRAY:
         if (this.type.charAt(0) != '[') {
            return "new " + Util.toShortClassName(this.type) + "[" + this.op + "]";
         }

         int dimension;
         for(dimension = 1; this.type.charAt(dimension) == '['; ++dimension) {
         }

         StringBuilder sb = (new StringBuilder("new ")).append(Util.toShortClassName(this.type.substring(dimension))).append("[").append(this.op).append("]");

         for(int i = 0; i < dimension; ++i) {
            sb.append("[]");
         }

         return sb.toString();
      default:
         return "UNKNOW";
      }
   }
}
