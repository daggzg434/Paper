package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.PhiExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DeadCodeTransformer implements Transformer {
   public void transform(IrMethod method) {
      Cfg.createCFG(method);
      Cfg.dfsVisit(method, (Cfg.DfsVisitor)null);
      if (method.traps != null) {
         Iterator it = method.traps.iterator();

         label171:
         while(true) {
            while(true) {
               if (!it.hasNext()) {
                  break label171;
               }

               Trap t = (Trap)it.next();
               boolean allNotThrow = true;

               for(Object p = t.start; p != t.end; p = ((Stmt)p).getNext()) {
                  if (((Stmt)p).visited && Cfg.isThrow((Stmt)p)) {
                     allNotThrow = false;
                     break;
                  }
               }

               if (allNotThrow) {
                  it.remove();
               } else {
                  boolean allNotVisited = true;
                  boolean allVisited = true;
                  LabelStmt[] var7 = t.handlers;
                  int var8 = var7.length;

                  int i;
                  for(i = 0; i < var8; ++i) {
                     LabelStmt labelStmt = var7[i];
                     if (labelStmt.visited) {
                        allNotVisited = false;
                     } else {
                        allVisited = false;
                     }
                  }

                  if (allNotVisited) {
                     it.remove();
                  } else {
                     t.start.visited = true;
                     t.end.visited = true;
                     if (!allVisited) {
                        List<String> types = new ArrayList(t.handlers.length);
                        List<LabelStmt> labelStmts = new ArrayList(t.handlers.length);

                        for(i = 0; i < t.handlers.length; ++i) {
                           labelStmts.add(t.handlers[i]);
                           types.add(t.types[i]);
                        }

                        t.handlers = (LabelStmt[])labelStmts.toArray(new LabelStmt[labelStmts.size()]);
                        t.types = (String[])types.toArray(new String[types.size()]);
                     }
                  }
               }
            }
         }
      }

      Set<Local> definedLocals = new HashSet();
      Iterator it = method.stmts.iterator();

      while(true) {
         while(it.hasNext()) {
            Stmt p = (Stmt)it.next();
            if (!p.visited) {
               it.remove();
            } else if ((p.st == Stmt.ST.ASSIGN || p.st == Stmt.ST.IDENTITY) && p.getOp1().vt == Value.VT.LOCAL) {
               definedLocals.add((Local)p.getOp1());
            }
         }

         if (method.phiLabels != null) {
            it = method.phiLabels.iterator();

            label122:
            while(true) {
               while(true) {
                  if (!it.hasNext()) {
                     break label122;
                  }

                  LabelStmt labelStmt = (LabelStmt)it.next();
                  if (!labelStmt.visited) {
                     it.remove();
                  } else if (labelStmt.phis != null) {
                     Iterator var21 = labelStmt.phis.iterator();

                     while(var21.hasNext()) {
                        AssignStmt phi = (AssignStmt)var21.next();
                        definedLocals.add((Local)phi.getOp1());
                     }
                  }
               }
            }
         }

         method.locals.clear();
         method.locals.addAll(definedLocals);
         Set<Value> tmp = new HashSet();
         if (method.phiLabels != null) {
            Iterator it = method.phiLabels.iterator();

            label104:
            while(true) {
               LabelStmt labelStmt;
               do {
                  if (!it.hasNext()) {
                     return;
                  }

                  labelStmt = (LabelStmt)it.next();
               } while(labelStmt.phis == null);

               Iterator var24 = labelStmt.phis.iterator();

               while(true) {
                  int var11;
                  int var12;
                  Value v;
                  PhiExpr phiExpr;
                  boolean needRebuild;
                  Value[] var30;
                  do {
                     if (!var24.hasNext()) {
                        continue label104;
                     }

                     AssignStmt phi = (AssignStmt)var24.next();
                     phiExpr = (PhiExpr)phi.getOp2();
                     needRebuild = false;
                     var30 = phiExpr.getOps();
                     var11 = var30.length;

                     for(var12 = 0; var12 < var11; ++var12) {
                        v = var30[var12];
                        if (!definedLocals.contains(v)) {
                           needRebuild = true;
                           break;
                        }
                     }
                  } while(!needRebuild);

                  var30 = phiExpr.getOps();
                  var11 = var30.length;

                  for(var12 = 0; var12 < var11; ++var12) {
                     v = var30[var12];
                     if (definedLocals.contains(v)) {
                        tmp.add(v);
                     }
                  }

                  phiExpr.setOps((Value[])tmp.toArray(new Value[tmp.size()]));
                  tmp.clear();
               }
            }
         }

         return;
      }
   }
}
