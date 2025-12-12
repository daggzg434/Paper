package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtSearcher;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;

public class NpeTransformer extends StatedTransformer {
   private static final NpeTransformer.MustThrowException NPE = new NpeTransformer.MustThrowException();
   private static final NpeTransformer.MustThrowException DIVE = new NpeTransformer.MustThrowException();
   private static final NpeTransformer.MustThrowException NEGATIVE_ARRAY_SIZE = new NpeTransformer.MustThrowException();

   public boolean transformReportChanged(IrMethod method) {
      boolean changed = false;
      if (method.locals.size() == 0) {
         return false;
      } else {
         StmtSearcher st = new StmtSearcher() {
            public void travel(Stmt stmt) {
               if (stmt.st == Stmt.ST.FILL_ARRAY_DATA && NpeTransformer.isNull(stmt.getOp1())) {
                  throw NpeTransformer.NPE;
               } else {
                  super.travel(stmt);
               }
            }

            public void travel(Value op) {
               Constant constantx;
               switch(op.vt) {
               case INVOKE_VIRTUAL:
               case INVOKE_SPECIAL:
               case INVOKE_INTERFACE:
                  if (NpeTransformer.isNull(op.getOps()[0])) {
                     throw NpeTransformer.NPE;
                  }
                  break;
               case ARRAY:
                  if (NpeTransformer.isNull(op.getOp1())) {
                     throw NpeTransformer.NPE;
                  }
                  break;
               case FIELD:
                  if (NpeTransformer.isNull(op.getOp())) {
                     throw NpeTransformer.NPE;
                  }
                  break;
               case IDIV:
                  if (op.getOp2().vt == Value.VT.CONSTANT) {
                     constantx = (Constant)op.getOp2();
                     if (((Number)constantx.value).intValue() == 0) {
                        throw NpeTransformer.DIVE;
                     }
                  }
                  break;
               case LDIV:
                  if (op.getOp2().vt == Value.VT.CONSTANT) {
                     constantx = (Constant)op.getOp2();
                     if (((Number)constantx.value).longValue() == 0L) {
                        throw NpeTransformer.DIVE;
                     }
                  }
                  break;
               case NEW_ARRAY:
                  if (op.getOp().vt == Value.VT.CONSTANT) {
                     constantx = (Constant)op.getOp();
                     if (((Number)constantx.value).intValue() < 0) {
                        throw NpeTransformer.NEGATIVE_ARRAY_SIZE;
                     }
                  }
                  break;
               case NEW_MUTI_ARRAY:
                  Value[] var2 = op.getOps();
                  int var3 = var2.length;

                  for(int var4 = 0; var4 < var3; ++var4) {
                     Value size = var2[var4];
                     if (size.vt == Value.VT.CONSTANT) {
                        Constant constant = (Constant)size;
                        if (((Number)constant.value).intValue() < 0) {
                           throw NpeTransformer.NEGATIVE_ARRAY_SIZE;
                        }
                     }
                  }
               }

            }
         };
         Stmt p = method.stmts.getFirst();

         while(p != null) {
            try {
               st.travel(p);
               p = p.getNext();
            } catch (NpeTransformer.MustThrowException var7) {
               this.replace(method, p);
               Stmt q = p.getNext();
               method.stmts.remove(p);
               changed = true;
               p = q;
            }
         }

         return changed;
      }
   }

