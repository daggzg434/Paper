package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.expr.Value;

public class UnopStmt extends Stmt.E1Stmt {
   public UnopStmt(Stmt.ST type, Value op) {
      super(type, op);
   }

   public Stmt clone(LabelAndLocalMapper mapper) {
      return new UnopStmt(this.st, this.op.clone(mapper));
   }

   public String toString() {
      switch(super.st) {
      case LOCK:
         return "lock " + this.op;
      case UNLOCK:
         return "unlock " + this.op;
      case THROW:
         return "throw " + this.op;
      case RETURN:
         return "return " + this.op;
      case LOCAL_END:
         return this.op + " ::END";
      default:
         return super.toString();
      }
   }
}
