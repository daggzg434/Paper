package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class RemoveLocalFromSSA extends StatedTransformer {
   static <T extends Value> void replaceAssign(List<AssignStmt> assignStmtList, Map<Local, T> toReplace) {
      Iterator var2 = assignStmtList.iterator();

      while(var2.hasNext()) {
         AssignStmt as = (AssignStmt)var2.next();
         Value right = as.getOp2();
         T to = (Value)toReplace.get(right);
         if (to != null) {
            as.setOp2(to);
         }
      }

   }

   private boolean simpleAssign(List<LabelStmt> phiLabels, List<AssignStmt> assignStmtList, Map<Local, Local> toReplace, StmtList stmts) {
      Set<Value> usedInPhi = new HashSet();
      if (phiLabels != null) {
         Iterator var6 = phiLabels.iterator();

         while(var6.hasNext()) {
            LabelStmt labelStmt = (LabelStmt)var6.next();
            Iterator var8 = labelStmt.phis.iterator();

            while(var8.hasNext()) {
               AssignStmt phi = (AssignStmt)var8.next();
               usedInPhi.addAll(Arrays.asList(phi.getOp2().getOps()));
            }
         }
      }

      boolean changed = false;
      Iterator it = assignStmtList.iterator();

      while(it.hasNext()) {
         AssignStmt as = (AssignStmt)it.next();
         if (!usedInPhi.contains(as.getOp1())) {
            it.remove();
            stmts.remove(as);
            toReplace.put((Local)as.getOp1(), (Local)as.getOp2());
            changed = true;
         }
      }

      return changed;
   }

   private void replacePhi(List<LabelStmt> phiLabels, Map<Local, Local> toReplace, Set<Value> set) {
      if (phiLabels != null) {
         Iterator var4 = phiLabels.iterator();

         while(var4.hasNext()) {
            LabelStmt labelStmt = (LabelStmt)var4.next();
            Iterator var6 = labelStmt.phis.iterator();

            while(var6.hasNext()) {
               AssignStmt phi = (AssignStmt)var6.next();
               Value[] ops = phi.getOp2().getOps();
               Value[] var9 = ops;
               int var10 = ops.length;

               for(int var11 = 0; var11 < var10; ++var11) {
                  Value op = var9[var11];
                  Value n = (Value)toReplace.get(op);
                  if (n != null) {
                     set.add(n);
                  } else {
                     set.add(op);
                  }
               }

               set.remove(phi.getOp1());
               phi.getOp2().setOps((Value[])set.toArray(new Value[set.size()]));
               set.clear();
            }
         }
      }

   }

   public static RemoveLocalFromSSA.PhiObject getOrCreate(Map<Local, RemoveLocalFromSSA.PhiObject> map, Local local) {
      RemoveLocalFromSSA.PhiObject po = (RemoveLocalFromSSA.PhiObject)map.get(local);
      if (po == null) {
         po = new RemoveLocalFromSSA.PhiObject();
         po.local = local;
         map.put(local, po);
      }

      return po;
   }

   public static void linkPhiObject(RemoveLocalFromSSA.PhiObject parent, RemoveLocalFromSSA.PhiObject child) {
      parent.children.add(child);
      child.parent.add(parent);
   }

   private boolean simplePhi(List<LabelStmt> phiLabels, Map<Local, Local> toReplace, Set<Value> set) {
      boolean changed = false;
      if (phiLabels != null) {
         Iterator itLabel = phiLabels.iterator();

         while(itLabel.hasNext()) {
            LabelStmt labelStmt = (LabelStmt)itLabel.next();

            for(Iterator it = labelStmt.phis.iterator(); it.hasNext(); set.clear()) {
               AssignStmt phi = (AssignStmt)it.next();
               set.addAll(Arrays.asList(phi.getOp2().getOps()));
               set.remove(phi.getOp1());
               if (set.size() == 1) {
                  it.remove();
                  changed = true;
                  toReplace.put((Local)phi.getOp1(), (Local)set.iterator().next());
               }
            }

            if (labelStmt.phis.size() == 0) {
               labelStmt.phis = null;
               itLabel.remove();
            }
         }
      }

      return changed;
   }

   private boolean removeLoopFromPhi(List<LabelStmt> phiLabels, Map<Local, Local> toReplace) {
      boolean changed = false;
      if (phiLabels != null) {
         Set<Local> toDeletePhiAssign = new HashSet();
         Map<Local, RemoveLocalFromSSA.PhiObject> phis = this.collectPhiObjects(phiLabels);
         Queue<RemoveLocalFromSSA.PhiObject> q = new UniqueQueue();
         q.addAll(phis.values());

         while(!q.isEmpty()) {
            RemoveLocalFromSSA.PhiObject po = (RemoveLocalFromSSA.PhiObject)q.poll();
            Iterator var8 = po.children.iterator();

            while(var8.hasNext()) {
               RemoveLocalFromSSA.PhiObject child = (RemoveLocalFromSSA.PhiObject)var8.next();
               if (child.isInitByPhi && child.parent.addAll(po.parent)) {
                  q.add(child);
               }
            }
         }

         Iterator itLabel = phis.values().iterator();

         label70:
         while(true) {
            RemoveLocalFromSSA.PhiObject po;
            do {
               if (!itLabel.hasNext()) {
                  itLabel = phiLabels.iterator();

                  while(itLabel.hasNext()) {
                     LabelStmt labelStmt = (LabelStmt)itLabel.next();
                     Iterator it = labelStmt.phis.iterator();

                     while(it.hasNext()) {
                        AssignStmt phi = (AssignStmt)it.next();
                        if (toDeletePhiAssign.contains(phi.getOp1())) {
                           it.remove();
                        }
                     }

                     if (labelStmt.phis.size() == 0) {
                        labelStmt.phis = null;
                        itLabel.remove();
                     }
                  }
                  break label70;
               }

               po = (RemoveLocalFromSSA.PhiObject)itLabel.next();
            } while(!po.isInitByPhi);

            Local local = null;
            Iterator var10 = po.parent.iterator();

            while(var10.hasNext()) {
               RemoveLocalFromSSA.PhiObject p = (RemoveLocalFromSSA.PhiObject)var10.next();
               if (!p.isInitByPhi) {
                  if (local != null) {
                     local = null;
                     break;
                  }

                  local = p.local;
               }
            }

            if (local != null) {
               toReplace.put(po.local, local);
               toDeletePhiAssign.add(po.local);
               changed = true;
            }
         }
      }

      return changed;
   }

   private Map<Local, RemoveLocalFromSSA.PhiObject> collectPhiObjects(List<LabelStmt> phiLabels) {
      Map<Local, RemoveLocalFromSSA.PhiObject> phis = new HashMap();
      Iterator var3 = phiLabels.iterator();

      while(var3.hasNext()) {
         LabelStmt labelStmt = (LabelStmt)var3.next();
         Iterator var5 = labelStmt.phis.iterator();

         while(var5.hasNext()) {
            AssignStmt as = (AssignStmt)var5.next();
            Local local = (Local)as.getOp1();
            RemoveLocalFromSSA.PhiObject child = getOrCreate(phis, local);
            child.isInitByPhi = true;
            Value[] var9 = as.getOp2().getOps();
            int var10 = var9.length;

            for(int var11 = 0; var11 < var10; ++var11) {
               Value op = var9[var11];
               if (op != local) {
                  RemoveLocalFromSSA.PhiObject parent = getOrCreate(phis, (Local)op);
                  linkPhiObject(parent, child);
               }
            }
         }
      }

      return phis;
   }

   static <T> void fixReplace(Map<Local, T> toReplace) {
      List<Entry<Local, T>> set = new ArrayList(toReplace.entrySet());
      Collections.sort(set, new Comparator<Entry<Local, T>>() {
         public int compare(Entry<Local, T> localTEntry, Entry<Local, T> t1) {
            return Integer.compare(((Local)localTEntry.getKey())._ls_index, ((Local)t1.getKey())._ls_index);
         }
      });
      boolean changed = true;

      while(changed) {
         changed = false;
         Iterator var3 = set.iterator();

         while(var3.hasNext()) {
            Entry<Local, T> e = (Entry)var3.next();
            T b = e.getValue();
            if (b instanceof Local) {
               T n = toReplace.get(b);
               if (n != null && b != n) {
                  changed = true;
                  e.setValue(n);
               }
            }
         }
      }

   }

   public boolean transformReportChanged(IrMethod method) {
      boolean irChanged = false;
      List<AssignStmt> assignStmtList = new ArrayList();
      List<LabelStmt> phiLabels = method.phiLabels;

      for(Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
         if (p.st == Stmt.ST.ASSIGN) {
            AssignStmt as = (AssignStmt)p;
            if (as.getOp1().vt == Value.VT.LOCAL && as.getOp2().vt == Value.VT.LOCAL) {
               assignStmtList.add(as);
            }
         }
      }

      final Map<Local, Local> toReplace = new HashMap();
      Set<Value> set = new HashSet();
      boolean changed = true;

      while(changed) {
         changed = false;
         if (this.removeLoopFromPhi(phiLabels, toReplace)) {
            fixReplace(toReplace);
            this.replacePhi(phiLabels, toReplace, set);
         }

         while(this.simplePhi(phiLabels, toReplace, set)) {
            fixReplace(toReplace);
            this.replacePhi(phiLabels, toReplace, set);
         }

         while(this.simpleAssign(phiLabels, assignStmtList, toReplace, method.stmts)) {
            fixReplace(toReplace);
            replaceAssign(assignStmtList, toReplace);
            changed = true;
            irChanged = true;
         }

         this.replacePhi(phiLabels, toReplace, set);
      }

      for(Iterator var8 = toReplace.keySet().iterator(); var8.hasNext(); irChanged = true) {
         Local local = (Local)var8.next();
         method.locals.remove(local);
      }

      if (toReplace.size() > 0) {
         Cfg.travelMod(method.stmts, new Cfg.TravelCallBack() {
            public Value onAssign(Local v, AssignStmt as) {
               return v;
            }

            public Value onUse(Local v) {
               Local n = (Local)toReplace.get(v);
               return n == null ? v : n;
            }
         }, true);
      }

      return irChanged;
   }

   static class PhiObject {
      Set<RemoveLocalFromSSA.PhiObject> parent = new HashSet();
      Set<RemoveLocalFromSSA.PhiObject> children = new HashSet();
      Local local;
      boolean isInitByPhi = false;
   }
}
