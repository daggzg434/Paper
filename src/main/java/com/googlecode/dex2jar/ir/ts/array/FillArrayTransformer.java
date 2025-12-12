package com.googlecode.dex2jar.ir.ts.array;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Cfg;
import com.googlecode.dex2jar.ir.ts.StatedTransformer;
import com.googlecode.dex2jar.ir.ts.UniqueQueue;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

public class FillArrayTransformer extends StatedTransformer {
   public static void main(String... args) {
      IrMethod m = new IrMethod();
      m.isStatic = true;
      m.name = "a";
      m.args = new String[0];
      m.ret = "[Ljava/lang/String;";
      m.owner = "La;";
      Local array = Exprs.nLocal(1);
      m.locals.add(array);
      m.stmts.add(Stmts.nAssign(array, Exprs.nNewArray("Ljava/lang/String;", Exprs.nInt(2))));
      m.stmts.add(Stmts.nAssign(Exprs.nArray(array, Exprs.nInt(1), "Ljava/lang/String;"), Exprs.nString("123")));
      m.stmts.add(Stmts.nAssign(Exprs.nArray(array, Exprs.nInt(0), "Ljava/lang/String;"), Exprs.nString("456")));
      m.stmts.add(Stmts.nReturn(array));
      (new FillArrayTransformer()).transform(m);
      System.out.println(m);
   }

   public boolean transformReportChanged(IrMethod method) {
      Map<Local, FillArrayTransformer.ArrayObject> arraySizes = this.searchForArrayObject(method);
      if (arraySizes.size() == 0) {
         return false;
      } else {
         this.makeSureAllElementAreAssigned(arraySizes);
         if (arraySizes.size() == 0) {
            return false;
         } else {
            this.makeSureArrayUsedAfterAllElementAssigned(method, arraySizes);
            if (arraySizes.size() == 0) {
               return false;
            } else {
               this.replace(method, arraySizes);
               return true;
            }
         }
      }
   }

   private void replace(IrMethod method, Map<Local, FillArrayTransformer.ArrayObject> arraySizes) {
      final List<FilledArrayExpr> filledArrayExprs = new ArrayList();
      Iterator var4 = arraySizes.entrySet().iterator();

      while(true) {
         while(true) {
            label71:
            while(var4.hasNext()) {
               Entry<Local, FillArrayTransformer.ArrayObject> e = (Entry)var4.next();
               final Local local0 = (Local)e.getKey();
               final FillArrayTransformer.ArrayObject ao = (FillArrayTransformer.ArrayObject)e.getValue();
               final Value[] t = new Value[ao.size];
               Iterator it = ao.putItem.iterator();

               while(true) {
                  Stmt stmt;
                  int idx;
                  Local local;
                  label52:
                  do {
                     while(it.hasNext()) {
                        stmt = (Stmt)it.next();
                        if (stmt.st == Stmt.ST.FILL_ARRAY_DATA) {
                           local = (Local)stmt.getOp1();
                           continue label52;
                        }

                        ArrayExpr ae = (ArrayExpr)stmt.getOp1();
                        Local local = (Local)ae.getOp1();
                        if (local == local0) {
                           idx = ((Number)((Constant)ae.getOp2()).value).intValue();
                           Value op2 = stmt.getOp2();
                           if (((Value)op2).vt != Value.VT.LOCAL && ((Value)op2).vt != Value.VT.CONSTANT) {
                              Local n = new Local(-1);
                              method.locals.add(n);
                              method.stmts.insertBefore(stmt, Stmts.nAssign(n, (Value)op2));
                              op2 = n;
                           }

                           t[idx] = (Value)op2;
                        }
                     }

                     method.locals.remove(local0);
                     method.stmts.remove(ao.init);
                     it = ao.putItem.iterator();

                     while(it.hasNext()) {
                        stmt = (Stmt)it.next();
                        method.stmts.remove(stmt);
                     }

                     Cfg.TravelCallBack tcb = new Cfg.TravelCallBack() {
                        public Value onAssign(Local v, AssignStmt as) {
                           return v;
                        }

                        public Value onUse(Local v) {
                           if (local0 == v) {
                              FilledArrayExpr fae = Exprs.nFilledArray(ao.type, t);
                              filledArrayExprs.add(fae);
                              return fae;
                           } else {
                              return v;
                           }
                        }
                     };
                     if (ao.used.size() == 1) {
                        stmt = (Stmt)ao.used.get(0);
                        if (method.stmts.contains(stmt)) {
                           Cfg.travelMod(stmt, tcb, false);
                           continue label71;
                        }

                        int size = filledArrayExprs.size();

                        for(int i = 0; i < size; ++i) {
                           Cfg.travelMod((Value)filledArrayExprs.get(i), tcb);
                        }
                        continue label71;
                     }

                     if (ao.used.size() != 0) {
                        throw new RuntimeException("array is used multiple times");
                     }
                     continue label71;
                  } while(local != local0);

                  Object vs = ((Constant)stmt.getOp2()).value;
                  idx = Array.getLength(vs);

                  for(int j = 0; j < idx; ++j) {
                     t[j] = Exprs.nConstant(Array.get(vs, j));
                  }
               }
            }

            return;
         }
      }
   }

