package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.expr.Value;

public final class Stmts {
   public static AssignStmt nAssign(Value left, Value right) {
      return new AssignStmt(Stmt.ST.ASSIGN, left, right);
   }

   public static AssignStmt nFillArrayData(Value left, Value arrayData) {
      return new AssignStmt(Stmt.ST.FILL_ARRAY_DATA, left, arrayData);
   }

   public static GotoStmt nGoto(LabelStmt target) {
      return new GotoStmt(target);
   }

   public static AssignStmt nIdentity(Value local, Value identityRef) {
      return new AssignStmt(Stmt.ST.IDENTITY, local, identityRef);
   }

   public static IfStmt nIf(Value a, LabelStmt target) {
      return new IfStmt(Stmt.ST.IF, a, target);
   }

   public static LabelStmt nLabel() {
      return new LabelStmt();
   }

   public static UnopStmt nLock(Value op) {
      return new UnopStmt(Stmt.ST.LOCK, op);
   }

   public static LookupSwitchStmt nLookupSwitch(Value key, int[] lookupValues, LabelStmt[] targets, LabelStmt target) {
      return new LookupSwitchStmt(key, lookupValues, targets, target);
   }

   public static NopStmt nNop() {
      return new NopStmt();
   }

   public static UnopStmt nReturn(Value op) {
      return new UnopStmt(Stmt.ST.RETURN, op);
   }

   public static ReturnVoidStmt nReturnVoid() {
      return new ReturnVoidStmt();
   }

   public static TableSwitchStmt nTableSwitch(Value key, int lowIndex, LabelStmt[] targets, LabelStmt target) {
      return new TableSwitchStmt(key, lowIndex, targets, target);
   }

   public static UnopStmt nThrow(Value op) {
      return new UnopStmt(Stmt.ST.THROW, op);
   }

   public static UnopStmt nUnLock(Value op) {
      return new UnopStmt(Stmt.ST.UNLOCK, op);
   }

   public static VoidInvokeStmt nVoidInvoke(Value op) {
      return new VoidInvokeStmt(op);
   }

   private Stmts() {
   }
}
