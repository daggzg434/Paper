package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

public class AssignStmt extends Stmt.E2Stmt {
   public AssignStmt(Stmt.ST type, Value left, Value right) {
      super(type, left, right);
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      return new AssignStmt(this.st, this.op1.clone(mapper), this.op2.clone(mapper));
   }

   public String toString() {
      switch(this.st) {
      case ASSIGN:
         return this.op1 + " = " + this.op2;
      case LOCAL_START:
      case IDENTITY:
         return this.op1 + " := " + this.op2;
      case FILL_ARRAY_DATA:
         return this.op1 + " <- " + this.op2;
      default:
         return super.toString();
      }
   }
}