   private void makeSureArrayUsedAfterAllElementAssigned(IrMethod method, final Map<Local, FillArrayTransformer.ArrayObject> arraySizes) {
      Local local;
      for(Iterator var3 = method.locals.iterator(); var3.hasNext(); local._ls_index = -1) {
         local = (Local)var3.next();
      }

      int MAX = true;
      if (arraySizes.size() < 50) {
         this.makeSureArrayUsedAfterAllElementAssigned0(method, arraySizes);
      } else {
         Map<Local, FillArrayTransformer.ArrayObject> keptInAll = new HashMap();
         Map<Local, FillArrayTransformer.ArrayObject> keptInPart = new HashMap();
         List<Local> arrays = new ArrayList(50);
         Iterator it = arraySizes.entrySet().iterator();

         while(it.hasNext()) {
            for(int i = 0; i < 50 && it.hasNext(); ++i) {
               Entry<Local, FillArrayTransformer.ArrayObject> e = (Entry)it.next();
               keptInPart.put(e.getKey(), e.getValue());
               it.remove();
               arrays.add(e.getKey());
            }

            this.makeSureArrayUsedAfterAllElementAssigned0(method, keptInPart);

            Local local;
            for(Iterator var12 = arrays.iterator(); var12.hasNext(); local._ls_index = -1) {
               local = (Local)var12.next();
            }

            arrays.clear();
            keptInAll.putAll(keptInPart);
            keptInPart.clear();
         }

         arraySizes.putAll(keptInAll);
      }

      Cfg.reIndexLocal(method);
   }

