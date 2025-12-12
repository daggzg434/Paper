package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.GotoStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.ArrayList;
import java.util.Iterator;

public class EndRemover implements Transformer {
   private static final LabelAndLocalMapper keepLocal = new LabelAndLocalMapper() {
      public Local map(Local local) {
         return local;
      }
   };

   public void transform(IrMethod irMethod) {
      Iterator var2 = (new ArrayList(irMethod.traps)).iterator();

      LabelStmt start;
      Stmt p;
      while(var2.hasNext()) {
         Trap trap = (Trap)var2.next();
         start = null;
         boolean removeTrap = true;
         p = trap.start.getNext();

         while(p != null && p != trap.end) {
            boolean notThrow = Cfg.notThrow(p);
            if (!notThrow) {
               start = null;
               p = p.getNext();
               removeTrap = false;
            } else {
               switch(p.st) {
               case LABEL:
                  if (start != null) {
                     this.move4Label(irMethod.stmts, start, p.getPre(), (LabelStmt)p);
                  }

                  start = (LabelStmt)p;
                  p = p.getNext();
                  break;
               case GOTO:
               case RETURN:
               case RETURN_VOID:
                  if (start != null) {
                     Stmt tmp = p.getNext();
                     this.move4End(irMethod.stmts, start, p);
                     start = null;
                     p = tmp;
                  } else {
                     p = p.getNext();
                  }
                  break;
               default:
                  p = p.getNext();
               }
            }
         }

         if (removeTrap) {
            irMethod.traps.remove(trap);
         }
      }

      StmtList stmts = irMethod.stmts;

      for(Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
         if (p.st == Stmt.ST.GOTO) {
            start = ((GotoStmt)p).target;
            Stmt next = start.getNext();
            if (next != null && (next.st == Stmt.ST.RETURN || next.st == Stmt.ST.RETURN_VOID)) {
               p = next.clone(keepLocal);
               stmts.insertAfter(p, p);
               stmts.remove(p);
               p = p;
            }
         }
      }

   }

   private void move4Label(StmtList stmts, LabelStmt start, Stmt end, LabelStmt label) {
      this.move4End(stmts, start, end);
      stmts.insertAfter(end, Stmts.nGoto(label));
   }

   private void move4End(StmtList stmts, LabelStmt start, Stmt end) {
      Stmt g1 = Stmts.nGoto(start);
      stmts.insertBefore(start, g1);

      Stmt last;
      for(last = stmts.getLast(); last.st == Stmt.ST.GOTO && ((GotoStmt)last).target == start; last = stmts.getLast()) {
         stmts.remove(last);
      }

      stmts.move(start, end, last);
   }
}
