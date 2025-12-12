package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.BaseSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.JumpStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class Cfg {
   public static int[] countLocalReads(IrMethod method) {
      int size = reIndexLocal(method);
      final int[] readCounts = new int[size];
      travel(method.stmts, new Cfg.TravelCallBack() {
         public Value onAssign(Local v, AssignStmt as) {
            return v;
         }

         public Value onUse(Local v) {
            int var10002 = readCounts[v._ls_index]++;
            return v;
         }
      }, true);
      return readCounts;
   }

   public static void reIndexLocalAndLabel(IrMethod irMethod) {
      reIndexLocal(irMethod);
      reIndexLabel(irMethod);
   }

   private static void reIndexLabel(IrMethod irMethod) {
      int i = 0;
      Iterator var2 = irMethod.stmts.iterator();

      while(var2.hasNext()) {
         Stmt stmt = (Stmt)var2.next();
         if (stmt.st == Stmt.ST.LABEL) {
            ((LabelStmt)stmt).displayName = "L" + i++;
         }
      }

   }

   public static boolean notThrow(Stmt s) {
      return !isThrow(s);
   }

   public static boolean isThrow(Stmt s) {
      Stmt.ST st = s.st;
      if (st.canThrow()) {
         return true;
      } else if (!st.mayThrow()) {
         return false;
      } else {
         ET et = s.et;
         if (et == ET.E1) {
            return isThrow(s.getOp());
         } else if (et != ET.E2) {
            throw new RuntimeException();
         } else {
            return isThrow(s.getOp1()) || isThrow(s.getOp2());
         }
      }
   }

   private static boolean isThrow(Value op) {
      Value.VT vt = op.vt;
      if (vt.canThrow()) {
         return true;
      } else if (vt.mayThrow()) {
         switch(op.et) {
         case E1:
            return isThrow(op.getOp());
         case E2:
            return isThrow(op.getOp1()) || isThrow(op.getOp2());
         case En:
         case E0:
         default:
            throw new RuntimeException();
         }
      } else {
         return false;
      }
   }

   public static void createCfgWithoutEx(IrMethod jm) {
      Iterator var1 = jm.stmts.iterator();

      Stmt st;
      while(var1.hasNext()) {
         st = (Stmt)var1.next();
         st.frame = null;
         st.exceptionHandlers = null;
         if (st._cfg_froms == null) {
            st._cfg_froms = new TreeSet(jm.stmts);
         } else {
            st._cfg_froms.clear();
         }
      }

      var1 = jm.stmts.iterator();

      while(true) {
         do {
            if (!var1.hasNext()) {
               return;
            }

            st = (Stmt)var1.next();
            if (st.st.canBranch()) {
               link(st, ((JumpStmt)st).getTarget());
            }

            if (st.st.canContinue()) {
               link(st, st.getNext());
            }
         } while(!st.st.canSwitch());

         BaseSwitchStmt bss = (BaseSwitchStmt)st;
         link(st, bss.defaultTarget);
         LabelStmt[] var4 = bss.targets;
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Stmt target = var4[var6];
            link(st, target);
         }
      }
   }

   public static void createCFG(IrMethod jm) {
      createCfgWithoutEx(jm);
      Iterator var1 = jm.traps.iterator();

      while(var1.hasNext()) {
         Trap t = (Trap)var1.next();

         for(Object s = t.start; s != t.end; s = ((Stmt)s).getNext()) {
            if (isThrow((Stmt)s)) {
               Set<LabelStmt> hs = ((Stmt)s).exceptionHandlers;
               if (hs == null) {
                  hs = new TreeSet(jm.stmts);
                  ((Stmt)s).exceptionHandlers = (Set)hs;
               }

               LabelStmt[] var5 = t.handlers;
               int var6 = var5.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  LabelStmt handler = var5[var7];
                  link((Stmt)s, handler);
                  ((Set)hs).add(handler);
               }
            }
         }
      }

   }

   public static void dfsVisit(IrMethod method, Cfg.DfsVisitor visitor) {
      Stmt currentStmt;
      for(Iterator var2 = method.stmts.iterator(); var2.hasNext(); currentStmt.visited = false) {
         currentStmt = (Stmt)var2.next();
      }

      Stack<Stmt> stack = new Stack();
      stack.add(method.stmts.getFirst());

      while(true) {
         do {
            if (stack.isEmpty()) {
               return;
            }

            currentStmt = (Stmt)stack.pop();
         } while(currentStmt.visited);

         currentStmt.visited = true;
         LabelStmt target;
         if (currentStmt.exceptionHandlers != null) {
            Iterator var4 = currentStmt.exceptionHandlers.iterator();

            while(var4.hasNext()) {
               target = (LabelStmt)var4.next();
               stack.push(target);
            }
         }

         if (visitor != null) {
            visitor.onVisit(currentStmt);
         }

         if (currentStmt.st.canSwitch()) {
            BaseSwitchStmt bs = (BaseSwitchStmt)currentStmt;
            Collections.addAll(stack, bs.targets);
            target = bs.defaultTarget;
            stack.add(target);
         }

         if (currentStmt.st.canBranch()) {
            Stmt target = ((JumpStmt)currentStmt).getTarget();
            stack.add(target);
         }

         if (currentStmt.st.canContinue()) {
            Stmt target = currentStmt.getNext();
            stack.add(target);
         }
      }
   }

   public static <T> void dfs(StmtList stmts, Cfg.FrameVisitor<T> sv) {
      if (stmts.getSize() != 0) {
         Stmt first;
         for(Iterator var2 = stmts.iterator(); var2.hasNext(); first.frame = null) {
            first = (Stmt)var2.next();
            first.visited = false;
         }

         Stack<Stmt> stack = new Stack();
         first = stmts.getFirst();
         Stmt nop = null;
         if (first.st == Stmt.ST.LABEL && first._cfg_froms.size() > 0) {
            nop = Stmts.nNop();
            first._cfg_froms.add(nop);
         }

         stack.add(first);
         first.frame = sv.initFirstFrame(first);

         while(true) {
            Stmt currentStmt;
            do {
               do {
                  if (stack.isEmpty()) {
                     if (nop != null) {
                        first._cfg_froms.remove(nop);
                     }

                     return;
                  }

                  currentStmt = (Stmt)stack.pop();
               } while(currentStmt == null);
            } while(currentStmt.visited);

            currentStmt.visited = true;
            T beforeExecFrame = currentStmt.frame;
            LabelStmt target;
            if (currentStmt.exceptionHandlers != null) {
               Iterator var7 = currentStmt.exceptionHandlers.iterator();

               while(var7.hasNext()) {
                  target = (LabelStmt)var7.next();
                  target.frame = sv.merge(beforeExecFrame, target.frame, currentStmt, target);
                  stack.push(target);
               }
            }

            T afterExecFrame = sv.exec(beforeExecFrame, currentStmt);
            if (currentStmt.st.canSwitch()) {
               BaseSwitchStmt bs = (BaseSwitchStmt)currentStmt;
               LabelStmt[] var9 = bs.targets;
               int var10 = var9.length;

               for(int var11 = 0; var11 < var10; ++var11) {
                  LabelStmt target = var9[var11];
                  target.frame = sv.merge(afterExecFrame, target.frame, currentStmt, target);
                  stack.push(target);
               }

               LabelStmt target = bs.defaultTarget;
               target.frame = sv.merge(afterExecFrame, target.frame, currentStmt, target);
               stack.push(target);
            }

            if (currentStmt.st.canBranch()) {
               target = ((JumpStmt)currentStmt).getTarget();
               target.frame = sv.merge(afterExecFrame, target.frame, currentStmt, target);
               stack.push(target);
            }

            if (currentStmt.st.canContinue()) {
               Stmt target = currentStmt.getNext();
               target.frame = sv.merge(afterExecFrame, target.frame, currentStmt, target);
               stack.push(target);
            }
         }
      }
   }

   private static void link(Stmt from, Stmt to) {
      if (to != null) {
         to._cfg_froms.add(from);
      }
   }

   public static Value travelMod(Value value, Cfg.OnUseCallBack callback) {
      switch(value.et) {
      case E1:
         value.setOp(travelMod(value.getOp(), callback));
         break;
      case E2:
         value.setOp1(travelMod(value.getOp1(), callback));
         value.setOp2(travelMod(value.getOp2(), callback));
         break;
      case En:
         Value[] ops = value.getOps();

         for(int i = 0; i < ops.length; ++i) {
            ops[i] = travelMod(ops[i], callback);
         }

         return value;
      case E0:
         if (value.vt == Value.VT.LOCAL) {
            return callback.onUse((Local)value);
         }
      }

      return value;
   }

   public static void travel(Value value, Cfg.OnUseCallBack callback) {
      switch(value.et) {
      case E1:
         travel(value.getOp(), callback);
         break;
      case E2:
         travel(value.getOp1(), callback);
         travel(value.getOp2(), callback);
         break;
      case En:
         Value[] ops = value.getOps();

         for(int i = 0; i < ops.length; ++i) {
            travel(ops[i], callback);
         }

         return;
      case E0:
         if (value.vt == Value.VT.LOCAL) {
            callback.onUse((Local)value);
         }
      }

   }

   public static void travelMod(Stmt p, Cfg.TravelCallBack callback, boolean travelPhi) {
      switch(p.et) {
      case E1:
         p.setOp(travelMod(p.getOp(), callback));
         break;
      case E2:
         Value e2op1 = p.getOp1();
         if (e2op1.vt != Value.VT.LOCAL || p.st != Stmt.ST.ASSIGN && p.st != Stmt.ST.IDENTITY) {
            p.setOp1(travelMod(p.getOp1(), callback));
            p.setOp2(travelMod(p.getOp2(), callback));
         } else {
            p.setOp2(travelMod(p.getOp2(), callback));
            p.setOp1(callback.onAssign((Local)e2op1, (AssignStmt)p));
         }
         break;
      case En:
      case E0:
         if (travelPhi && p.st == Stmt.ST.LABEL) {
            LabelStmt labelStmt = (LabelStmt)p;
            if (labelStmt.phis != null) {
               Iterator var5 = labelStmt.phis.iterator();

               while(var5.hasNext()) {
                  AssignStmt phi = (AssignStmt)var5.next();
                  travelMod((Stmt)phi, callback, false);
               }
            }
         }
      }

   }

   public static void travel(Stmt p, Cfg.TravelCallBack callback, boolean travelPhi) {
      switch(p.et) {
      case E1:
         travel(p.getOp(), callback);
         break;
      case E2:
         Value e2op1 = p.getOp1();
         if (e2op1.vt != Value.VT.LOCAL || p.st != Stmt.ST.ASSIGN && p.st != Stmt.ST.IDENTITY) {
            travel(p.getOp1(), callback);
            travel(p.getOp2(), callback);
         } else {
            travel(p.getOp2(), callback);
            callback.onAssign((Local)e2op1, (AssignStmt)p);
         }
         break;
      case En:
      case E0:
         if (travelPhi && p.st == Stmt.ST.LABEL) {
            LabelStmt labelStmt = (LabelStmt)p;
            if (labelStmt.phis != null) {
               Iterator var5 = labelStmt.phis.iterator();

               while(var5.hasNext()) {
                  AssignStmt phi = (AssignStmt)var5.next();
                  travel((Stmt)phi, callback, false);
               }
            }
         }
      }

   }

   public static void travel(StmtList stmts, Cfg.TravelCallBack callback, boolean travelPhi) {
      for(Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
         travel(p, callback, travelPhi);
      }

   }

   public static void travelMod(StmtList stmts, Cfg.TravelCallBack callback, boolean travelPhi) {
      for(Stmt p = stmts.getFirst(); p != null; p = p.getNext()) {
         travelMod(p, callback, travelPhi);
      }

   }

   public static int reIndexLocal(IrMethod method) {
      int i = 0;

      Local local;
      for(Iterator var2 = method.locals.iterator(); var2.hasNext(); local._ls_index = i++) {
         local = (Local)var2.next();
      }

      return i;
   }

   public static void collectTos(Stmt stmt, Set<Stmt> tos) {
      if (stmt.st.canBranch()) {
         tos.add(((JumpStmt)stmt).getTarget());
      }

      if (stmt.st.canContinue()) {
         tos.add(stmt.getNext());
      }

      if (stmt.st.canSwitch()) {
         BaseSwitchStmt bss = (BaseSwitchStmt)stmt;
         tos.add(bss.defaultTarget);
         LabelStmt[] var3 = bss.targets;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Stmt target = var3[var5];
            tos.add(target);
         }
      }

      if (stmt.exceptionHandlers != null) {
         tos.addAll(stmt.exceptionHandlers);
      }

   }

   public interface TravelCallBack extends Cfg.OnUseCallBack, Cfg.OnAssignCallBack {
   }

   public interface OnAssignCallBack {
      Value onAssign(Local v, AssignStmt as);
   }

   public interface OnUseCallBack {
      Value onUse(Local v);
   }

   public interface DfsVisitor {
      void onVisit(Stmt p);
   }

   public interface FrameVisitor<T> {
      T merge(T srcFrame, T distFrame, Stmt src, Stmt dist);

      T initFirstFrame(Stmt first);

      T exec(T frame, Stmt stmt);
   }
}
