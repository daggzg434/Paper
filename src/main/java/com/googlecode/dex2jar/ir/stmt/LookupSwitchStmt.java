package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

public class LookupSwitchStmt extends BaseSwitchStmt {
   public int[] lookupValues;

   public LookupSwitchStmt(Value key, int[] lookupValues, LabelStmt[] targets, LabelStmt defaultTarget) {
      super(Stmt.ST.LOOKUP_SWITCH, key);
      this.lookupValues = lookupValues;
      this.targets = targets;
      this.defaultTarget = defaultTarget;
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      LabelStmt[] nTargets = new LabelStmt[this.targets.length];

      for(int i = 0; i < nTargets.length; ++i) {
         nTargets[i] = mapper.map(this.targets[i]);
      }

      int[] nLookupValues = new int[this.lookupValues.length];
      System.arraycopy(this.lookupValues, 0, nLookupValues, 0, nLookupValues.length);
      return new LookupSwitchStmt(this.op.clone(mapper), nLookupValues, nTargets, mapper.map(this.defaultTarget));
   }

   public String toString() {
      StringBuilder sb = (new StringBuilder("switch(")).append(this.op).append(") {");

      for(int i = 0; i < this.lookupValues.length; ++i) {
         sb.append("\n case ").append(this.lookupValues[i]).append(": GOTO ").append(this.targets[i].getDisplayName()).append(";");
      }

      sb.append("\n default : GOTO ").append(this.defaultTarget.getDisplayName()).append(";");
      sb.append("\n}");
      return sb.toString();
   }
}