   private void makeSureArrayUsedAfterAllElementAssigned0(IrMethod method, final Map<Local, FillArrayTransformer.ArrayObject> arraySizes) {
      final int i = 0;

      Local local;
      for(Iterator var4 = arraySizes.keySet().iterator(); var4.hasNext(); local._ls_index = i++) {
         local = (Local)var4.next();
      }

      final List<FillArrayTransformer.ArrayObjectValue> values = new ArrayList();
      Cfg.dfs(method.stmts, new Cfg.FrameVisitor<FillArrayTransformer.ArrayObjectValue[]>() {
         FillArrayTransformer.ArrayObjectValue[] tmp = this.initFirstFrame((Stmt)null);
         Stmt currentStmt;

         public FillArrayTransformer.ArrayObjectValue[] merge(FillArrayTransformer.ArrayObjectValue[] srcFrame, FillArrayTransformer.ArrayObjectValue[] distFrame, Stmt src, Stmt dist) {
            int ix;
            FillArrayTransformer.ArrayObjectValue arc;
            FillArrayTransformer.ArrayObjectValue aov;
            if (distFrame == null) {
               distFrame = new FillArrayTransformer.ArrayObjectValue[i];

               for(ix = 0; ix < i; ++ix) {
                  arc = srcFrame[ix];
                  if (arc != null) {
                     aov = new FillArrayTransformer.ArrayObjectValue(arc.local);
                     values.add(aov);
                     aov.array = arc.array;
                     aov.parent = arc;
                     aov.pos = (BitSet)arc.pos.clone();
                     distFrame[ix] = aov;
                  }
               }
            } else {
               for(ix = 0; ix < i; ++ix) {
                  arc = srcFrame[ix];
                  aov = distFrame[ix];
                  if (arc != null && aov != null) {
                     if (aov.otherParent == null) {
                        aov.otherParent = new HashSet();
                     }

                     aov.otherParent.add(arc);
                  }
               }
            }

            return distFrame;
         }

         public FillArrayTransformer.ArrayObjectValue[] initFirstFrame(Stmt first) {
            return new FillArrayTransformer.ArrayObjectValue[i];
         }

         public FillArrayTransformer.ArrayObjectValue[] exec(FillArrayTransformer.ArrayObjectValue[] frame, Stmt stmt) {
            this.currentStmt = stmt;
            System.arraycopy(frame, 0, this.tmp, 0, i);
            Local localx;
            FillArrayTransformer.ArrayObjectValue aov;
            if (stmt.st == Stmt.ST.FILL_ARRAY_DATA) {
               if (stmt.getOp1().vt == Value.VT.LOCAL) {
                  localx = (Local)stmt.getOp1();
                  if (localx._ls_index >= 0) {
                     aov = this.tmp[localx._ls_index];
                     Constant cst = (Constant)stmt.getOp2();
                     int endPos = Array.getLength(cst.value);
                     aov.pos.set(0, endPos);
                  }
               } else {
                  this.use(stmt.getOp1());
               }
            } else if (stmt.st == Stmt.ST.ASSIGN && stmt.getOp1().vt == Value.VT.ARRAY) {
               this.use(stmt.getOp2());
               ArrayExpr ae = (ArrayExpr)stmt.getOp1();
               if (ae.getOp1().vt == Value.VT.LOCAL) {
                  Local local = (Local)ae.getOp1();
                  if (local._ls_index >= 0) {
                     int index = ((Number)((Constant)ae.getOp2()).value).intValue();
                     FillArrayTransformer.ArrayObjectValue av = this.tmp[local._ls_index];
                     av.pos.set(index);
                  } else {
                     this.use(ae);
                  }
               } else {
                  this.use(ae);
               }
            } else if (stmt.st == Stmt.ST.ASSIGN && stmt.getOp1().vt == Value.VT.LOCAL) {
               localx = (Local)stmt.getOp1();
               this.use(stmt.getOp2());
               if (localx._ls_index >= 0) {
                  aov = new FillArrayTransformer.ArrayObjectValue(localx);
                  aov.array = (FillArrayTransformer.ArrayObject)arraySizes.get(localx);
                  aov.pos = new BitSet();
                  values.add(aov);
                  this.tmp[localx._ls_index] = aov;
               }
            } else {
               switch(stmt.et) {
               case E0:
               default:
                  break;
               case E1:
                  this.use(stmt.getOp());
                  break;
               case E2:
                  this.use(stmt.getOp1());
                  this.use(stmt.getOp2());
                  break;
               case En:
                  throw new RuntimeException();
               }
            }

            return this.tmp;
         }

         private void use(Value v) {
            switch(v.et) {
            case E0:
               if (v.vt == Value.VT.LOCAL) {
                  Local local = (Local)v;
                  if (local._ls_index >= 0) {
                     FillArrayTransformer.ArrayObjectValue aov = this.tmp[local._ls_index];
                     aov.array.used.add(this.currentStmt);
                     aov.used = true;
                  }
               }
               break;
            case E1:
               this.use(v.getOp());
               break;
            case E2:
               this.use(v.getOp1());
               this.use(v.getOp2());
               break;
            case En:
               Value[] var2 = v.getOps();
               int var3 = var2.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                  Value op = var2[var4];
                  this.use(op);
               }
            }

         }
      });
      Set<FillArrayTransformer.ArrayObjectValue> used = this.markUsed(values);
      Iterator it = used.iterator();

