package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;

public class LocalVar {
   public LabelStmt start;
   public LabelStmt end;
   public String name;
   public String type;
   public String signature;
   public Local reg;

   public LocalVar(String name, String type, String signature, LabelStmt start, LabelStmt end, Local reg) {
      this.name = name;
      this.start = start;
      this.end = end;
      this.type = type;
      this.signature = signature;
      this.reg = reg;
   }

   public LocalVar clone(LabelAndLocalMapper map) {
      return new LocalVar(this.name, this.type, this.signature, this.start.clone(map), this.end.clone(map), (Local)this.reg.clone());
   }

   public String toString() {
      return String.format(".var %s ~ %s %s -> %s //%s", this.start.getDisplayName(), this.end.getDisplayName(), this.reg, this.name, this.type);
   }
}
