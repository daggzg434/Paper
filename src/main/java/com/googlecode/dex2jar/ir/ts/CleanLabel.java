package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LocalVar;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.stmt.BaseSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CleanLabel implements Transformer {
   public void transform(IrMethod irMethod) {
      Set<LabelStmt> uselabels = new HashSet();
      this.addTrap(irMethod.traps, uselabels);
      this.addVars(irMethod.vars, uselabels);
      this.addStmt(irMethod.stmts, uselabels);
      this.rmUnused(irMethod.stmts, uselabels);
   }

   private void addVars(List<LocalVar> vars, Set<LabelStmt> uselabels) {
      if (vars != null) {
         Iterator var3 = vars.iterator();

         while(var3.hasNext()) {
            LocalVar var = (LocalVar)var3.next();
            uselabels.add(var.start);
            uselabels.add(var.end);
         }
      }

   }

   private void rmUnused(StmtList stmts, Set<LabelStmt> uselabels) {
      Stmt p = stmts.getFirst();

      while(true) {
         while(p != null) {
            if (p.st == Stmt.ST.LABEL && !uselabels.contains(p)) {
               Stmt q = p.getNext();
               stmts.remove(p);
               p = q;
            } else {
               p = p.getNext();
            }
         }

         return;
      }
   }

   private void addStmt(StmtList stmts, Set<LabelStmt> labels) {
      for(Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
         if (p instanceof JumpStmt) {
            labels.add(((JumpStmt)p).getTarget());
         } else if (p instanceof BaseSwitchStmt) {
            BaseSwitchStmt stmt = (BaseSwitchStmt)p;
            labels.add(stmt.defaultTarget);
            LabelStmt[] var5 = stmt.targets;
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               LabelStmt t = var5[var7];
               labels.add(t);
            }
         }
      }

   }

   private void addTrap(List<Trap> traps, Set<LabelStmt> labels) {
      if (traps != null) {
         Iterator var3 = traps.iterator();

         while(var3.hasNext()) {
            Trap trap = (Trap)var3.next();
            labels.add(trap.start);
            labels.add(trap.end);
            LabelStmt[] var5 = trap.handlers;
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               LabelStmt h = var5[var7];
               labels.add(h);
            }
         }
      }

   }
}
