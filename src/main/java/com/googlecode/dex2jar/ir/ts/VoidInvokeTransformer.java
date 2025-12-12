package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

public class VoidInvokeTransformer extends StatedTransformer {
   public boolean transformReportChanged(IrMethod method) {
      if (method.locals.size() == 0) {
         return false;
      } else {
         int[] reads = Cfg.countLocalReads(method);
         boolean changed = false;

         for(Object p = method.stmts.getFirst(); p != null; p = ((Stmt)p).getNext()) {
            if (((Stmt)p).st == Stmt.ST.ASSIGN && ((Stmt)p).getOp1().vt == Value.VT.LOCAL) {
               Local left = (Local)((Stmt)p).getOp1();
               if (reads[left._ls_index] == 0) {
                  switch(((Stmt)p).getOp2().vt) {
                  case INVOKE_INTERFACE:
                  case INVOKE_NEW:
                  case INVOKE_SPECIAL:
                  case INVOKE_STATIC:
                  case INVOKE_VIRTUAL:
                     method.locals.remove(left);
                     Stmt nVoidInvoke = Stmts.nVoidInvoke(((Stmt)p).getOp2());
                     method.stmts.replace((Stmt)p, nVoidInvoke);
                     p = nVoidInvoke;
                     changed = true;
                  }
               }
            }
         }

         return changed;
      }
   }

   public void transform(IrMethod method) {
      this.transformReportChanged(method);
   }
}
