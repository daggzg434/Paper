package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class NopStmt extends Stmt.E0Stmt {
   public NopStmt() {
      super(Stmt.ST.NOP);
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      return new NopStmt();
   }

   public String toString() {
      return "NOP";
   }
}
