package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.BaseSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.an.AnalyzeValue;
import com.googlecode.dex2jar.ir.ts.an.BaseAnalyze;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SSATransformer implements Transformer {
   private void cleanTagsAndReIndex(IrMethod method) {
      int i = 0;

      Local local;
      for(Iterator var3 = method.locals.iterator(); var3.hasNext(); local._ls_index = i++) {
         local = (Local)var3.next();
         local.tag = null;
      }

   }

   private void deleteDeadCode(IrMethod method) {
      Iterator it = method.stmts.iterator();

      while(it.hasNext()) {
         Stmt stmt = (Stmt)it.next();
         if (!stmt.visited && stmt.st != Stmt.ST.LABEL) {
            it.remove();
         }
      }

   }

   private void replaceLocalsWithSSA(final IrMethod method) {
      final List<Local> locals = method.locals;
      locals.clear();
      StmtList stmts = method.stmts;
      Cfg.TravelCallBack tcb = new Cfg.TravelCallBack() {
         public Value onAssign(Local a, AssignStmt as) {
            if (a._ls_index < 0) {
               locals.add(a);
               return a;
            } else {
               SSATransformer.SSAValue lsv = (SSATransformer.SSAValue)a.tag;
               Local b = lsv.local;
               locals.add(b);
               return b;
            }
         }

         public Value onUse(Local a) {
            if (a._ls_index < 0) {
               return a;
            } else {
               SSATransformer.SSAValue lsv = (SSATransformer.SSAValue)a.tag;
               Local b = lsv.local;
               return b;
            }
         }
      };
      Set<Value> froms = new HashSet();
      List<LabelStmt> phiLabels = new ArrayList();

      for(Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
         if (p.st != Stmt.ST.LABEL) {
            Cfg.travelMod(p, tcb, true);
         } else {
            LabelStmt labelStmt = (LabelStmt)p;
            List<AssignStmt> phis = null;
            SSATransformer.SSAValue[] frame = (SSATransformer.SSAValue[])((SSATransformer.SSAValue[])p.frame);
            if (frame != null) {
               SSATransformer.SSAValue[] var11 = frame;
               int var12 = frame.length;

               for(int var13 = 0; var13 < var12; ++var13) {
                  SSATransformer.SSAValue v = var11[var13];
                  if (v != null && v.used) {
                     if (v.parent != null) {
                        froms.add(v.parent.local);
                     }

                     if (v.otherParents != null) {
                        Iterator var15 = v.otherParents.iterator();

                        while(var15.hasNext()) {
                           SSATransformer.SSAValue parent = (SSATransformer.SSAValue)var15.next();
                           froms.add(parent.local);
                        }
                     }

                     froms.remove(v.local);
                     if (phis == null) {
                        phis = new ArrayList();
                     }

                     locals.add(v.local);
                     phis.add(Stmts.nAssign(v.local, Exprs.nPhi((Value[])froms.toArray(new Value[froms.size()]))));
                     froms.clear();
                  }
               }
            }

            labelStmt.phis = phis;
            if (phis != null) {
               phiLabels.add(labelStmt);
            }
         }

         p.frame = null;
      }

      if (phiLabels.size() > 0) {
         method.phiLabels = phiLabels;
      }

   }

   public void transform(final IrMethod method) {
      boolean needSSA = this.prepare(method);
      if (needSSA) {
         (new SSATransformer.SSAAnalyze(method)).analyze();
         this.deleteDeadCode(method);
         this.replaceLocalsWithSSA(method);
      }

      this.cleanTagsAndReIndex(method);
   }

   private boolean prepare(final IrMethod method) {
      int index = Cfg.reIndexLocal(method);
      final int[] readCounts = new int[index];
      final int[] writeCounts = new int[index];
      Cfg.travel(method.stmts, new Cfg.TravelCallBack() {
         public Value onAssign(Local v, AssignStmt as) {
            int var10002 = writeCounts[v._ls_index]++;
            return v;
         }

         public Value onUse(Local v) {
            int var10002 = readCounts[v._ls_index]++;
            return v;
         }
      }, true);
      boolean needTravel = false;
      boolean needSSAAnalyze = false;
      index = 0;
      List<Local> oldLocals = method.locals;
      List<Local> locals = new ArrayList(oldLocals);
      oldLocals.clear();
      Iterator var9 = locals.iterator();

      while(var9.hasNext()) {
         Local local = (Local)var9.next();
         int idx = local._ls_index;
         int read = readCounts[idx];
         int write = writeCounts[idx];
         if (read > 0 && write == 0) {
         }

         if (read != 0 || write != 0) {
            if (write <= 1) {
               local._ls_index = -1;
               oldLocals.add(local);
            } else if (read == 0) {
               local._ls_index = -2;
               needTravel = true;
            } else {
               needSSAAnalyze = true;
               local._ls_index = index++;
               oldLocals.add(local);
            }
         }
      }

      if (needSSAAnalyze || needTravel) {
         Cfg.travelMod(method.stmts, new Cfg.TravelCallBack() {
            public Value onAssign(Local v, AssignStmt as) {
               if (v._ls_index == -1) {
                  return v;
               } else if (v._ls_index == -2) {
                  Local n = (Local)v.clone();
                  method.locals.add(n);
                  return n;
               } else {
                  return v.clone();
               }
            }

            public Value onUse(Local v) {
               return (Value)(v._ls_index == -1 ? v : v.clone());
            }
         }, true);
      }

      return needSSAAnalyze;
   }

   private static class SSAValue implements AnalyzeValue {
      public Local local;
      public Set<SSATransformer.SSAValue> otherParents;
      public boolean used;
      public SSATransformer.SSAValue parent;

      private SSAValue() {
         this.used = false;
      }

      public char toRsp() {
         return (char)(this.used ? 'x' : '.');
      }

      public String toString() {
         return this.local != null ? this.local.toString() : "N";
      }

      // $FF: synthetic method
      SSAValue(Object x0) {
         this();
      }
   }

   static class SSAAnalyze extends BaseAnalyze<SSATransformer.SSAValue> {
      public int nextIndex;

      public SSAAnalyze(IrMethod method) {
         super(method, false);
      }

      protected void afterExec(SSATransformer.SSAValue[] frame, Stmt stmt) {
         if (stmt._cfg_froms.size() < 2) {
            this.setFrame(stmt, (AnalyzeValue[])null);
         }

      }

      public Local onUse(Local local) {
         return local._ls_index < 0 ? local : super.onUse(local);
      }

      public Local onAssign(Local local, AssignStmt as) {
         return local._ls_index < 0 ? local : super.onAssign(local, as);
      }

      protected void analyzeValue() {
         Set<SSATransformer.SSAValue> set = this.markUsed();
         this.aValues.clear();
         this.aValues = null;
         Iterator var2 = set.iterator();

         while(var2.hasNext()) {
            SSATransformer.SSAValue v0 = (SSATransformer.SSAValue)var2.next();
            if (v0.used && v0.local == null) {
               v0.local = new Local(this.nextIndex++);
            }
         }

      }

      protected void clearLsEmptyValueFromFrame() {
         for(Stmt p = this.method.stmts.getFirst(); p != null; p = p.getNext()) {
            SSATransformer.SSAValue[] frame = (SSATransformer.SSAValue[])((SSATransformer.SSAValue[])p.frame);
            if (frame != null) {
               for(int i = 0; i < frame.length; ++i) {
                  SSATransformer.SSAValue r = frame[i];
                  if (r != null && !r.used) {
                     frame[i] = null;
                  }
               }
            }
         }

      }

      protected void init() {
         super.init();
         this.nextIndex = this.method.locals.size();
      }

      protected void initCFG() {
         Cfg.createCFG(this.method);
      }

      protected Set<SSATransformer.SSAValue> markUsed() {
         Set<SSATransformer.SSAValue> used = new HashSet(this.aValues.size() / 2);
         Queue<SSATransformer.SSAValue> q = new UniqueQueue();
         q.addAll(this.aValues);

         while(true) {
            SSATransformer.SSAValue v;
            do {
               do {
                  if (q.isEmpty()) {
                     return used;
                  }

                  v = (SSATransformer.SSAValue)q.poll();
               } while(!v.used);

               used.add(v);
               SSATransformer.SSAValue p = v.parent;
               if (p != null && !p.used) {
                  p.used = true;
                  q.add(p);
               }
            } while(v.otherParents == null);

            Iterator var6 = v.otherParents.iterator();

            while(var6.hasNext()) {
               SSATransformer.SSAValue p = (SSATransformer.SSAValue)var6.next();
               if (!p.used) {
                  p.used = true;
                  q.add(p);
               }
            }
         }
      }

      public SSATransformer.SSAValue[] merge(SSATransformer.SSAValue[] frame, SSATransformer.SSAValue[] distFrame, Stmt src, Stmt dist) {
         if (distFrame != null) {
            this.relationMerge(frame, dist, distFrame);
         } else if (dist._cfg_froms.size() > 1) {
            distFrame = (SSATransformer.SSAValue[])this.newFrame();
            this.relationMerge(frame, dist, distFrame);
         } else if (needCopyFrame(src)) {
            distFrame = (SSATransformer.SSAValue[])this.newFrame();
            System.arraycopy(frame, 0, distFrame, 0, distFrame.length);
         } else {
            distFrame = frame;
         }

         return distFrame;
      }

      private static boolean needCopyFrame(Stmt src) {
         int c = 0;
         if (src.exceptionHandlers != null) {
            c += src.exceptionHandlers.size();
            if (c > 1) {
               return true;
            }
         }

         if (src.st.canContinue()) {
            ++c;
            if (c > 1) {
               return true;
            }
         }

         if (src.st.canBranch()) {
            ++c;
            if (c > 1) {
               return true;
            }
         }

         if (src.st.canSwitch()) {
            ++c;
            BaseSwitchStmt bss = (BaseSwitchStmt)src;
            c += bss.targets.length;
         }

         return c > 1;
      }

      protected SSATransformer.SSAValue[] newFrame(int size) {
         return new SSATransformer.SSAValue[size];
      }

      protected SSATransformer.SSAValue newValue() {
         return new SSATransformer.SSAValue();
      }

      protected SSATransformer.SSAValue onAssignLocal(Local local, Value value) {
         SSATransformer.SSAValue aValue = this.newValue();
         aValue.local = local;
         local.tag = aValue;
         return aValue;
      }

      protected void onUseLocal(SSATransformer.SSAValue aValue, Local local) {
         local.tag = aValue;
         aValue.used = true;
      }

      protected void relationMerge(SSATransformer.SSAValue[] frame, Stmt dist, SSATransformer.SSAValue[] distFrame) {
         for(int i = 0; i < this.localSize; ++i) {
            SSATransformer.SSAValue srcValue = frame[i];
            if (srcValue != null) {
               SSATransformer.SSAValue distValue = distFrame[i];
               if (distValue == null) {
                  if (!dist.visited) {
                     distValue = this.newValue();
                     this.aValues.add(distValue);
                     distFrame[i] = distValue;
                     this.linkParentChildren(srcValue, distValue);
                  }
               } else {
                  this.linkParentChildren(srcValue, distValue);
               }
            }
         }

      }

      private void linkParentChildren(SSATransformer.SSAValue p, SSATransformer.SSAValue c) {
         if (c.parent == null) {
            c.parent = p;
         } else {
            if (c.parent == p) {
               return;
            }

            Set<SSATransformer.SSAValue> ps = c.otherParents;
            if (ps == null) {
               c.otherParents = (Set)(ps = new HashSet(3));
            }

            ((Set)ps).add(p);
         }

      }
   }
}
