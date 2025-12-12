package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import java.util.Iterator;

public class StmtTraveler {
   public void travel(IrMethod method) {
      this.travel(method.stmts);
   }

   public void travel(StmtList stmts) {
      Iterator it = stmts.iterator();

      while(it.hasNext()) {
         Stmt stmt = (Stmt)it.next();
         Stmt n = this.travel(stmt);
         if (n != stmt) {
            stmts.insertBefore(stmt, n);
            it.remove();
         }
      }

   }

   public Stmt travel(Stmt stmt) {
      switch(stmt.et) {
      case E0:
      default:
         break;
      case E1:
         stmt.setOp(this.travel(stmt.getOp()));
         break;
      case E2:
         stmt.setOp1(this.travel(stmt.getOp1()));
         stmt.setOp2(this.travel(stmt.getOp2()));
         break;
      case En:
         Value[] ops = stmt.getOps();

         for(int i = 0; i < ops.length; ++i) {
            ops[i] = this.travel(ops[i]);
         }
      }

      return stmt;
   }

   public Value travel(Value op) {
      switch(op.et) {
      case E0:
      default:
         break;
      case E1:
         op.setOp(this.travel(op.getOp()));
         break;
      case E2:
         op.setOp1(this.travel(op.getOp1()));
         op.setOp2(this.travel(op.getOp2()));
         break;
      case En:
         Value[] ops = op.getOps();

         for(int i = 0; i < ops.length; ++i) {
            ops[i] = this.travel(ops[i]);
         }
      }

      return op;
   }
}
