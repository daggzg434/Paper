package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Util;

public class InvokeExpr extends AbstractInvokeExpr {
   public Method method;

   protected void releaseMemory() {
      this.method = null;
      super.releaseMemory();
   }

   public Proto getProto() {
      return this.method.getProto();
   }

   public InvokeExpr(Value.VT type, Value[] args, String ownerType, String methodName, String[] argmentTypes, String returnType) {
      super(type, args);
      this.method = new Method(ownerType, methodName, argmentTypes, returnType);
   }

   public InvokeExpr(Value.VT type, Value[] args, Method method) {
      super(type, args);
      this.method = method;
   }

   public Value clone() {
      return new InvokeExpr(this.vt, this.cloneOps(), this.method);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new InvokeExpr(this.vt, this.cloneOps(mapper), this.method);
   }

   public String toString0() {
      StringBuilder sb = new StringBuilder();
      int i = 0;
      if (super.vt == Value.VT.INVOKE_NEW) {
         sb.append("new ").append(Util.toShortClassName(this.method.getOwner()));
      } else if (super.vt == Value.VT.INVOKE_STATIC) {
         sb.append(Util.toShortClassName(this.method.getOwner())).append('.').append(this.method.getName());
      } else {
         sb.append(this.ops[i++]).append('.').append(this.method.getName());
      }

      sb.append('(');

      for(boolean first = true; i < this.ops.length; ++i) {
         if (first) {
            first = false;
         } else {
            sb.append(',');
         }

         sb.append(this.ops[i]);
      }

      sb.append(')');
      return sb.toString();
   }

   public String getOwner() {
      return this.method.getOwner();
   }

   public String getRet() {
      return this.method.getReturnType();
   }

   public String getName() {
      return this.method.getName();
   }

   public String[] getArgs() {
      return this.method.getParameterTypes();
   }
}
