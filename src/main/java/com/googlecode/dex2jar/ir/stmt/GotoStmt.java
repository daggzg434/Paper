package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class GotoStmt extends Stmt.E0Stmt implements JumpStmt {
   public LabelStmt target;

   public LabelStmt getTarget() {
      return this.target;
   }

   public void setTarget(LabelStmt target) {
      this.target = target;
   }

   public GotoStmt(LabelStmt target) {
      super(Stmt.ST.GOTO);
      this.target = target;
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      LabelStmt nTarget = mapper.map(this.target);
      return new GotoStmt(nTarget);
   }

   public String toString() {
      return "GOTO " + this.target.getDisplayName();
   }
}
