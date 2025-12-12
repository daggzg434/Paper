package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

public class IfStmt extends Stmt.E1Stmt implements JumpStmt {
   public LabelStmt target;

   public LabelStmt getTarget() {
      return this.target;
   }

   public void setTarget(LabelStmt target) {
      this.target = target;
   }

   public IfStmt(Stmt.ST type, Value condition, LabelStmt target) {
      super(type, condition);
      this.target = target;
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      LabelStmt nTarget = mapper.map(this.target);
      return new IfStmt(this.st, this.op.clone(mapper), nTarget);
   }

   public String toString() {
      return "if " + this.op + " GOTO " + this.target.getDisplayName();
   }
}
