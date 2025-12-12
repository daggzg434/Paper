package com.googlecode.dex2jar.ir.ts.array;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import com.googlecode.dex2jar.ir.ts.Transformer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayNullPointerTransformer implements Transformer {
   public void transform(IrMethod irMethod) {
      Stmt p = irMethod.stmts.getFirst();

      while(p != null) {
         if (this.arrayNPE(p)) {
            Stmt q = p.getNext();
            this.replaceNPE(irMethod.stmts, irMethod.locals, p);
            p = q;
         } else {
            p = p.getNext();
         }
      }

   }

   private void replaceNPE(StmtList stmts, List<Local> locals, Stmt p) {
      List<Value> values = new ArrayList();
      switch(p.et) {
      case E1:
         this.tryAdd(((Stmt.E1Stmt)p).op.trim(), values);
         break;
      case E2:
         Stmt.E2Stmt e2 = (Stmt.E2Stmt)p;
         switch(e2.op1.trim().vt) {
         case LOCAL:
            this.tryAdd(e2.op2.trim(), values);
            break;
         case ARRAY:
            ArrayExpr ae = (ArrayExpr)e2.op1.trim();
            if (this.tryAdd(ae.op1.trim(), values) && this.tryAdd(ae.op2.trim(), values)) {
               this.tryAdd(e2.op2.trim(), values);
            }
            break;
         case FIELD:
            FieldExpr fe = (FieldExpr)e2.op1.trim();
            if (fe.op == null || fe.op.trim() == null || this.tryAdd(fe.op.trim(), values)) {
               this.tryAdd(e2.op2.trim(), values);
            }
            break;
         default:
            if (this.tryAdd(e2.op2.trim(), values)) {
               this.tryAdd(e2.op1.trim(), values);
            }
         }
      }

      Iterator var8 = values.iterator();

      while(var8.hasNext()) {
         Value value = (Value)var8.next();
         switch(value.vt) {
         case LOCAL:
         case CONSTANT:
            break;
         default:
            Local n = Exprs.nLocal("xxx");
            locals.add(n);
            stmts.insertBefore(p, Stmts.nAssign(n, value));
         }
      }

      stmts.insertBefore(p, Stmts.nThrow(Exprs.nInvokeNew(new Value[0], new String[0], "Ljava/lang/NullPointerException;")));
      stmts.remove(p);
   }

   private boolean tryAdd(Value value, List<Value> values) {
      if (!this.arrayNPE(value)) {
         values.add(value);
         return true;
      } else {
         switch(value.et) {
         case E1:
            Value.E1Expr e1 = (Value.E1Expr)value;
            if (e1.op != null && e1.op.trim() != null) {
               this.tryAdd(e1.op.trim(), values);
               break;
            }

            return false;
         case E2:
            Value.E2Expr e2 = (Value.E2Expr)value;
            if (e2.vt == Value.VT.ARRAY && e2.op1.trim().vt == Value.VT.CONSTANT) {
               Constant cst = (Constant)e2.op1.trim();
               if (cst.value.equals(0)) {
                  this.tryAdd(e2.op2.trim(), values);
                  return false;
               }
            }

            if (this.tryAdd(e2.op1.trim(), values)) {
               this.tryAdd(e2.op2.trim(), values);
            }
         case En:
            Value[] var9 = ((Value.EnExpr)value).ops;
            int var6 = var9.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Value vb = var9[var7];
               if (!this.tryAdd(vb.trim(), values)) {
                  return false;
               }
            }

            return false;
         case E0:
            values.add(value);
         }

         return false;
      }
   }

   private boolean arrayNPE(Stmt p) {
      switch(p.et) {
      case E1:
         if (p.st == Stmt.ST.GOTO) {
            return false;
         }

         return this.arrayNPE(((Stmt.E1Stmt)p).op.trim());
      case E2:
         Stmt.E2Stmt e2 = (Stmt.E2Stmt)p;
         switch(e2.op1.trim().vt) {
         case ARRAY:
         case FIELD:
            return this.arrayNPE(e2.op1.trim()) || this.arrayNPE(e2.op2.trim());
         default:
            return this.arrayNPE(e2.op2.trim()) || this.arrayNPE(e2.op1.trim());
         }
      case E0:
         return false;
      case En:
         return false;
      default:
         return false;
      }
   }

   private boolean arrayNPE(Value value) {
      switch(value.et) {
      case E1:
         Value.E1Expr e1 = (Value.E1Expr)value;
         if (e1.op != null && e1.op.trim() != null) {
            return this.arrayNPE(e1.op.trim());
         }

         return false;
      case E2:
         Value.E2Expr e2 = (Value.E2Expr)value;
         if (e2.vt == Value.VT.ARRAY && e2.op1.trim().vt == Value.VT.CONSTANT) {
            Constant cst = (Constant)e2.op1.trim();
            if (cst.value.equals(0)) {
               return true;
            }
         }

         return this.arrayNPE(e2.op1.trim()) || this.arrayNPE(e2.op2.trim());
      case E0:
         return false;
      case En:
         Value[] var4 = ((Value.EnExpr)value).ops;
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Value vb = var4[var6];
            if (this.arrayNPE(vb.trim())) {
               return true;
            }
         }
      default:
         return false;
      }
   }
}
