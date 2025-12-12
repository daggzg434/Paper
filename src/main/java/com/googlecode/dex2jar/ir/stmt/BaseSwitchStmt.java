package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.expr.Value;

public abstract class BaseSwitchStmt extends Stmt.E1Stmt {
   public LabelStmt[] targets;
   public LabelStmt defaultTarget;

   public BaseSwitchStmt(Stmt.ST type, Value op) {
      super(type, op);
   }
}
