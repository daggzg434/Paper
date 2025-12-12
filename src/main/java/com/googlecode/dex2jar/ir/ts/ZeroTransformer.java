package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ZeroTransformer extends StatedTransformer {
   public boolean transformReportChanged(IrMethod method) {
      boolean changed = false;
      List<AssignStmt> assignStmtList = new ArrayList();

      for(Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
         if (p.st == Stmt.ST.ASSIGN) {
            AssignStmt as = (AssignStmt)p;
            if (as.getOp1().vt == Value.VT.LOCAL && as.getOp2().vt == Value.VT.CONSTANT) {
               Constant cst = (Constant)as.getOp2();
               Object value = cst.value;
               if (value instanceof Number && !(value instanceof Long) && !(value instanceof Double)) {
                  int v = ((Number)value).intValue();
                  if (v == 0 || v == 1) {
                     assignStmtList.add(as);
                  }
               }
            }
         }
      }

      if (assignStmtList.size() == 0) {
         return false;
      } else {
         List<LabelStmt> phiLabels = method.phiLabels;
         if (phiLabels != null) {
            Iterator var18 = assignStmtList.iterator();

            while(var18.hasNext()) {
               AssignStmt as = (AssignStmt)var18.next();
               Local local = (Local)as.getOp1();
               boolean first = true;
               Iterator var9 = phiLabels.iterator();

               while(var9.hasNext()) {
                  LabelStmt labelStmt = (LabelStmt)var9.next();
                  Iterator var11 = labelStmt.phis.iterator();

                  while(var11.hasNext()) {
                     AssignStmt phi = (AssignStmt)var11.next();
                     Value[] vs = phi.getOp2().getOps();

                     for(int i = 0; i < vs.length; ++i) {
                        Value v = vs[i];
                        if (v == local) {
                           if (first) {
                              first = false;
                           } else {
                              Local nLocal = Exprs.nLocal(-1);
                              method.locals.add(nLocal);
                              changed = true;
                              method.stmts.insertBefore(as, Stmts.nAssign(nLocal, as.getOp2().clone()));
                              vs[i] = nLocal;
                           }
                        }
                     }
                  }
               }
            }
         }

         return changed;
      }
   }
}
