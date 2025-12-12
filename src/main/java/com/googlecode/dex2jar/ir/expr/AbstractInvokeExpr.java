package com.googlecode.dex2jar.ir.expr;

import com.googlecode.d2j.Proto;

public abstract class AbstractInvokeExpr extends Value.EnExpr {
   protected void releaseMemory() {
      super.releaseMemory();
   }

   public abstract Proto getProto();

   public AbstractInvokeExpr(Value.VT type, Value[] args) {
      super(type, args);
   }
}
