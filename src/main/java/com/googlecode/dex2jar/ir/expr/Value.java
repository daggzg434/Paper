package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public abstract class Value implements Cloneable {
   public static final int CAN_THROW = 8;
   public static final int MAY_THROW = 16;
   public final ET et;
   private Value next;
   public String valueType;
   public Object tag;
   public final Value.VT vt;

   public void setOp(Value op) {
   }

   public void setOp1(Value op) {
   }

   public void setOp2(Value op) {
   }

   public void setOps(Value[] op) {
   }

   protected Value(Value.VT vt, ET et) {
      this.vt = vt;
      this.et = et;
   }

   public abstract Value clone();

   public abstract Value clone(LabelAndLocalMapper mapper);

   public Value getOp() {
      return null;
   }

   public Value getOp1() {
      return null;
   }

   public Value getOp2() {
      return null;
   }

   public Value[] getOps() {
      return null;
   }

   protected void releaseMemory() {
   }

   public final String toString() {
      return this.trim().toString0();
   }

   protected abstract String toString0();

   public Value trim() {
      Value a;
      Value b;
      for(a = this; a.next != null; a = b) {
         b = a.next;
         a.next = b;
      }

      return a;
   }

   public static enum VT {
      ADD("+", 16),
      AND("&", 16),
      ARRAY(16),
      CAST(16),
      CHECK_CAST(8),
      CONSTANT(0),
      DCMPG(16),
      DCMPL(16),
      IDIV("/", 8),
      LDIV("/", 8),
      FDIV("/", 16),
      DDIV("/", 16),
      EQ("==", 16),
      EXCEPTION_REF(0),
      FCMPG(16),
      FCMPL(16),
      FIELD(8),
      FILLED_ARRAY(8),
      GE(">=", 16),
      GT(">", 16),
      INSTANCE_OF(8),
      INVOKE_INTERFACE(8),
      INVOKE_NEW(8),
      INVOKE_SPECIAL(8),
      INVOKE_STATIC(8),
      INVOKE_VIRTUAL(8),
      INVOKE_CUSTOM(8),
      INVOKE_POLYMORPHIC(8),
      LCMP(16),
      LE("<=", 16),
      LENGTH(8),
      LOCAL(0),
      LT("<", 16),
      MUL("*", 16),
      NE("!=", 16),
      NEG(16),
      NEW(8),
      NEW_ARRAY(8),
      NEW_MUTI_ARRAY(8),
      NOT(16),
      OR("|", 16),
      PARAMETER_REF(0),
      PHI(0),
      REM("%", 16),
      SHL("<<", 16),
      SHR(">>", 16),
      STATIC_FIELD(8),
      SUB("-", 16),
      THIS_REF(16),
      USHR(">>>", 16),
      XOR("^", 16);

      private String name;
      private int flags;

      private VT(int flags) {
         this((String)null, flags);
      }

      private VT(String name, int flags) {
         this.name = name;
         this.flags = flags;
      }

      public String toString() {
         return this.name == null ? super.toString() : this.name;
      }

      public boolean canThrow() {
         return 8 == this.flags;
      }

      public boolean mayThrow() {
         return 16 == this.flags;
      }
   }

   public abstract static class EnExpr extends Value {
      public Value[] ops;

      public void setOps(Value[] ops) {
         this.ops = ops;
      }

      public EnExpr(Value.VT vt, Value[] ops) {
         super(vt, ET.En);
         this.ops = ops;
      }

      protected Value[] cloneOps() {
         Value[] nOps = new Value[this.ops.length];

         for(int i = 0; i < nOps.length; ++i) {
            nOps[i] = this.ops[i].trim().clone();
         }

         return nOps;
      }

      protected Value[] cloneOps(LabelAndLocalMapper mapper) {
         Value[] nOps = new Value[this.ops.length];

         for(int i = 0; i < nOps.length; ++i) {
            nOps[i] = this.ops[i].clone(mapper);
         }

         return nOps;
      }

      public Value[] getOps() {
         return this.ops;
      }

      protected void releaseMemory() {
         this.ops = null;
      }
   }

   public abstract static class E2Expr extends Value {
      public Value op1;
      public Value op2;

      public void setOp1(Value op1) {
         this.op1 = op1;
      }

      public void setOp2(Value op2) {
         this.op2 = op2;
      }

      public E2Expr(Value.VT vt, Value op1, Value op2) {
         super(vt, ET.E2);
         this.op1 = op1;
         this.op2 = op2;
      }

      public Value getOp1() {
         return this.op1;
      }

      public Value getOp2() {
         return this.op2;
      }

      protected void releaseMemory() {
         this.op1 = this.op2 = null;
      }
   }

   public abstract static class E1Expr extends Value {
      public Value op;

      public void setOp(Value op) {
         this.op = op;
      }

      public E1Expr(Value.VT vt, Value op) {
         super(vt, ET.E1);
         this.op = op;
      }

      public Value getOp() {
         return this.op;
      }

      protected void releaseMemory() {
         this.op = null;
      }
   }

   public abstract static class E0Expr extends Value {
      public E0Expr(Value.VT vt) {
         super(vt, ET.E0);
      }
   }
}
