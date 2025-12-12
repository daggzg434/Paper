package com.googlecode.dex2jar.ir.ts.array;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Cfg;
import com.googlecode.dex2jar.ir.ts.StatedTransformer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArrayElementTransformer extends StatedTransformer {
   public boolean transformReportChanged(IrMethod method) {
      Set<Local> arrays = this.searchForArrayObject(method);
      if (arrays.size() == 0) {
         return false;
      } else {
         Local local;
         for(Iterator var3 = method.locals.iterator(); var3.hasNext(); local._ls_index = -1) {
            local = (Local)var3.next();
         }

         final int i = 0;

         Local local;
         for(Iterator var10 = arrays.iterator(); var10.hasNext(); local._ls_index = i++) {
            local = (Local)var10.next();
         }

         Cfg.createCFG(method);
         final List<ArrayElementTransformer.ArrayValue> values = new ArrayList();
         final List<Stmt> used = new ArrayList();
         Cfg.dfs(method.stmts, new Cfg.FrameVisitor<ArrayElementTransformer.ArrayValue[]>() {
            Set<Integer> phis = new HashSet();
            ArrayElementTransformer.ArrayValue[] tmp = new ArrayElementTransformer.ArrayValue[i];
            Stmt currentStmt;

            public ArrayElementTransformer.ArrayValue[] merge(ArrayElementTransformer.ArrayValue[] srcFrame, ArrayElementTransformer.ArrayValue[] distFrame, Stmt src, Stmt dist) {
               if (dist.st == Stmt.ST.LABEL) {
                  LabelStmt labelStmt = (LabelStmt)dist;
                  if (labelStmt.phis != null) {
                     Iterator var6 = labelStmt.phis.iterator();

                     while(var6.hasNext()) {
                        AssignStmt phi = (AssignStmt)var6.next();
                        int idx = ((Local)phi.getOp1())._ls_index;
                        if (idx >= 0) {
                           this.phis.add(idx);
                        }
                     }
                  }
               }

               int ix;
               ArrayElementTransformer.ArrayValue arc;
               ArrayElementTransformer.ArrayValue aov;
               if (distFrame == null) {
                  distFrame = new ArrayElementTransformer.ArrayValue[i];

                  for(ix = 0; ix < i; ++ix) {
                     if (this.phis.contains(ix)) {
                        arc = new ArrayElementTransformer.ArrayValue();
                        values.add(arc);
                        arc.s = ArrayElementTransformer.ArrayValue.S.UNKNOWN;
                        arc.indexType = ArrayElementTransformer.ArrayValue.IndexType.NONE;
                        arc.stmt = dist;
                        distFrame[ix] = arc;
                     } else {
                        arc = srcFrame[ix];
                        if (arc != null) {
                           aov = new ArrayElementTransformer.ArrayValue();
                           values.add(aov);
                           aov.s = ArrayElementTransformer.ArrayValue.S.INHERIT;
                           aov.indexType = ArrayElementTransformer.ArrayValue.IndexType.NONE;
                           aov.stmt = dist;
                           aov.parent = arc;
                           distFrame[ix] = aov;
                        }
                     }
                  }
               } else {
                  for(ix = 0; ix < i; ++ix) {
                     if (!this.phis.contains(ix)) {
                        arc = srcFrame[ix];
                        aov = distFrame[ix];
                        if (arc != null && aov != null) {
                           if (aov.parent == null) {
                              aov.parent = arc;
                           } else if (!aov.parent.equals(arc)) {
                              if (aov.otherParents == null) {
                                 aov.otherParents = new HashSet();
                              }

                              aov.otherParents.add(arc);
                           }
                        }
                     }
                  }
               }

               this.phis.clear();
               return distFrame;
            }

            public ArrayElementTransformer.ArrayValue[] initFirstFrame(Stmt first) {
               return new ArrayElementTransformer.ArrayValue[i];
            }

            public ArrayElementTransformer.ArrayValue[] exec(ArrayElementTransformer.ArrayValue[] frame, Stmt stmt) {
               this.currentStmt = stmt;
               System.arraycopy(frame, 0, this.tmp, 0, i);
               Local local;
               ArrayElementTransformer.ArrayValue avx;
               int ix;
               ArrayElementTransformer.ArrayValue av;
               int size;
               if (stmt.st == Stmt.ST.ASSIGN) {
                  if (stmt.getOp1().vt == Value.VT.LOCAL) {
                     local = (Local)stmt.getOp1();
                     this.use(stmt.getOp2());
                     if (local._ls_index >= 0) {
                        Value op2 = stmt.getOp2();
                        if (op2.vt == Value.VT.NEW_ARRAY) {
                           avx = new ArrayElementTransformer.ArrayValue();
                           avx.s = ArrayElementTransformer.ArrayValue.S.DEFAULT;
                           avx.size = op2.getOp();
                           values.add(avx);
                           this.tmp[local._ls_index] = avx;
                        } else if (op2.vt == Value.VT.FILLED_ARRAY) {
                           avx = new ArrayElementTransformer.ArrayValue();
                           avx.s = ArrayElementTransformer.ArrayValue.S.DEFAULT;
                           avx.indexType = ArrayElementTransformer.ArrayValue.IndexType.CONST;
                           avx.stmt = stmt;
                           FilledArrayExpr fae = (FilledArrayExpr)stmt.getOp2();
                           avx.size = Exprs.nInt(fae.getOps().length);
                           Value[] ops = fae.getOps();

                           for(ix = 0; ix < ops.length; ++ix) {
                              avx.elements1.put(ix, ops[ix]);
                           }

                           values.add(avx);
                           this.tmp[local._ls_index] = avx;
                        } else if (op2.vt == Value.VT.CONSTANT) {
                           Object cst = ((Constant)op2).value;
                           if (cst != null && !cst.equals(Constant.Null) && cst.getClass().isArray()) {
                              av = new ArrayElementTransformer.ArrayValue();
                              av.s = ArrayElementTransformer.ArrayValue.S.DEFAULT;
                              av.indexType = ArrayElementTransformer.ArrayValue.IndexType.CONST;
                              av.stmt = stmt;
                              size = Array.getLength(cst);
                              av.size = Exprs.nInt(size);

                              for(ix = 0; ix < size; ++ix) {
                                 av.elements1.put(ix, Exprs.nConstant(Array.get(cst, size)));
                              }

                              values.add(av);
                              this.tmp[local._ls_index] = av;
                           } else {
                              av = new ArrayElementTransformer.ArrayValue();
                              values.add(av);
                              av.s = ArrayElementTransformer.ArrayValue.S.UNKNOWN;
                              av.indexType = ArrayElementTransformer.ArrayValue.IndexType.NONE;
                              av.stmt = stmt;
                              this.tmp[local._ls_index] = av;
                           }
                        } else {
                           avx = new ArrayElementTransformer.ArrayValue();
                           values.add(avx);
                           avx.s = ArrayElementTransformer.ArrayValue.S.UNKNOWN;
                           avx.indexType = ArrayElementTransformer.ArrayValue.IndexType.NONE;
                           avx.stmt = stmt;
                           this.tmp[local._ls_index] = avx;
                        }
                     }
                  } else if (stmt.getOp1().vt == Value.VT.ARRAY) {
                     this.use(stmt.getOp2());
                     ArrayExpr ae = (ArrayExpr)stmt.getOp1();
                     if (ae.getOp1().vt == Value.VT.LOCAL) {
                        Local localx = (Local)ae.getOp1();
                        Value index = ae.getOp2();
                        if (localx._ls_index >= 0) {
                           ArrayElementTransformer.ArrayValue avxx;
                           if (index.vt == Value.VT.CONSTANT) {
                              av = this.tmp[localx._ls_index];
                              avxx = new ArrayElementTransformer.ArrayValue();
                              values.add(avxx);
                              avxx.parent = av;
                              avxx.elements1.put(((Number)((Number)((Constant)index).value)).intValue(), stmt.getOp2());
                              avxx.indexType = ArrayElementTransformer.ArrayValue.IndexType.CONST;
                              avxx.s = ArrayElementTransformer.ArrayValue.S.INHERIT;
                              avxx.stmt = stmt;
                              this.tmp[localx._ls_index] = avxx;
                           } else if (index.vt == Value.VT.LOCAL) {
                              av = this.tmp[localx._ls_index];
                              avxx = new ArrayElementTransformer.ArrayValue();
                              values.add(avxx);
                              avxx.parent = av;
                              avxx.elements1.put(index, stmt.getOp2());
                              avxx.indexType = ArrayElementTransformer.ArrayValue.IndexType.LOCAL;
                              avxx.s = ArrayElementTransformer.ArrayValue.S.INHERIT;
                              avxx.stmt = stmt;
                              this.tmp[localx._ls_index] = avxx;
                           } else {
                              av = new ArrayElementTransformer.ArrayValue();
                              values.add(av);
                              av.s = ArrayElementTransformer.ArrayValue.S.UNKNOWN;
                              av.indexType = ArrayElementTransformer.ArrayValue.IndexType.NONE;
                              av.stmt = stmt;
                              this.tmp[localx._ls_index] = av;
                           }
                        } else {
                           this.use(stmt.getOp1());
                        }
                     } else {
                        this.use(stmt.getOp1());
                     }
                  } else {
                     this.use(stmt.getOp1());
                     this.use(stmt.getOp2());
                  }
               } else if (stmt.st == Stmt.ST.FILL_ARRAY_DATA) {
                  if (stmt.getOp1().vt == Value.VT.LOCAL) {
                     local = (Local)stmt.getOp1();
                     if (local._ls_index >= 0) {
                        Object array = ((Constant)stmt.getOp2()).value;
                        avx = this.tmp[local._ls_index];
                        av = new ArrayElementTransformer.ArrayValue();
                        values.add(av);
                        av.parent = avx;
                        size = Array.getLength(array);
                        av.size = Exprs.nInt(size);

                        for(ix = 0; ix < size; ++ix) {
                           av.elements1.put(ix, Exprs.nConstant(Array.get(array, ix)));
                        }

                        av.indexType = ArrayElementTransformer.ArrayValue.IndexType.CONST;
                        av.s = ArrayElementTransformer.ArrayValue.S.INHERIT;
                        av.stmt = stmt;
                        this.tmp[local._ls_index] = av;
                     }
                  } else {
                     this.use(stmt.getOp1());
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
               default:
                  break;
               case E1:
                  this.use(v.getOp());
                  break;
               case E2:
                  Value op1 = v.getOp1();
                  Value op2 = v.getOp2();
                  this.use(op1);
                  this.use(op2);
                  if (v.vt == Value.VT.ARRAY && op1.vt == Value.VT.LOCAL && (op2.vt == Value.VT.LOCAL || op2.vt == Value.VT.CONSTANT)) {
                     Local local = (Local)op1;
                     if (local._ls_index > 0) {
                        used.add(this.currentStmt);
                     }
                  }
                  break;
               case En:
                  Value[] var4 = v.getOps();
                  int var5 = var4.length;

                  for(int var6 = 0; var6 < var5; ++var6) {
                     Value op = var4[var6];
                     this.use(op);
                  }
               }

            }
         });

         Stmt var8;
         for(Iterator var7 = method.stmts.iterator(); var7.hasNext(); var8 = (Stmt)var7.next()) {
         }

         (new StmtTraveler() {
            public Value travel(Value op) {
               op = super.travel(op);
               if (op.vt == Value.VT.ARRAY) {
               }

               return op;
            }
         }).travel(method.stmts);
         return false;
      }
   }

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
      (new ArrayElementTransformer()).transform(m);
   }

   private Set<Local> searchForArrayObject(IrMethod method) {
      Set<Local> arrays = new HashSet();
      Iterator var3 = method.stmts.iterator();

      while(true) {
         Stmt stmt;
         Local local;
         label41:
         do {
            while(true) {
               while(var3.hasNext()) {
                  stmt = (Stmt)var3.next();
                  if (stmt.st == Stmt.ST.ASSIGN) {
                     if (stmt.getOp1().vt == Value.VT.LOCAL) {
                        local = (Local)stmt.getOp1();
                        continue label41;
                     }

                     if (stmt.getOp1().vt == Value.VT.ARRAY) {
                        ArrayExpr ae = (ArrayExpr)stmt.getOp1();
                        if (ae.getOp1().vt == Value.VT.LOCAL) {
                           Local local = (Local)ae.getOp1();
                           arrays.add(local);
                        }
                     }
                  } else if (stmt.st == Stmt.ST.FILL_ARRAY_DATA && stmt.getOp1().vt == Value.VT.LOCAL) {
                     local = (Local)stmt.getOp1();
                     arrays.add(local);
                  }
               }

               return arrays;
            }
         } while(stmt.getOp2().vt != Value.VT.NEW_ARRAY && stmt.getOp2().vt != Value.VT.FILLED_ARRAY);

         arrays.add(local);
      }
   }

   static class ArrayValue {
      ArrayElementTransformer.ArrayValue.IndexType indexType;
      ArrayElementTransformer.ArrayValue.S s;
      ArrayElementTransformer.ArrayValue parent;
      Value size;
      Set<ArrayElementTransformer.ArrayValue> otherParents;
      Map<Object, Value> elements1;
      Stmt stmt;

      ArrayValue() {
         this.indexType = ArrayElementTransformer.ArrayValue.IndexType.NONE;
         this.s = ArrayElementTransformer.ArrayValue.S.INHERIT;
         this.elements1 = new HashMap();
      }

      static enum IndexType {
         CONST,
         LOCAL,
         NONE;
      }

      static enum S {
         DEFAULT,
         UNKNOWN,
         INHERIT;
      }
   }
}
