package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

public class TableSwitchStmt extends BaseSwitchStmt {
   public int lowIndex;

   public TableSwitchStmt() {
      super(Stmt.ST.TABLE_SWITCH, (Value)null);
   }

   public TableSwitchStmt(Value key, int lowIndex, LabelStmt[] targets, LabelStmt defaultTarget) {
      super(Stmt.ST.TABLE_SWITCH, key);
      this.lowIndex = lowIndex;
      this.targets = targets;
      this.defaultTarget = defaultTarget;
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      LabelStmt[] nTargets = new LabelStmt[this.targets.length];

      for(int i = 0; i < nTargets.length; ++i) {
         nTargets[i] = mapper.map(this.targets[i]);
      }

      return new TableSwitchStmt(this.op.clone(mapper), this.lowIndex, nTargets, mapper.map(this.defaultTarget));
   }

   public String toString() {
      StringBuilder sb = (new StringBuilder("switch(")).append(this.op).append(") {");

      for(int i = 0; i < this.targets.length; ++i) {
         sb.append("\n case ").append(this.lowIndex + i).append(": GOTO ").append(this.targets[i].getDisplayName()).append(";");
      }

      sb.append("\n default : GOTO ").append(this.defaultTarget.getDisplayName()).append(";");
      sb.append("\n}");
      return sb.toString();
   }
}
