package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;
import java.util.Set;

public abstract class Stmt {
   public static final int CAN_CONTINUE = 1;
   public static final int CAN_BRNANCH = 2;
   public static final int CAN_SWITCH = 4;
   public static final int CAN_THROW = 8;
   public static final int MAY_THROW = 16;
   public Set<Stmt> _cfg_froms;
   public Set<LabelStmt> exceptionHandlers;
   public boolean visited;
   public Object frame;
   public Stmt _ts_default_next;
   public final ET et;
   public int id;
   StmtList list;
   Stmt next;
   Stmt pre;
   public final Stmt.ST st;

   protected Stmt(Stmt.ST st, ET et) {
      this.st = st;
      this.et = et;
   }

   public abstract Stmt clone(LabelAndLocalMapper mapper);

   public final Stmt getNext() {
      return this.next;
   }

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

   public final Stmt getPre() {
      return this.pre;
   }

   public void setOp(Value op) {
   }

   public void setOp1(Value op) {
   }

   public void setOp2(Value op) {
   }

   public void setOps(Value[] op) {
   }

   public static enum ST {
      LOCAL_START(1),
      LOCAL_END(1),
      ASSIGN(17),
      IDENTITY(1),
      LABEL(1),
      LOCK(9),
      NOP(1),
      UNLOCK(9),
      VOID_INVOKE(9),
      FILL_ARRAY_DATA(9),
      RETURN(16),
      RETURN_VOID(0),
      THROW(8),
      GOTO(2),
      IF(19),
      LOOKUP_SWITCH(20),
      TABLE_SWITCH(20);

      private int config;

      private ST(int config) {
         this.config = config;
      }

      public boolean canBranch() {
         return 0 != (2 & this.config);
      }

      public boolean canContinue() {
         return 0 != (1 & this.config);
      }

      public boolean canSwitch() {
         return 0 != (4 & this.config);
      }

      public boolean mayThrow() {
         return 0 != (16 & this.config);
      }

      public boolean canThrow() {
         return 0 != (8 & this.config);
      }
   }

   public abstract static class E2Stmt extends Stmt {
      public Value op1;
      public Value op2;

      public E2Stmt(Stmt.ST type, Value op1, Value op2) {
         super(type, ET.E2);
         this.op1 = op1;
         this.op2 = op2;
      }

      public Value getOp1() {
         return this.op1;
      }

      public Value getOp2() {
         return this.op2;
      }

      public void setOp1(Value op1) {
         this.op1 = op1;
      }

      public void setOp2(Value op2) {
         this.op2 = op2;
      }
   }

   public abstract static class E1Stmt extends Stmt {
      public Value op;

      public E1Stmt(Stmt.ST type, Value op) {
         super(type, ET.E1);
         this.op = op;
      }

      public Value getOp() {
         return this.op;
      }

      public void setOp(Value op) {
         this.op = op;
      }
   }

   public abstract static class E0Stmt extends Stmt {
      public E0Stmt(Stmt.ST type) {
         super(type, ET.E0);
      }
   }
}