   private void replace(final IrMethod m, final Stmt p) {
      StmtTraveler traveler = new StmtTraveler() {
         public Value travel(Value op) {
            Value[] ops;
            int i;
            Constant constantx;
            switch(op.vt) {
            case INVOKE_VIRTUAL:
            case INVOKE_SPECIAL:
            case INVOKE_INTERFACE:
               ops = op.getOps();
               if (NpeTransformer.isNull(ops[0])) {
                  for(i = 1; i < ops.length; ++i) {
                     this.travel(ops[i]);
                  }

                  throw NpeTransformer.NPE;
               }
               break;
            case ARRAY:
               if (NpeTransformer.isNull(op.getOp1())) {
                  this.travel(op.getOp2());
                  throw NpeTransformer.NPE;
               }
               break;
            case FIELD:
               if (NpeTransformer.isNull(op.getOp())) {
                  throw NpeTransformer.NPE;
               }
               break;
            case IDIV:
               if (op.getOp2().vt == Value.VT.CONSTANT) {
                  constantx = (Constant)op.getOp2();
                  if (((Number)constantx.value).intValue() == 0) {
                     this.travel(op.getOp1());
                     throw NpeTransformer.DIVE;
                  }
               }
               break;
            case LDIV:
               if (op.getOp2().vt == Value.VT.CONSTANT) {
                  constantx = (Constant)op.getOp2();
                  if (((Number)constantx.value).longValue() == 0L) {
                     this.travel(op.getOp1());
                     throw NpeTransformer.DIVE;
                  }
               }
               break;
            case NEW_ARRAY:
               if (op.getOp().vt == Value.VT.CONSTANT) {
                  constantx = (Constant)op.getOp();
                  if (((Number)constantx.value).intValue() < 0) {
                     throw NpeTransformer.NEGATIVE_ARRAY_SIZE;
                  }
               }
               break;
            case NEW_MUTI_ARRAY:
               ops = op.getOps();
               i = ops.length;

               for(int var4 = 0; var4 < i; ++var4) {
                  Value size = ops[var4];
                  if (size.vt == Value.VT.CONSTANT) {
                     Constant constant = (Constant)size;
                     if (((Number)constant.value).intValue() < 0) {
                        throw NpeTransformer.NEGATIVE_ARRAY_SIZE;
                     }

                     this.travel(size);
                  }
               }
            }

            Value sop = super.travel(op);
            if (sop.vt != Value.VT.LOCAL && sop.vt != Value.VT.CONSTANT) {
               Local local = new Local();
               m.locals.add(local);
               m.stmts.insertBefore(p, Stmts.nAssign(local, sop));
               return local;
            } else {
               return sop;
            }
         }
      };

      try {
         switch(p.et) {
         case E0:
         case En:
         default:
            break;
         case E1:
            traveler.travel(p.getOp());
            break;
         case E2:
            if (p.st == Stmt.ST.ASSIGN) {
               switch(p.getOp1().vt) {
               case ARRAY:
                  traveler.travel(p.getOp1().getOp1());
                  traveler.travel(p.getOp1().getOp2());
                  traveler.travel(p.getOp2());
                  break;
               case FIELD:
                  traveler.travel(p.getOp1().getOp());
                  traveler.travel(p.getOp2());
               case IDIV:
               case LDIV:
               case NEW_ARRAY:
               case NEW_MUTI_ARRAY:
               default:
                  break;
               case STATIC_FIELD:
               case LOCAL:
                  traveler.travel(p.getOp2());
               }
            } else if (p.st == Stmt.ST.FILL_ARRAY_DATA) {
               if (isNull(p.getOp1())) {
                  throw NPE;
               }

               traveler.travel(p.getOp1());
            }
         }
      } catch (NpeTransformer.MustThrowException var5) {
         if (var5 == NPE) {
            m.stmts.insertBefore(p, Stmts.nThrow(Exprs.nInvokeNew(new Value[0], new String[0], "Ljava/lang/NullPointerException;")));
         } else if (var5 == DIVE) {
            m.stmts.insertBefore(p, Stmts.nThrow(Exprs.nInvokeNew(new Value[]{Exprs.nString("divide by zero")}, new String[]{"Ljava/lang/String;"}, "Ljava/lang/ArithmeticException;")));
         } else if (var5 == NEGATIVE_ARRAY_SIZE) {
            m.stmts.insertBefore(p, Stmts.nThrow(Exprs.nInvokeNew(new Value[0], new String[0], "Ljava/lang/NegativeArraySizeException;")));
         }
      }

   }

   static boolean isNull(Value v) {
      if (v.vt == Value.VT.CONSTANT) {
         Constant cst = (Constant)v;
         if (Constant.Null.equals(cst.value)) {
            return true;
         }

         if (cst.value instanceof Number) {
            return ((Number)cst.value).intValue() == 0;
         }
      }

      return false;
   }

   private static class MustThrowException extends RuntimeException {
      private MustThrowException() {
      }

      // $FF: synthetic method
      MustThrowException(Object x0) {
         this();
      }
   }
}
