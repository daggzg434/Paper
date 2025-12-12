package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class InvokeCustomExpr extends AbstractInvokeExpr {
   public String name;
   public Proto proto;
   public MethodHandle handle;
   public Object[] bsmArgs;

   protected void releaseMemory() {
      this.name = null;
      this.proto = null;
      this.handle = null;
      this.bsmArgs = null;
      super.releaseMemory();
   }

   public Proto getProto() {
      return this.proto;
   }

   public InvokeCustomExpr(Value.VT type, Value[] args, String methodName, Proto proto, MethodHandle handle, Object[] bsmArgs) {
      super(type, args);
      this.proto = proto;
      this.name = methodName;
      this.handle = handle;
      this.bsmArgs = bsmArgs;
   }

   public Value clone() {
      return new InvokeCustomExpr(this.vt, this.cloneOps(), this.name, this.proto, this.handle, this.bsmArgs);
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return new InvokeCustomExpr(this.vt, this.cloneOps(mapper), this.name, this.proto, this.handle, this.bsmArgs);
   }

   public String toString0() {
      StringBuilder sb = new StringBuilder();
      sb.append("InvokeCustomExpr(....)");
      return sb.toString();
   }
}
