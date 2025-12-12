package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import java.util.Iterator;

public class StmtSearcher {
   public void travel(StmtList stmts) {
      Iterator var2 = stmts.iterator();

      while(var2.hasNext()) {
         Stmt stmt = (Stmt)var2.next();
         this.travel(stmt);
      }

   }

   public void travel(Stmt stmt) {
      switch(stmt.et) {
      case E0:
      default:
         break;
      case E1:
         this.travel(stmt.getOp());
         break;
      case E2:
         this.travel(stmt.getOp1());
         this.travel(stmt.getOp2());
         break;
      case En:
         Value[] ops = stmt.getOps();
         Value[] var3 = ops;
         int var4 = ops.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Value op = var3[var5];
            this.travel(op);
         }
      }

   }

   public void travel(Value op) {
      switch(op.et) {
      case E0:
      default:
         break;
      case E1:
         this.travel(op.getOp());
         break;
      case E2:
         this.travel(op.getOp1());
         this.travel(op.getOp2());
         break;
      case En:
         Value[] ops = op.getOps();
         Value[] var3 = ops;
         int var4 = ops.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Value op1 = var3[var5];
            this.travel(op1);
         }
      }

   }
}
