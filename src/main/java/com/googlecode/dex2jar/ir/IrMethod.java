package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IrMethod {
   public boolean isStatic;
   public String[] args;
   public List<Local> locals = new ArrayList();
   public String name;
   public String owner;
   public String ret;
   public StmtList stmts = new StmtList();
   public List<Trap> traps = new ArrayList();
   public List<LocalVar> vars = new ArrayList();
   public List<LabelStmt> phiLabels;

   public IrMethod clone() {
      IrMethod n = new IrMethod();
      LabelAndLocalMapper mapper = new LabelAndLocalMapper();
      n.name = this.name;
      n.args = this.args;
      n.isStatic = this.isStatic;
      n.owner = this.owner;
      n.ret = this.ret;
      n.stmts = this.stmts.clone(mapper);
      Iterator var3 = this.traps.iterator();

      while(var3.hasNext()) {
         Trap trap = (Trap)var3.next();
         n.traps.add(trap.clone(mapper));
      }

      var3 = this.vars.iterator();

      while(var3.hasNext()) {
         LocalVar var = (LocalVar)var3.next();
         n.vars.add(var.clone(mapper));
      }

      if (this.phiLabels != null) {
         List<LabelStmt> nPhiLabels = new ArrayList(this.phiLabels.size());
         Iterator var8 = this.phiLabels.iterator();

         while(var8.hasNext()) {
            LabelStmt labelStmt = (LabelStmt)var8.next();
            nPhiLabels.add(labelStmt.clone(mapper));
         }

         n.phiLabels = nPhiLabels;
      }

      var3 = this.locals.iterator();

      while(var3.hasNext()) {
         Local local = (Local)var3.next();
         n.locals.add((Local)local.clone(mapper));
      }

      return n;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("// ").append(this.owner).append("\n");
      if (this.isStatic) {
         sb.append(" static ");
      }

      sb.append(this.ret == null ? null : Util.toShortClassName(this.ret)).append(' ').append(this.name).append('(');
      if (this.args != null) {
         boolean first = true;
         String[] var3 = this.args;
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String arg = var3[var5];
            if (first) {
               first = false;
            } else {
               sb.append(',');
            }

            sb.append(Util.toShortClassName(arg));
         }
      }

      sb.append(") {\n\n").append(this.stmts).append("\n");
      if (this.traps.size() > 0 || this.vars.size() > 0) {
         sb.append("=============\n");
         Iterator var7 = this.traps.iterator();

         while(var7.hasNext()) {
            Trap trap = (Trap)var7.next();
            sb.append(trap).append('\n');
         }

         var7 = this.vars.iterator();

         while(var7.hasNext()) {
            LocalVar var = (LocalVar)var7.next();
            sb.append(var).append('\n');
         }
      }

      sb.append("}");
      return sb.toString();
   }
}
