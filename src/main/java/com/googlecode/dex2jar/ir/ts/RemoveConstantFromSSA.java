package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemoveConstantFromSSA extends StatedTransformer {
   public static final Comparator<Local> LOCAL_COMPARATOR = new Comparator<Local>() {
      public int compare(Local local, Local t1) {
         return Integer.compare(local._ls_index, t1._ls_index);
      }
   };

   public boolean transformReportChanged(IrMethod method) {
      boolean changed = false;
      List<AssignStmt> assignStmtList = new ArrayList();
      Map<Local, Object> cstMap = new HashMap();

      for(Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
         if (p.st == Stmt.ST.ASSIGN) {
            AssignStmt as = (AssignStmt)p;
            if (as.getOp1().vt == Value.VT.LOCAL) {
               if (as.getOp2().vt == Value.VT.CONSTANT) {
                  assignStmtList.add(as);
                  cstMap.put((Local)as.getOp1(), ((Constant)as.getOp2()).value);
               } else if (as.getOp2().vt == Value.VT.LOCAL) {
                  cstMap.put((Local)as.getOp1(), as.getOp2());
               }
            }
         }
      }

      if (assignStmtList.size() == 0) {
         return false;
      } else {
         RemoveLocalFromSSA.fixReplace(cstMap);
         final Map<Local, Value> toReplace = new HashMap();
         Set<Value> usedInPhi = new HashSet();
         List<LabelStmt> phiLabels = method.phiLabels;
         if (phiLabels != null) {
            boolean loopAgain = true;

            label94:
            while(loopAgain) {
               loopAgain = false;
               usedInPhi.clear();
               Iterator it = phiLabels.iterator();

               while(true) {
                  LabelStmt labelStmt;
                  do {
                     if (!it.hasNext()) {
                        continue label94;
                     }

                     labelStmt = (LabelStmt)it.next();
                  } while(labelStmt.phis == null);

                  Iterator it2 = labelStmt.phis.iterator();

                  while(it2.hasNext()) {
                     AssignStmt phi = (AssignStmt)it2.next();
                     Value[] vs = phi.getOp2().getOps();
                     Object sameCst = null;
                     boolean allEqual = true;
                     Value[] var16 = vs;
                     int var17 = vs.length;

                     for(int var18 = 0; var18 < var17; ++var18) {
                        Value p = var16[var18];
                        Object cst = cstMap.get(p);
                        if (cst == null) {
                           allEqual = false;
                           break;
                        }

                        if (sameCst == null) {
                           sameCst = cst;
                        } else if (!sameCst.equals(cst)) {
                           allEqual = false;
                           break;
                        }
                     }

                     if (allEqual) {
                        cstMap.put((Local)phi.getOp1(), sameCst);
                        if (sameCst instanceof Local) {
                           phi.setOp2((Value)sameCst);
                        } else {
                           phi.setOp2(Exprs.nConstant(sameCst));
                           assignStmtList.add(phi);
                        }

                        it2.remove();
                        method.stmts.insertAfter(labelStmt, phi);
                        changed = true;
                        loopAgain = true;
                     } else {
                        usedInPhi.addAll(Arrays.asList(phi.getOp2().getOps()));
                     }
                  }

                  if (labelStmt.phis.size() == 0) {
                     it.remove();
                  }
               }
            }
         }

         AssignStmt as;
         for(Iterator it = assignStmtList.iterator(); it.hasNext(); toReplace.put((Local)as.getOp1(), as.getOp2())) {
            as = (AssignStmt)it.next();
            if (!usedInPhi.contains(as.getOp1())) {
               it.remove();
               method.stmts.remove(as);
               method.locals.remove(as.getOp1());
               changed = true;
            }
         }

         Cfg.travelMod(method.stmts, new Cfg.TravelCallBack() {
            public Value onAssign(Local v, AssignStmt as) {
               return v;
            }

            public Value onUse(Local v) {
               Value n = (Value)toReplace.get(v);
               return (Value)(n == null ? v : n.clone());
            }
         }, false);
         return changed;
      }
   }
}
