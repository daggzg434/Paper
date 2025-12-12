package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExceptionHandlerTrim implements Transformer {
   public void transform(IrMethod irMethod) {
      List<Trap> trips = irMethod.traps;
      irMethod.traps = new ArrayList();
      LabelAndLocalMapper map = new LabelAndLocalMapper() {
         public LabelStmt map(LabelStmt label) {
            return label;
         }
      };
      Iterator var4 = trips.iterator();

      while(var4.hasNext()) {
         Trap trap = (Trap)var4.next();
         Trap ntrap = trap.clone(map);
         int status = false;

         for(Stmt p = trap.start.getNext(); p != trap.end; p = p.getNext()) {
            Object pre;
            if (!Cfg.notThrow(p)) {
               if (status) {
                  if (status) {
                  }
               } else {
                  pre = p.getPre();
                  if (pre == null || ((Stmt)pre).st != Stmt.ST.LABEL) {
                     pre = Stmts.nLabel();
                     irMethod.stmts.insertBefore(p, (Stmt)pre);
                  }

                  ntrap.start = (LabelStmt)pre;
                  status = true;
               }
            } else if (status) {
               pre = p.getPre();
               if (pre == null || ((Stmt)pre).st != Stmt.ST.LABEL) {
                  pre = Stmts.nLabel();
                  irMethod.stmts.insertBefore(p, (Stmt)pre);
               }

               ntrap.end = (LabelStmt)pre;
               irMethod.traps.add(ntrap);
               status = false;
               ntrap = trap.clone(map);
            }
         }

         if (status) {
            ntrap.end = trap.end;
            irMethod.traps.add(ntrap);
            status = false;
         }
      }

   }
}