      while(true) {
         while(it.hasNext()) {
            FillArrayTransformer.ArrayObjectValue avo = (FillArrayTransformer.ArrayObjectValue)it.next();
            if (avo.array.used.size() > 1) {
               arraySizes.remove(avo.local);
            } else if (avo.parent != null && avo.otherParent != null) {
               BitSet p = avo.parent.pos;
               Iterator var10 = avo.otherParent.iterator();

               while(var10.hasNext()) {
                  FillArrayTransformer.ArrayObjectValue ps = (FillArrayTransformer.ArrayObjectValue)var10.next();
                  if (!p.equals(ps.pos)) {
                     arraySizes.remove(avo.local);
                     break;
                  }
               }
            }
         }

         it = arraySizes.entrySet().iterator();

         while(true) {
            while(it.hasNext()) {
               Entry<Local, FillArrayTransformer.ArrayObject> e = (Entry)it.next();
               Local local = (Local)e.getKey();
               FillArrayTransformer.ArrayObject arrayObject = (FillArrayTransformer.ArrayObject)e.getValue();
               Iterator var21 = arrayObject.used.iterator();

               while(var21.hasNext()) {
                  Stmt use = (Stmt)var21.next();
                  FillArrayTransformer.ArrayObjectValue[] frame = (FillArrayTransformer.ArrayObjectValue[])((FillArrayTransformer.ArrayObjectValue[])use.frame);
                  FillArrayTransformer.ArrayObjectValue aov = frame[local._ls_index];
                  BitSet pos = aov.pos;
                  if (pos.nextClearBit(0) < arrayObject.size || pos.nextSetBit(arrayObject.size) >= 0) {
                     it.remove();
                     break;
                  }
               }
            }

            Stmt stmt;
            for(it = method.stmts.iterator(); it.hasNext(); stmt.frame = null) {
               stmt = (Stmt)it.next();
            }

            return;
         }
      }
   }

   protected Set<FillArrayTransformer.ArrayObjectValue> markUsed(Collection<FillArrayTransformer.ArrayObjectValue> values) {
      Set<FillArrayTransformer.ArrayObjectValue> used = new HashSet(values.size() / 2);
      Queue<FillArrayTransformer.ArrayObjectValue> q = new UniqueQueue();
      q.addAll(values);
      values.clear();

      while(true) {
         FillArrayTransformer.ArrayObjectValue v;
         do {
            do {
               do {
                  if (q.isEmpty()) {
                     return used;
                  }

                  v = (FillArrayTransformer.ArrayObjectValue)q.poll();
               } while(!v.used);
            } while(used.contains(v));

            used.add(v);
            FillArrayTransformer.ArrayObjectValue p = v.parent;
            if (p != null && !p.used) {
               p.used = true;
               q.add(p);
            }
         } while(v.otherParent == null);

         Iterator var7 = v.otherParent.iterator();

         while(var7.hasNext()) {
            FillArrayTransformer.ArrayObjectValue p = (FillArrayTransformer.ArrayObjectValue)var7.next();
            if (!p.used) {
               p.used = true;
               q.add(p);
            }
         }
      }
   }

   private void makeSureAllElementAreAssigned(Map<Local, FillArrayTransformer.ArrayObject> arraySizes) {
      BitSet pos = new BitSet();

      for(Iterator it = arraySizes.entrySet().iterator(); it.hasNext(); pos.clear()) {
         Entry<Local, FillArrayTransformer.ArrayObject> e = (Entry)it.next();
         FillArrayTransformer.ArrayObject arrayObject = (FillArrayTransformer.ArrayObject)e.getValue();
         boolean needRemove = false;
         Iterator var7 = arrayObject.putItem.iterator();

         while(var7.hasNext()) {
            Stmt p = (Stmt)var7.next();
            int idx;
            if (p.st == Stmt.ST.FILL_ARRAY_DATA) {
               int endPos = Array.getLength(((Constant)p.getOp2()).value);
               idx = pos.nextSetBit(0);
               if (idx >= 0 && idx < endPos) {
                  needRemove = true;
                  break;
               }

               pos.set(0, endPos);
            } else {
               ArrayExpr ae = (ArrayExpr)p.getOp1();
               idx = ((Number)((Constant)ae.getOp2()).value).intValue();
               if (pos.get(idx)) {
                  needRemove = true;
                  break;
               }

               pos.set(idx);
            }
         }

         if (needRemove || pos.nextClearBit(0) < arrayObject.size || pos.nextSetBit(arrayObject.size) >= 0) {
            it.remove();
         }
      }

   }

   private Map<Local, FillArrayTransformer.ArrayObject> searchForArrayObject(IrMethod method) {
      final Map<Local, FillArrayTransformer.ArrayObject> arraySizes = new HashMap();
      if (method.locals.size() == 0) {
         return arraySizes;
      } else {
         Cfg.createCFG(method);
         Cfg.dfsVisit(method, new Cfg.DfsVisitor() {
            public void onVisit(Stmt p) {
               if (p.st == Stmt.ST.ASSIGN) {
                  if (p.getOp2().vt == Value.VT.NEW_ARRAY && p.getOp1().vt == Value.VT.LOCAL) {
                     TypeExpr aex = (TypeExpr)p.getOp2();
                     if (aex.getOp().vt == Value.VT.CONSTANT) {
                        int size = ((Number)((Constant)aex.getOp()).value).intValue();
                        if (size >= 0) {
                           arraySizes.put((Local)p.getOp1(), new FillArrayTransformer.ArrayObject(size, aex.type, (AssignStmt)p));
                        }
                     }
                  } else if (p.getOp1().vt == Value.VT.ARRAY) {
                     ArrayExpr ae = (ArrayExpr)p.getOp1();
                     if (ae.getOp1().vt == Value.VT.LOCAL) {
                        Local localx = (Local)ae.getOp1();
                        FillArrayTransformer.ArrayObject arrayObjectx = (FillArrayTransformer.ArrayObject)arraySizes.get(localx);
                        if (arrayObjectx != null) {
                           if (ae.getOp2().vt == Value.VT.CONSTANT) {
                              arrayObjectx.putItem.add(p);
                           } else {
                              arraySizes.remove(localx);
                           }
                        }
                     }
                  }
               } else if (p.st == Stmt.ST.FILL_ARRAY_DATA && p.getOp1().vt == Value.VT.LOCAL) {
                  Local local = (Local)p.getOp1();
                  FillArrayTransformer.ArrayObject arrayObject = (FillArrayTransformer.ArrayObject)arraySizes.get(local);
                  if (arrayObject != null) {
                     arrayObject.putItem.add(p);
                  }
               }

            }
         });
         if (arraySizes.size() > 0) {
            Set<Local> set = new HashSet();
            Iterator var4;
            if (method.phiLabels != null) {
               var4 = method.phiLabels.iterator();

               label49:
               while(true) {
                  LabelStmt labelStmt;
                  do {
                     if (!var4.hasNext()) {
                        break label49;
                     }

                     labelStmt = (LabelStmt)var4.next();
                  } while(labelStmt.phis == null);

                  Iterator var6 = labelStmt.phis.iterator();

                  while(var6.hasNext()) {
                     AssignStmt as = (AssignStmt)var6.next();
                     set.add((Local)as.getOp1());
                     Value[] var8 = as.getOp2().getOps();
                     int var9 = var8.length;

                     for(int var10 = 0; var10 < var9; ++var10) {
                        Value v = var8[var10];
                        set.add((Local)v);
                     }
                  }
               }
            }

            if (set.size() > 0) {
               var4 = set.iterator();

               while(var4.hasNext()) {
                  Local local = (Local)var4.next();
                  arraySizes.remove(local);
               }
            }
         }

         return arraySizes;
      }
   }

   static class ArrayObjectValue {
      BitSet pos;
      Local local;
      FillArrayTransformer.ArrayObject array;
      FillArrayTransformer.ArrayObjectValue parent;
      Set<FillArrayTransformer.ArrayObjectValue> otherParent;
      boolean used;

      public ArrayObjectValue(Local local) {
         this.local = local;
      }
   }

   private static class ArrayObject {
      int size;
      String type;
      AssignStmt init;
      List<Stmt> putItem;
      List<Stmt> used;

      private ArrayObject(int size, String type, AssignStmt init) {
         this.putItem = new ArrayList();
         this.used = new ArrayList();
         this.size = size;
         this.type = type;
         this.init = init;
      }

      // $FF: synthetic method
      ArrayObject(int x0, String x1, AssignStmt x2, Object x3) {
         this(x0, x1, x2);
      }
   }
}
