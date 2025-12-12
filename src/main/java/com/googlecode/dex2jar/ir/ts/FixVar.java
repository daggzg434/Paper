package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.LocalVar;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.Iterator;

public class FixVar implements Transformer {
   public void transform(IrMethod irMethod) {
      int i = 0;
      Iterator var3 = irMethod.vars.iterator();

      while(var3.hasNext()) {
         LocalVar var = (LocalVar)var3.next();
         if (var.reg.trim().vt != Value.VT.LOCAL && var.reg.trim().vt == Value.VT.CONSTANT) {
            Local n = new Local(i++);
            Value old = var.reg.trim();
            irMethod.stmts.insertBefore(var.start, Stmts.nAssign(n, old));
            var.reg = n;
            irMethod.locals.add(n);
         }
      }

   }
}
