package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.ET;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.NewExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class NewTransformer implements Transformer {
   static NewTransformer.Vx IGNORED = new NewTransformer.Vx((NewTransformer.TObject)null, true);

   public void transform(IrMethod method) {
      this.replaceX(method);
      this.replaceAST(method);
   }

   void replaceX(IrMethod method) {
      Map<Local, NewTransformer.TObject> init = new HashMap();
      Iterator var3 = method.stmts.iterator();

      while(var3.hasNext()) {
         Stmt p = (Stmt)var3.next();
         if (p.st == Stmt.ST.ASSIGN && p.getOp1().vt == Value.VT.LOCAL && p.getOp2().vt == Value.VT.NEW) {
            Local local = (Local)p.getOp1();
            init.put(local, new NewTransformer.TObject(local, (AssignStmt)p));
         }
      }

      if (init.size() > 0) {
         int size = Cfg.reIndexLocal(method);
         this.makeSureUsedBeforeConstructor(method, init, size);
         if (init.size() > 0) {
            this.replace0(method, init, size);
         }

         Stmt stmt;
         for(Iterator var7 = method.stmts.iterator(); var7.hasNext(); stmt.frame = null) {
            stmt = (Stmt)var7.next();
         }
      }

   }

   void replaceAST(IrMethod method) {
      Iterator it = method.stmts.iterator();

      while(it.hasNext()) {
         Stmt p = (Stmt)it.next();
         InvokeExpr ie = this.findInvokeExpr(p, (InvokeExpr)null);
         if (ie != null && "<init>".equals(ie.getName()) && "V".equals(ie.getRet())) {
            Value[] orgOps = ie.getOps();
            if (orgOps[0].vt == Value.VT.NEW) {
               NewExpr newExpr = (NewExpr)ie.getOps()[0];
               if (newExpr != null) {
                  Value[] nOps = (Value[])Arrays.copyOfRange(orgOps, 1, orgOps.length);
                  InvokeExpr invokeNew = Exprs.nInvokeNew(nOps, ie.getArgs(), ie.getOwner());
                  method.stmts.insertBefore(p, Stmts.nVoidInvoke(invokeNew));
                  it.remove();
               }
            }
         }
      }

   }

   void replace0(IrMethod method, Map<Local, NewTransformer.TObject> init, int size) {
      Set<Local> toDelete = new HashSet();
      Local[] locals = new Local[size];

      Iterator it;
      Local local;
      for(it = method.locals.iterator(); it.hasNext(); locals[local._ls_index] = local) {
         local = (Local)it.next();
      }

      it = init.values().iterator();

      NewTransformer.Vx[] frame;
      int i;
      NewTransformer.Vx s;
      NewTransformer.TObject obj;
      while(it.hasNext()) {
         obj = (NewTransformer.TObject)it.next();
         frame = (NewTransformer.Vx[])((NewTransformer.Vx[])obj.invokeStmt.frame);

         for(i = 0; i < frame.length; ++i) {
            s = frame[i];
            if (s != null && s.obj == obj) {
               toDelete.add(locals[i]);
            }
         }
      }

      it = method.stmts.iterator();

      while(it.hasNext()) {
         Stmt p = (Stmt)it.next();
         if (p.st == Stmt.ST.ASSIGN && p.getOp1().vt == Value.VT.LOCAL && toDelete.contains((Local)p.getOp1())) {
            it.remove();
         }
      }

      it = init.values().iterator();

      while(it.hasNext()) {
         obj = (NewTransformer.TObject)it.next();
         frame = (NewTransformer.Vx[])((NewTransformer.Vx[])obj.invokeStmt.frame);

         for(i = 0; i < frame.length; ++i) {
            s = frame[i];
            if (s != null && s.obj == obj) {
               Local b = locals[i];
               if (b != obj.local) {
                  method.stmts.insertAfter(obj.invokeStmt, Stmts.nAssign(b, obj.local));
               }
            }
         }

         InvokeExpr ie = this.findInvokeExpr(obj.invokeStmt, (InvokeExpr)null);
         Value[] orgOps = ie.getOps();
         Value[] nOps = (Value[])Arrays.copyOfRange(orgOps, 1, orgOps.length);
         InvokeExpr invokeNew = Exprs.nInvokeNew(nOps, ie.getArgs(), ie.getOwner());
         method.stmts.replace(obj.invokeStmt, Stmts.nAssign(obj.local, invokeNew));
      }

   }

   void makeSureUsedBeforeConstructor(IrMethod method, final Map<Local, NewTransformer.TObject> init, final int size) {
      Cfg.createCFG(method);
      Cfg.dfs(method.stmts, new Cfg.FrameVisitor<NewTransformer.Vx[]>() {
         boolean keepFrame = false;
         NewTransformer.Vx[] tmp = new NewTransformer.Vx[size];
         StmtTraveler stmtTraveler = new StmtTraveler() {
            Stmt current;

            public Stmt travel(Stmt stmt) {
               this.current = stmt;
               if (stmt.et == ET.E2 && stmt.getOp1().vt == Value.VT.LOCAL) {
                  Local op1 = (Local)stmt.getOp1();
                  if (stmt.getOp2().vt == Value.VT.LOCAL) {
                     Local op2 = (Local)stmt.getOp2();
                     tmp[op1._ls_index] = tmp[op2._ls_index];
                     return stmt;
                  } else if (stmt.getOp2().vt == Value.VT.NEW) {
                     tmp[op1._ls_index] = new NewTransformer.Vx((NewTransformer.TObject)init.get(op1), false);
                     return stmt;
                  } else {
                     this.travel(stmt.getOp2());
                     tmp[op1._ls_index] = NewTransformer.IGNORED;
                     return stmt;
                  }
               } else if (stmt.st != Stmt.ST.LABEL) {
                  return super.travel(stmt);
               } else {
                  LabelStmt labelStmt = (LabelStmt)stmt;
                  Local local;
                  if (labelStmt.phis != null) {
                     for(Iterator var3 = labelStmt.phis.iterator(); var3.hasNext(); tmp[local._ls_index] = NewTransformer.IGNORED) {
                        AssignStmt phi = (AssignStmt)var3.next();
                        local = (Local)phi.getOp1();
                     }
                  }

                  return stmt;
               }
            }

            public Value travel(Value op) {
               if (op.vt == Value.VT.INVOKE_SPECIAL && op.getOps().length >= 1) {
                  InvokeExpr ie = (InvokeExpr)op;
                  if ("<init>".equals(ie.getName())) {
                     Value thiz = op.getOps()[0];
                     if (thiz.vt == Value.VT.LOCAL) {
                        Local local = (Local)thiz;
                        NewTransformer.Vx vx = tmp[local._ls_index];
                        NewTransformer.TObject object = vx.obj;
                        if (object != null) {
                           if (object.invokeStmt != null) {
                              object.useBeforeInit = true;
                           } else {
                              vx.init = true;
                              object.invokeStmt = this.current;

                              for(int i = 0; i < tmp.length; ++i) {
                                 NewTransformer.Vx s = tmp[i];
                                 if (s != null && s.obj == object) {
                                    tmp[i] = NewTransformer.IGNORED;
                                 }
                              }

                              keepFrame = true;
                           }
                        }
                     }
                  }
               }

               op = super.travel(op);
               if (op.vt == Value.VT.LOCAL) {
                  use((Local)op);
               }

               return op;
            }
         };

         public NewTransformer.Vx[] merge(NewTransformer.Vx[] srcFrame, NewTransformer.Vx[] distFrame, Stmt src, Stmt dist) {
            if (distFrame == null) {
               distFrame = new NewTransformer.Vx[size];
               System.arraycopy(srcFrame, 0, distFrame, 0, size);
            } else {
               for(int ix = 0; ix < size; ++ix) {
                  NewTransformer.Vx sx = srcFrame[ix];
                  NewTransformer.Vx dx = distFrame[ix];
                  if (sx != null) {
                     if (dx == null) {
                        distFrame[ix] = sx;
                     } else if (sx != dx) {
                        NewTransformer.TObject objx = sx.obj;
                        if (objx != null) {
                           objx.useBeforeInit = true;
                        }

                        objx = dx.obj;
                        if (objx != null) {
                           objx.useBeforeInit = true;
                        }
                     }
                  }
               }
            }

            if (dist.st == Stmt.ST.LABEL) {
               List<AssignStmt> phis = ((LabelStmt)dist).phis;
               if (phis != null && phis.size() > 0) {
                  Iterator var18 = phis.iterator();

                  while(var18.hasNext()) {
                     AssignStmt phi = (AssignStmt)var18.next();
                     Value[] var20 = phi.getOp2().getOps();
                     int var9 = var20.length;

                     for(int var10 = 0; var10 < var9; ++var10) {
                        Value value = var20[var10];
                        Local local = (Local)value;
                        int i = local._ls_index;
                        NewTransformer.Vx s = srcFrame[i];
                        NewTransformer.Vx d = distFrame[i];
                        NewTransformer.TObject obj;
                        if (d != null) {
                           if (!d.init) {
                              obj = d.obj;
                              if (obj != null) {
                                 obj.useBeforeInit = true;
                              }
                           }
                        } else if (s != null && !s.init) {
                           obj = s.obj;
                           if (obj != null) {
                              obj.useBeforeInit = true;
                           }
                        }
                     }
                  }
               }
            }

            return distFrame;
         }

         public NewTransformer.Vx[] initFirstFrame(Stmt first) {
            return new NewTransformer.Vx[size];
         }

         public NewTransformer.Vx[] exec(NewTransformer.Vx[] frame, Stmt stmt) {
            this.keepFrame = false;
            System.arraycopy(frame, 0, this.tmp, 0, size);
            this.stmtTraveler.travel(stmt);
            if (stmt._cfg_froms.size() > 1) {
               this.keepFrame = true;
            }

            if (!this.keepFrame) {
               stmt.frame = null;
            }

            return this.tmp;
         }

         void use(Local local) {
            NewTransformer.Vx vx = this.tmp[local._ls_index];
            if (!vx.init) {
               NewTransformer.TObject object = vx.obj;
               if (object != null) {
                  object.useBeforeInit = true;
               }

               this.tmp[local._ls_index] = NewTransformer.IGNORED;
            }

         }
      });
      Iterator iterator = init.entrySet().iterator();

      while(iterator.hasNext()) {
         Entry<Local, NewTransformer.TObject> e = (Entry)iterator.next();
         boolean keep = true;
         NewTransformer.TObject obj = (NewTransformer.TObject)e.getValue();
         if (obj.useBeforeInit) {
            keep = false;
         }

         if (obj.invokeStmt == null) {
            keep = false;
         }

         if (!keep) {
            iterator.remove();
         }
      }

   }

   InvokeExpr findInvokeExpr(Stmt p, InvokeExpr ie) {
      if (p.st == Stmt.ST.ASSIGN) {
         if (p.getOp2().vt == Value.VT.INVOKE_SPECIAL) {
            ie = (InvokeExpr)p.getOp2();
         }
      } else if (p.st == Stmt.ST.VOID_INVOKE) {
         ie = (InvokeExpr)p.getOp();
      }

      return ie;
   }

   static class Vx {
      boolean init;
      NewTransformer.TObject obj;

      public Vx(NewTransformer.TObject obj, boolean init) {
         this.obj = obj;
         this.init = init;
      }
   }

   static class TObject {
      public Stmt invokeStmt;
      Local local;
      boolean useBeforeInit;
      private AssignStmt init;

      TObject(Local local, AssignStmt init) {
         this.local = local;
         this.init = init;
      }
   }
}
