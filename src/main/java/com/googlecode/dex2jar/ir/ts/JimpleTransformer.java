package com.googlecode.dex2jar.ir.ts;

import com.googlecode.d2j.DexType;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JimpleTransformer implements Transformer {
   public void transform(IrMethod method) {
      List<Stmt> tmp = new ArrayList();
      JimpleTransformer.N n = new JimpleTransformer.N(tmp, method.locals);

      for(Stmt p = method.stmts.getFirst(); p != null; p = p.getNext()) {
         tmp.clear();
         this.convertStmt(p, n);
         Iterator var5 = tmp.iterator();

         while(var5.hasNext()) {
            Stmt t = (Stmt)var5.next();
            method.stmts.insertBefore(p, t);
         }
      }

   }

   private Value convertExpr(Value x, boolean keep, JimpleTransformer.N tmp) {
      switch(x.et) {
      case E0:
         if (keep) {
            break;
         }

         switch(x.vt) {
         case CONSTANT:
            Constant cst = (Constant)x;
            if (!(cst.value instanceof String) && !(cst.value instanceof DexType) && !cst.value.getClass().isArray()) {
               return x;
            }

            return tmp.newAssign(x);
         case NEW:
         case STATIC_FIELD:
            return tmp.newAssign(x);
         default:
            return x;
         }
      case E1:
         x.setOp(this.convertExpr(x.getOp(), false, tmp));
         if (!keep) {
            return tmp.newAssign(x);
         }
         break;
      case E2:
         x.setOp1(this.convertExpr(x.getOp1(), false, tmp));
         x.setOp2(this.convertExpr(x.getOp2(), false, tmp));
         if (!keep) {
            return tmp.newAssign(x);
         }
         break;
      case En:
         Value[] ops = x.getOps();

         for(int i = 0; i < ops.length; ++i) {
            ops[i] = this.convertExpr(ops[i], false, tmp);
         }

         if (!keep) {
            return tmp.newAssign(x);
         }
      }

      return x;
   }

   private void convertStmt(Stmt p, JimpleTransformer.N tmp) {
      switch(p.et) {
      case E0:
         return;
      case E1:
         boolean keep;
         switch(p.st) {
         case LOOKUP_SWITCH:
         case TABLE_SWITCH:
         case RETURN:
         case THROW:
            keep = false;
            break;
         default:
            keep = true;
         }

         p.setOp(this.convertExpr(p.getOp(), keep, tmp));
         break;
      case E2:
         if (p.st == Stmt.ST.IDENTITY) {
            return;
         }

         if (p.st == Stmt.ST.FILL_ARRAY_DATA) {
            p.setOp1(this.convertExpr(p.getOp1(), false, tmp));
            p.setOp2(this.convertExpr(p.getOp2(), true, tmp));
         } else {
            p.setOp1(this.convertExpr(p.getOp1(), true, tmp));
            p.setOp2(this.convertExpr(p.getOp2(), p.getOp1().vt == Value.VT.LOCAL, tmp));
         }
         break;
      case En:
         Value[] ops = p.getOps();

         for(int i = 0; i < ops.length; ++i) {
            ops[i] = this.convertExpr(ops[i], true, tmp);
         }
      }

   }

   static class N {
      public List<Stmt> tmp;
      int nextIdx;
      private List<Local> locals;

      public N(List<Stmt> tmp, List<Local> locals) {
         this.tmp = tmp;
         this.locals = locals;
         this.nextIdx = locals.size();
      }

      Value newAssign(Value x) {
         Local loc = Exprs.nLocal(this.nextIdx++);
         loc.valueType = x.valueType;
         this.locals.add(loc);
         this.tmp.add(Stmts.nAssign(loc, x));
         return loc;
      }
   }
}
