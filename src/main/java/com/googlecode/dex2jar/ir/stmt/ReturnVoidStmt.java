package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class ReturnVoidStmt extends Stmt.E0Stmt {
   public ReturnVoidStmt() {
      super(Stmt.ST.RETURN_VOID);
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      return new ReturnVoidStmt();
   }

   public String toString() {
      return "return";
   }
}
