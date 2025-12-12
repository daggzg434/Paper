package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.DexType;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;
import java.lang.reflect.Array;

public class Constant extends Value.E0Expr {
   public static final Object Null = new Object();
   public Object value;

   public Constant(Object value) {
      super(Value.VT.CONSTANT);
      this.value = value;
   }

   public Value clone() {
      return new Constant(this.value);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new Constant(this.value);
   }

   public String toString0() {
      if (Null == this.value) {
         return "null";
      } else if (this.value == null) {
         return "NULL";
      } else if (this.value instanceof Number) {
         if (this.value instanceof Float) {
            return this.value.toString() + "F";
         } else {
            return this.value instanceof Long ? this.value.toString() + "L" : this.value.toString();
         }
      } else if (this.value instanceof String) {
         StringBuffer buf = new StringBuffer();
         Util.appendString(buf, (String)this.value);
         return buf.toString();
      } else if (this.value instanceof DexType) {
         return Util.toShortClassName(((DexType)this.value).desc) + ".class";
      } else if (!this.value.getClass().isArray()) {
         return "" + this.value;
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append("[");
         int size = Array.getLength(this.value);

         for(int i = 0; i < size; ++i) {
            sb.append(Array.get(this.value, i)).append(",");
         }

         if (size > 0) {
            sb.setLength(sb.length() - 1);
         }

         sb.append("]");
         return sb.toString();
      }
   }
}
