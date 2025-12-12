package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class AggTransformer extends StatedTransformer {
   private static AggTransformer.MergeResult FAIL = new AggTransformer.MergeResult();
   private static AggTransformer.MergeResult SUCCESS = new AggTransformer.MergeResult();

   public boolean transformReportChanged(IrMethod method) {
      boolean changed = false;
      Set<Stmt> locationSensitiveStmts = new HashSet();
      changed = this.simpleMergeLocals(method, changed, locationSensitiveStmts);
      if (locationSensitiveStmts.size() == 0) {
         return changed;
      } else {
         AggTransformer.ReplaceX replaceX = new AggTransformer.ReplaceX();
         Queue<Stmt> q = new UniqueQueue();
         q.addAll(locationSensitiveStmts);

         while(true) {
            while(!q.isEmpty()) {
               Stmt stmt = (Stmt)q.poll();
               Local local = (Local)stmt.getOp1();
               Stmt next = stmt.getNext();
               switch(next.st) {
               case LABEL:
               case GOTO:
               case IDENTITY:
               case FILL_ARRAY_DATA:
               case NOP:
               case RETURN_VOID:
                  break;
               default:
                  try {
                     localCanExecFirst(local, next);
                     throw new RuntimeException();
                  } catch (AggTransformer.MergeResult var11) {
                     if (var11 == SUCCESS) {
                        replaceX.local = local;
                        replaceX.replaceWith = stmt.getOp2();
                        method.locals.remove(local);
                        method.stmts.remove(stmt);
                        Cfg.travelMod((Stmt)next, replaceX, false);
                        Stmt pre = next.getPre();
                        if (pre != null && locationSensitiveStmts.contains(pre)) {
                           q.add(pre);
                        }
                     }
                  }
               }
            }

            return changed;
         }
      }
   }

   private static void localCanExecFirst(Local local, Stmt target) throws AggTransformer.MergeResult {
      switch(target.et) {
      case E0:
      case En:
         throw FAIL;
      case E1:
         localCanExecFirst(local, target.getOp());
         break;
      case E2:
         AssignStmt as = (AssignStmt)target;
         Value op1 = as.getOp1();
         Value op2 = as.getOp2();
         switch(op1.vt) {
         case LOCAL:
            localCanExecFirst(local, op2);
            break;
         case FIELD:
            localCanExecFirst(local, op1.getOp());
         case STATIC_FIELD:
            localCanExecFirst(local, op2);
            break;
         case ARRAY:
            localCanExecFirst(local, op1.getOp1());
            localCanExecFirst(local, op1.getOp2());
            localCanExecFirst(local, op2);
         }
      }

      throw FAIL;
   }

   private static void localCanExecFirst(Local local, Value op) throws AggTransformer.MergeResult {
      label39:
      switch(op.et) {
      case E0:
         if (local.vt == Value.VT.LOCAL && op == local) {
            throw SUCCESS;
         }
         break;
      case En:
         Value[] var2 = op.getOps();
         int var3 = var2.length;
         int var4 = 0;

         while(true) {
            if (var4 >= var3) {
               break label39;
            }

            Value v = var2[var4];
            localCanExecFirst(local, v);
            ++var4;
         }
      case E1:
         localCanExecFirst(local, op.getOp());
         break;
      case E2:
         localCanExecFirst(local, op.getOp1());
         localCanExecFirst(local, op.getOp2());
      }

      boolean shouldExclude = false;
      if (op.vt == Value.VT.INVOKE_STATIC) {
         InvokeExpr ie = (InvokeExpr)op;
         if (ie.getName().equals("valueOf") && ie.getOwner().startsWith("Ljava/lang/") && ie.getArgs().length == 1 && ie.getArgs()[0].length() == 1) {
            shouldExclude = true;
         }
      }

      if (!isLocationInsensitive(op.vt) && !shouldExclude) {
         throw FAIL;
      }
   }

   private boolean simpleMergeLocals(IrMethod method, boolean changed, Set<Stmt> locationSensitiveStmts) {
      if (method.locals.size() == 0) {
         return false;
      } else {
         int[] readCounts = Cfg.countLocalReads(method);
         Set<Local> useInPhi = this.collectLocalUsedInPhi(method);
         final Map<Local, Value> toReplace = new HashMap();
         Iterator it = method.stmts.iterator();

         while(it.hasNext()) {
            Stmt p = (Stmt)it.next();
            if (p.st == Stmt.ST.ASSIGN && p.getOp1().vt == Value.VT.LOCAL) {
               Local local = (Local)p.getOp1();
               if (!useInPhi.contains(local) && readCounts[local._ls_index] < 2) {
                  Value op2 = p.getOp2();
                  if (isLocationInsensitive(op2)) {
                     method.locals.remove(local);
                     toReplace.put(local, op2);
                     it.remove();
                     changed = true;
                  } else {
                     locationSensitiveStmts.add(p);
                  }
               }
            }
         }

         Cfg.TravelCallBack tcb = new Cfg.TravelCallBack() {
            public Value onAssign(Local v, AssignStmt as) {
               return v;
            }

            public Value onUse(Local v) {
               Value v2 = (Value)toReplace.get(v);
               return (Value)(v2 != null ? v2 : v);
            }
         };
         this.modReplace(toReplace, tcb);
         Cfg.travelMod(method.stmts, tcb, false);
         return changed;
      }
   }

   private Set<Local> collectLocalUsedInPhi(IrMethod method) {
      Set<Local> useInPhi = new HashSet();
      if (method.phiLabels != null) {
         Iterator var3 = method.phiLabels.iterator();

         while(true) {
            LabelStmt labelStmt;
            do {
               if (!var3.hasNext()) {
                  return useInPhi;
               }

               labelStmt = (LabelStmt)var3.next();
            } while(labelStmt.phis == null);

            Iterator var5 = labelStmt.phis.iterator();

            while(var5.hasNext()) {
               AssignStmt phi = (AssignStmt)var5.next();
               useInPhi.add((Local)phi.getOp1());
               Value[] var7 = phi.getOp2().getOps();
               int var8 = var7.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  Value op = var7[var9];
                  useInPhi.add((Local)op);
               }
            }
         }
      } else {
         return useInPhi;
      }
   }

   private void modReplace(Map<Local, Value> toReplace, Cfg.TravelCallBack tcb) {
      Iterator var3 = toReplace.entrySet().iterator();

      while(true) {
         while(var3.hasNext()) {
            Entry<Local, Value> e = (Entry)var3.next();
            Value v = (Value)e.getValue();
            if (v.vt == Value.VT.LOCAL) {
               Value v2;
               do {
                  v2 = (Value)toReplace.get(v);
                  if (v2 == null) {
                     break;
                  }

                  v = v2;
               } while(v2.vt == Value.VT.LOCAL);

               e.setValue(v);
            } else {
               Cfg.travelMod(v, tcb);
            }
         }

         return;
      }
   }

   static boolean isLocationInsensitive(Value.VT vt) {
      switch(vt) {
      case LOCAL:
      case CONSTANT:
      case ADD:
      case SUB:
      case MUL:
      case REM:
      case AND:
      case OR:
      case XOR:
      case SHL:
      case SHR:
      case USHR:
      case GE:
      case GT:
      case LE:
      case LT:
      case EQ:
      case NE:
      case DCMPG:
      case DCMPL:
      case LCMP:
      case FCMPG:
      case FCMPL:
      case NOT:
         return true;
      case FIELD:
      case STATIC_FIELD:
      case ARRAY:
      default:
         return false;
      }
   }

   static boolean isLocationInsensitive(Value op) {
      switch(op.et) {
      case E0:
         return isLocationInsensitive(op.vt);
      case En:
         int var3;
         if (op.vt == Value.VT.INVOKE_STATIC) {
            InvokeExpr ie = (InvokeExpr)op;
            if (ie.getName().equals("valueOf") && ie.getOwner().startsWith("Ljava/lang/") && ie.getArgs().length == 1 && ie.getArgs()[0].length() == 1) {
               Value[] var7 = op.getOps();
               var3 = var7.length;

               for(int var8 = 0; var8 < var3; ++var8) {
                  Value v = var7[var8];
                  if (!isLocationInsensitive(v)) {
                     return false;
                  }
               }

               return true;
            }

            return false;
         } else {
            if (isLocationInsensitive(op.vt)) {
               Value[] var1 = op.getOps();
               int var2 = var1.length;

               for(var3 = 0; var3 < var2; ++var3) {
                  Value v = var1[var3];
                  if (!isLocationInsensitive(v)) {
                     return false;
                  }
               }

               return true;
            }

            return false;
         }
      case E1:
         return isLocationInsensitive(op.vt) && isLocationInsensitive(op.getOp());
      case E2:
         return isLocationInsensitive(op.vt) && isLocationInsensitive(op.getOp1()) && isLocationInsensitive(op.getOp2());
      default:
         return false;
      }
   }

   static class ReplaceX implements Cfg.TravelCallBack {
      Local local;
      Value replaceWith;

      public Value onAssign(Local v, AssignStmt as) {
         return v;
      }

      public Value onUse(Local v) {
         return (Value)(v == this.local ? this.replaceWith : v);
      }
   }

   static class MergeResult extends Throwable {
   }
}
