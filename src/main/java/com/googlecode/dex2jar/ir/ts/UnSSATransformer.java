package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.PhiExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.an.AnalyzeValue;
import com.googlecode.dex2jar.ir.ts.an.BaseAnalyze;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class UnSSATransformer implements Transformer {
   private static final boolean DEBUG = false;
   protected static final Comparator<UnSSATransformer.RegAssign> OrderRegAssignByExcludeSizeDesc = new Comparator<UnSSATransformer.RegAssign>() {
      public int compare(UnSSATransformer.RegAssign o1, UnSSATransformer.RegAssign o2) {
         return o2.excludes.size() - o1.excludes.size();
      }
   };

   private void fixPhi(IrMethod method, Collection<LabelStmt> phiLabels) {
      Iterator var3 = phiLabels.iterator();

      label61:
      while(var3.hasNext()) {
         LabelStmt labelStmt = (LabelStmt)var3.next();
         List<AssignStmt> phis = labelStmt.phis;
         Iterator var6 = phis.iterator();

         while(true) {
            AssignStmt phi;
            Local a;
            boolean introduceNewLocal;
            do {
               if (!var6.hasNext()) {
                  continue label61;
               }

               phi = (AssignStmt)var6.next();
               a = (Local)phi.getOp1();
               PhiExpr b = (PhiExpr)phi.getOp2();
               introduceNewLocal = false;
               UnSSATransformer.RegAssign aReg = (UnSSATransformer.RegAssign)a.tag;
               Value[] var12 = b.getOps();
               int var13 = var12.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  Value op = var12[var14];
                  UnSSATransformer.RegAssign bReg = (UnSSATransformer.RegAssign)((Local)op).tag;
                  if (aReg.excludes.contains(bReg)) {
                     introduceNewLocal = true;
                     break;
                  }
               }
            } while(!introduceNewLocal);

            Local newLocal = (Local)a.clone();
            phi.op1 = newLocal;
            UnSSATransformer.RegAssign newRegAssign = new UnSSATransformer.RegAssign();
            newLocal.tag = newRegAssign;
            method.locals.add(newLocal);
            Stmt newAssigStmt = Stmts.nAssign(a, newLocal);
            Stmt next = labelStmt.getNext();
            if (next != null && next.st == Stmt.ST.IDENTITY && next.getOp2().vt == Value.VT.EXCEPTION_REF) {
               method.stmts.insertAfter(next, newAssigStmt);
            } else {
               method.stmts.insertAfter(labelStmt, newAssigStmt);
            }

            UnSSATransformer.LiveV[] frame = (UnSSATransformer.LiveV[])((UnSSATransformer.LiveV[])labelStmt.frame);
            UnSSATransformer.LiveV thePhi = frame[a._ls_index];
            thePhi.local = newLocal;
            UnSSATransformer.LiveV[] var18 = frame;
            int var19 = frame.length;

            for(int var20 = 0; var20 < var19; ++var20) {
               UnSSATransformer.LiveV v = var18[var20];
               if (v != null && v.used) {
                  UnSSATransformer.RegAssign s = (UnSSATransformer.RegAssign)v.local.tag;
                  s.excludes.add(newRegAssign);
                  newRegAssign.excludes.add(s);
               }
            }
         }
      }

   }

   private void insertAssignPath(IrMethod method, Collection<LabelStmt> phiLabels) {
      List<AssignStmt> buff = new ArrayList();
      Iterator var4 = phiLabels.iterator();

      label36:
      while(var4.hasNext()) {
         LabelStmt labelStmt = (LabelStmt)var4.next();
         List<AssignStmt> phis = labelStmt.phis;
         UnSSATransformer.LiveV[] frame = (UnSSATransformer.LiveV[])((UnSSATransformer.LiveV[])labelStmt.frame);
         Iterator var8 = labelStmt._cfg_froms.iterator();

         while(true) {
            Stmt from;
            do {
               if (!var8.hasNext()) {
                  continue label36;
               }

               from = (Stmt)var8.next();
            } while(!from.visited);

            Iterator var10 = phis.iterator();

            while(var10.hasNext()) {
               AssignStmt phi = (AssignStmt)var10.next();
               Local a = (Local)phi.getOp1();
               UnSSATransformer.LiveV v = frame[a._ls_index];
               Local local = (Local)v.stmt2regMap.get(from);
               if (local != a) {
                  buff.add(Stmts.nAssign(a, local));
               }
            }

            this.insertAssignPath(method.stmts, from, labelStmt, buff);
            buff.clear();
         }
      }

   }

   private void insertAssignPath(StmtList stmts, Stmt from, LabelStmt labelStmt, List<AssignStmt> buff) {
      boolean insertBeforeFromStmt;
      if (from.exceptionHandlers != null && from.exceptionHandlers.contains(labelStmt)) {
         insertBeforeFromStmt = true;
      } else {
         switch(from.st) {
         case GOTO:
         case IF:
            JumpStmt jumpStmt = (JumpStmt)from;
            insertBeforeFromStmt = jumpStmt.getTarget().equals(labelStmt);
            break;
         case TABLE_SWITCH:
         case LOOKUP_SWITCH:
            insertBeforeFromStmt = true;
            break;
         default:
            insertBeforeFromStmt = false;
         }
      }

      AssignStmt as;
      Iterator var17;
      if (insertBeforeFromStmt) {
         var17 = buff.iterator();

         while(var17.hasNext()) {
            as = (AssignStmt)var17.next();
            stmts.insertBefore(from, as);
         }
      } else {
         var17 = buff.iterator();

         while(var17.hasNext()) {
            as = (AssignStmt)var17.next();
            stmts.insertAfter(from, as);
         }
      }

      UnSSATransformer.LiveV[] frame = (UnSSATransformer.LiveV[])((UnSSATransformer.LiveV[])from.frame);
      List<UnSSATransformer.LiveV> newLiveVs = new ArrayList(buff.size());
      Iterator var8 = buff.iterator();

      while(var8.hasNext()) {
         AssignStmt as = (AssignStmt)var8.next();
         Local left = (Local)as.getOp1();
         UnSSATransformer.LiveV liveV = new UnSSATransformer.LiveV();
         liveV.local = left;
         liveV.used = true;
         newLiveVs.add(liveV);
         UnSSATransformer.RegAssign leftRegAssign = (UnSSATransformer.RegAssign)left.tag;
         Local right = (Local)as.getOp2();
         int toSkip = right._ls_index;

         UnSSATransformer.RegAssign assign;
         for(int i = 0; i < frame.length; ++i) {
            if (i != toSkip) {
               UnSSATransformer.LiveV v = frame[i];
               if (v != null && v.used) {
                  assign = (UnSSATransformer.RegAssign)v.local.tag;
                  assign.excludes.add(leftRegAssign);
                  leftRegAssign.excludes.add(assign);
               }
            }
         }

         Iterator var23 = buff.iterator();

         while(var23.hasNext()) {
            AssignStmt as2 = (AssignStmt)var23.next();
            assign = (UnSSATransformer.RegAssign)((Local)as2.getOp1()).tag;
            assign.excludes.add(leftRegAssign);
            leftRegAssign.excludes.add(assign);
         }
      }

      UnSSATransformer.LiveV[] newFrame = new UnSSATransformer.LiveV[frame.length + newLiveVs.size()];
      System.arraycopy(frame, 0, newFrame, 0, frame.length);

      for(int i = 0; i < newLiveVs.size(); ++i) {
         newFrame[i + frame.length] = (UnSSATransformer.LiveV)newLiveVs.get(i);
      }

   }

   public void transform(IrMethod method) {
      if (method.phiLabels != null && method.phiLabels.size() != 0) {
         Iterator var2 = method.phiLabels.iterator();

         Stmt stmt;
         while(var2.hasNext()) {
            LabelStmt phiLabel = (LabelStmt)var2.next();
            stmt = phiLabel.getNext();
            if (stmt.st == Stmt.ST.LABEL) {
               LabelStmt labelStmt2 = (LabelStmt)stmt;
               if (labelStmt2.phis != null && labelStmt2.phis.size() > 0) {
                  method.stmts.insertAfter(phiLabel, Stmts.nNop());
               }
            }
         }

         UnSSATransformer.LiveA liveA = new UnSSATransformer.LiveA(method);
         liveA.analyze();
         this.genRegGraph(method, liveA);
         this.fixPhi(method, method.phiLabels);
         this.insertAssignPath(method, method.phiLabels);

         Iterator var7;
         Local local;
         for(var7 = method.locals.iterator(); var7.hasNext(); local.tag = null) {
            local = (Local)var7.next();
         }

         for(var7 = method.stmts.iterator(); var7.hasNext(); stmt.frame = null) {
            stmt = (Stmt)var7.next();
         }

         LabelStmt labelStmt;
         for(var7 = method.phiLabels.iterator(); var7.hasNext(); labelStmt.phis = null) {
            labelStmt = (LabelStmt)var7.next();
         }

         method.phiLabels = null;
      }
   }

   private void genRegGraph(IrMethod method, UnSSATransformer.LiveA liveA) {
      Local local;
      for(Iterator var3 = method.locals.iterator(); var3.hasNext(); local.tag = new UnSSATransformer.RegAssign()) {
         local = (Local)var3.next();
      }

      Set<Stmt> tos = new HashSet();
      Iterator var18 = method.stmts.iterator();

      while(true) {
         label73:
         while(var18.hasNext()) {
            Stmt stmt = (Stmt)var18.next();
            UnSSATransformer.LiveV[] frame;
            if ((stmt.st == Stmt.ST.ASSIGN || stmt.st == Stmt.ST.IDENTITY) && stmt.getOp1().vt == Value.VT.LOCAL) {
               Local localAssignTo = (Local)stmt.getOp1();
               UnSSATransformer.RegAssign regAssignTo = (UnSSATransformer.RegAssign)localAssignTo.tag;
               Set<Integer> excludeIdx = new HashSet();
               Cfg.collectTos(stmt, tos);
               Iterator var22 = tos.iterator();

               while(true) {
                  Stmt target;
                  do {
                     if (!var22.hasNext()) {
                        tos.clear();
                        continue label73;
                     }

                     target = (Stmt)var22.next();
                     frame = (UnSSATransformer.LiveV[])((UnSSATransformer.LiveV[])target.frame);
                  } while(frame == null);

                  excludeIdx.clear();
                  excludeIdx.add(localAssignTo._ls_index);
                  if (target.st == Stmt.ST.LABEL) {
                     LabelStmt label = (LabelStmt)target;
                     if (label.phis != null) {
                        Iterator var26 = label.phis.iterator();

                        while(var26.hasNext()) {
                           AssignStmt phiAssignStmt = (AssignStmt)var26.next();
                           Local phiLocal = (Local)phiAssignStmt.getOp1();
                           excludeIdx.add(phiLocal._ls_index);
                        }
                     }
                  }

                  for(int i = 0; i < frame.length; ++i) {
                     if (!excludeIdx.contains(i)) {
                        UnSSATransformer.LiveV v = frame[i];
                        if (v != null && v.used) {
                           UnSSATransformer.RegAssign b = (UnSSATransformer.RegAssign)v.local.tag;
                           regAssignTo.excludes.add(b);
                           b.excludes.add(regAssignTo);
                        }
                     }
                  }
               }
            } else if (stmt.st == Stmt.ST.LABEL) {
               LabelStmt label = (LabelStmt)stmt;
               if (label.phis != null) {
                  Iterator var7 = label.phis.iterator();

                  while(var7.hasNext()) {
                     AssignStmt phiAssignStmt = (AssignStmt)var7.next();
                     Local phiLocal = (Local)phiAssignStmt.getOp1();
                     UnSSATransformer.RegAssign a = (UnSSATransformer.RegAssign)phiLocal.tag;
                     frame = (UnSSATransformer.LiveV[])((UnSSATransformer.LiveV[])stmt.frame);
                     UnSSATransformer.LiveV[] var12 = frame;
                     int var13 = frame.length;

                     for(int var14 = 0; var14 < var13; ++var14) {
                        UnSSATransformer.LiveV v = var12[var14];
                        if (v != null && v.used) {
                           UnSSATransformer.RegAssign b = (UnSSATransformer.RegAssign)v.local.tag;
                           a.excludes.add(b);
                           b.excludes.add(a);
                        }
                     }
                  }
               }
            }
         }

         return;
      }
   }

   protected static class RegAssign {
      public Set<UnSSATransformer.RegAssign> excludes = new HashSet();
   }

   private static class LiveV implements AnalyzeValue {
      public int hops;
      public Local local;
      public UnSSATransformer.LiveV parent;
      public boolean used;
      public List<UnSSATransformer.LiveV> otherParents;
      Map<Stmt, Local> stmt2regMap;

      private LiveV() {
      }

      public char toRsp() {
         return (char)(this.used ? 'x' : '?');
      }

      public String toString() {
         return this.local + "|" + this.hops;
      }

      // $FF: synthetic method
      LiveV(Object x0) {
         this();
      }
   }

   protected static class LiveA extends BaseAnalyze<UnSSATransformer.LiveV> {
      static Comparator<UnSSATransformer.LiveV> sortByHopsASC = new Comparator<UnSSATransformer.LiveV>() {
         public int compare(UnSSATransformer.LiveV arg0, UnSSATransformer.LiveV arg1) {
            return arg0.hops - arg1.hops;
         }
      };

      public LiveA(IrMethod method) {
         super(method);
      }

      protected void analyzeValue() {
         this.markUsed();
      }

      protected void clearUnUsedFromFrame() {
         for(Stmt p = this.method.stmts.getFirst(); p != null; p = p.getNext()) {
            UnSSATransformer.LiveV[] frame = (UnSSATransformer.LiveV[])((UnSSATransformer.LiveV[])p.frame);
            if (frame != null) {
               for(int i = 0; i < frame.length; ++i) {
                  UnSSATransformer.LiveV r = frame[i];
                  if (r != null && !r.used) {
                     frame[i] = null;
                  }
               }
            }
         }

      }

      protected Set<UnSSATransformer.LiveV> markUsed() {
         Set<UnSSATransformer.LiveV> used = new HashSet(this.aValues.size() / 2);
         Queue<UnSSATransformer.LiveV> q = new UniqueQueue();
         q.addAll(this.aValues);

         while(true) {
            UnSSATransformer.LiveV v;
            List otherParent;
            do {
               do {
                  UnSSATransformer.LiveV parent;
                  do {
                     do {
                        if (q.isEmpty()) {
                           for(Iterator var7 = this.aValues.iterator(); var7.hasNext(); parent.parent = null) {
                              parent = (UnSSATransformer.LiveV)var7.next();
                           }

                           this.aValues = null;
                           return used;
                        }

                        v = (UnSSATransformer.LiveV)q.poll();
                     } while(!v.used);
                  } while(used.contains(v));

                  used.add(v);
                  parent = v.parent;
                  if (parent != null && !parent.used) {
                     parent.used = true;
                     q.add(parent);
                  }

                  otherParent = v.otherParents;
               } while(otherParent == null);
            } while(otherParent.size() <= 0);

            Iterator var5 = otherParent.iterator();

            while(var5.hasNext()) {
               UnSSATransformer.LiveV parent = (UnSSATransformer.LiveV)var5.next();
               if (parent != null && !parent.used) {
                  parent.used = true;
                  q.add(parent);
               }
            }

            v.otherParents = null;
         }
      }

      public UnSSATransformer.LiveV[] merge(UnSSATransformer.LiveV[] srcFrame, UnSSATransformer.LiveV[] distFrame, Stmt src, Stmt dist) {
         Map<Integer, AssignStmt> phiLives = new HashMap();
         Iterator var7;
         AssignStmt phiAssignStmt;
         Local phiLocal;
         if (dist.st == Stmt.ST.LABEL) {
            LabelStmt label = (LabelStmt)dist;
            if (label.phis != null) {
               var7 = label.phis.iterator();

               while(var7.hasNext()) {
                  phiAssignStmt = (AssignStmt)var7.next();
                  phiLocal = (Local)phiAssignStmt.getOp1();
                  phiLives.put(phiLocal._ls_index, phiAssignStmt);
               }
            }
         }

         boolean firstMerge = false;
         int i;
         UnSSATransformer.LiveV srcV;
         UnSSATransformer.LiveV distV;
         if (distFrame == null) {
            distFrame = (UnSSATransformer.LiveV[])this.newFrame();
            firstMerge = true;

            for(i = 0; i < distFrame.length; ++i) {
               if (!phiLives.containsKey(i)) {
                  srcV = srcFrame[i];
                  if (srcV != null) {
                     distV = this.newValue();
                     this.aValues.add(distV);
                     distV.parent = srcV;
                     distV.hops = srcV.hops + 1;
                     distV.local = srcV.local;
                     distFrame[i] = distV;
                  }
               }
            }
         }

         if (!firstMerge) {
            for(i = 0; i < distFrame.length; ++i) {
               if (!phiLives.containsKey(i)) {
                  srcV = srcFrame[i];
                  distV = distFrame[i];
                  if (srcV != null && distV != null) {
                     if (distV.otherParents == null) {
                        distV.otherParents = new ArrayList(5);
                     }

                     distV.otherParents.add(srcV);
                  }
               }
            }
         }

         var7 = phiLives.values().iterator();

         while(var7.hasNext()) {
            phiAssignStmt = (AssignStmt)var7.next();
            phiLocal = (Local)phiAssignStmt.getOp1();
            UnSSATransformer.LiveV distValue;
            if (firstMerge) {
               distValue = new UnSSATransformer.LiveV();
               distValue.local = phiLocal;
               distValue.stmt2regMap = new HashMap();
               distFrame[phiLocal._ls_index] = distValue;
            } else {
               distValue = distFrame[phiLocal._ls_index];
            }

            List<UnSSATransformer.LiveV> liveVs = new ArrayList();
            UnSSATransformer.LiveV possiblePhiLocal = srcFrame[phiLocal._ls_index];
            if (possiblePhiLocal != null) {
               liveVs.add(possiblePhiLocal);
            }

            Value[] var13 = phiAssignStmt.getOp2().getOps();
            int var14 = var13.length;

            for(int var15 = 0; var15 < var14; ++var15) {
               Value p0 = var13[var15];
               Local srcLocal = (Local)p0;
               UnSSATransformer.LiveV s = srcFrame[srcLocal._ls_index];
               if (s != null) {
                  liveVs.add(s);
               }
            }

            Collections.sort(liveVs, sortByHopsASC);
            UnSSATransformer.LiveV a = (UnSSATransformer.LiveV)liveVs.get(0);
            a.used = true;
            distValue.stmt2regMap.put(src, a.local);
         }

         return distFrame;
      }

      protected UnSSATransformer.LiveV[] newFrame(int size) {
         return new UnSSATransformer.LiveV[size];
      }

      protected UnSSATransformer.LiveV newValue() {
         return new UnSSATransformer.LiveV();
      }

      protected UnSSATransformer.LiveV onAssignLocal(Local local, Value value) {
         UnSSATransformer.LiveV v = (UnSSATransformer.LiveV)super.onAssignLocal(local, value);
         v.local = local;
         v.used = true;
         return v;
      }

      protected void onUseLocal(UnSSATransformer.LiveV aValue, Local local) {
         aValue.used = true;
      }
   }
}
