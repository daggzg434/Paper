package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.PhiExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class ConstTransformer implements Transformer {
   public void transform(IrMethod m) {
      this.init(m);
      this.collect(m);
      this.markConstant(m);
      this.markReplacable(m);
      this.replace(m);
      this.clean(m);
   }

   private void clean(IrMethod m) {
      Local local;
      for(Iterator var2 = m.locals.iterator(); var2.hasNext(); local.tag = null) {
         local = (Local)var2.next();
      }

   }

   private void replace(IrMethod m) {
      Cfg.travelMod(m.stmts, new Cfg.TravelCallBack() {
         public Value onUse(Local v) {
            ConstTransformer.ConstAnalyzeValue cav = (ConstTransformer.ConstAnalyzeValue)v.tag;
            return (Value)(cav.replacable ? Exprs.nConstant(cav.cst) : v);
         }

         public Value onAssign(Local v, AssignStmt as) {
            ConstTransformer.ConstAnalyzeValue cav = (ConstTransformer.ConstAnalyzeValue)v.tag;
            if (cav.replacable && as.op2.trim().vt != Value.VT.CONSTANT) {
               as.op2 = Exprs.nConstant(cav.cst);
            }

            return v;
         }
      }, true);
   }

   private void markReplacable(IrMethod m) {
      Iterator var2 = m.locals.iterator();

      while(true) {
         ConstTransformer.ConstAnalyzeValue cav;
         do {
            if (!var2.hasNext()) {
               return;
            }

            Local local = (Local)var2.next();
            cav = (ConstTransformer.ConstAnalyzeValue)local.tag;
         } while(!Boolean.TRUE.equals(cav.isConst));

         boolean allTosAreCst = true;
         Iterator var6 = cav.assignTo.iterator();

         while(var6.hasNext()) {
            ConstTransformer.ConstAnalyzeValue c = (ConstTransformer.ConstAnalyzeValue)var6.next();
            if (!Boolean.TRUE.equals(c.isConst)) {
               allTosAreCst = false;
               break;
            }
         }

         if (allTosAreCst) {
            cav.replacable = true;
         }
      }
   }

   private void markConstant(IrMethod m) {
      Queue<Local> queue = new UniqueQueue();
      queue.addAll(m.locals);

      while(true) {
         ConstTransformer.ConstAnalyzeValue cav;
         Iterator var8;
         ConstTransformer.ConstAnalyzeValue p0;
         do {
            if (queue.isEmpty()) {
               return;
            }

            cav = (ConstTransformer.ConstAnalyzeValue)((Local)queue.poll()).tag;
            Object cst = cav.cst;
            if (cav.isConst == null && cst != null) {
               boolean allCstEquals = true;
               Iterator var6 = cav.assignFrom.iterator();

               while(var6.hasNext()) {
                  ConstTransformer.ConstAnalyzeValue p0 = (ConstTransformer.ConstAnalyzeValue)var6.next();
                  if (!cst.equals(p0.cst)) {
                     allCstEquals = false;
                     break;
                  }
               }

               if (allCstEquals) {
                  cav.isConst = true;
               }
            }

            if (cst != null || Boolean.TRUE.equals(cav.isConst)) {
               var8 = cav.assignTo.iterator();

               while(var8.hasNext()) {
                  p0 = (ConstTransformer.ConstAnalyzeValue)var8.next();
                  if (p0.isConst == null) {
                     if (p0.cst == null) {
                        p0.cst = cst;
                     }

                     queue.add(p0.local);
                  }
               }
            }
         } while(!Boolean.FALSE.equals(cav.isConst));

         cav.cst = null;
         var8 = cav.assignTo.iterator();

         while(var8.hasNext()) {
            p0 = (ConstTransformer.ConstAnalyzeValue)var8.next();
            if (!Boolean.FALSE.equals(p0.isConst)) {
               p0.cst = null;
               p0.isConst = false;
               queue.add(p0.local);
            }
         }
      }
   }

   private void collect(IrMethod m) {
      for(Stmt p = m.stmts.getFirst(); p != null; p = p.getNext()) {
         if (p.st == Stmt.ST.ASSIGN || p.st == Stmt.ST.IDENTITY) {
            Stmt.E2Stmt e2 = (Stmt.E2Stmt)p;
            Value op1 = e2.op1.trim();
            Value op2 = e2.op2.trim();
            if (op1.vt == Value.VT.LOCAL) {
               ConstTransformer.ConstAnalyzeValue cav = (ConstTransformer.ConstAnalyzeValue)((Local)op1).tag;
               if (op2.vt == Value.VT.CONSTANT) {
                  Constant c = (Constant)op2;
                  cav.isConst = true;
                  cav.cst = c.value;
               } else if (op2.vt == Value.VT.LOCAL) {
                  Local local2 = (Local)op2;
                  ConstTransformer.ConstAnalyzeValue zaf2 = (ConstTransformer.ConstAnalyzeValue)local2.tag;
                  cav.assignFrom.add(zaf2);
                  zaf2.assignTo.add(cav);
               } else if (op2.vt == Value.VT.PHI) {
                  PhiExpr pe = (PhiExpr)op2;
                  Value[] var8 = pe.ops;
                  int var9 = var8.length;

                  for(int var10 = 0; var10 < var9; ++var10) {
                     Value v = var8[var10];
                     ConstTransformer.ConstAnalyzeValue zaf2 = (ConstTransformer.ConstAnalyzeValue)((Local)v.trim()).tag;
                     cav.assignFrom.add(zaf2);
                     zaf2.assignTo.add(cav);
                  }
               } else {
                  cav.isConst = Boolean.FALSE;
               }
            }
         }
      }

   }

   private void init(IrMethod m) {
      Local local;
      for(Iterator var2 = m.locals.iterator(); var2.hasNext(); local.tag = new ConstTransformer.ConstAnalyzeValue(local)) {
         local = (Local)var2.next();
      }

   }

   static class ConstAnalyzeValue {
      private static final Integer ZERO = 0;
      public final Local local;
      public Boolean isConst = null;
      public boolean replacable = false;
      public Object cst;
      public Set<ConstTransformer.ConstAnalyzeValue> assignFrom = new HashSet(3);
      public Set<ConstTransformer.ConstAnalyzeValue> assignTo = new HashSet(3);

      public ConstAnalyzeValue(Local local) {
         this.local = local;
      }

      public boolean isZero() {
         if (this.isConst == null) {
            return false;
         } else {
            return this.isConst && ZERO.equals(this.cst);
         }
      }
   }
}
