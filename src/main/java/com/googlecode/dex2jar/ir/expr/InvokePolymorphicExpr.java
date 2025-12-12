package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class InvokePolymorphicExpr extends AbstractInvokeExpr {
   public Proto proto;
   public Method method;

   protected void releaseMemory() {
      this.method = null;
      this.proto = null;
      super.releaseMemory();
   }

   public Proto getProto() {
      return this.proto;
   }

   public InvokePolymorphicExpr(Value.VT type, Value[] args, Proto proto, Method method) {
      super(type, args);
      this.proto = proto;
      this.method = method;
   }

   public Value clone() {
      return new InvokePolymorphicExpr(this.vt, this.cloneOps(), this.proto, this.method);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new InvokePolymorphicExpr(this.vt, this.cloneOps(mapper), this.proto, this.method);
   }

   public String toString0() {
      StringBuilder sb = new StringBuilder();
      int i = 0;
      int i = i + 1;
      sb.append(this.ops[i]).append('.').append(this.method.getName());
      String[] argTypes = this.getProto().getParameterTypes();
      sb.append('(');
      int j = 0;

      for(boolean first = true; i < this.ops.length; ++i) {
         if (first) {
            first = false;
         } else {
            sb.append(',');
         }

         sb.append("(").append(Util.toShortClassName(argTypes[j++])).append(")").append(this.ops[i]);
      }

      sb.append(')');
      return sb.toString();
   }
}
