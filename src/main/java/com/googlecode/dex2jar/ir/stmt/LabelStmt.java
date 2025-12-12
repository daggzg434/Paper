package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LabelStmt extends Stmt.E0Stmt {
   public String displayName;
   public int lineNumber = -1;
   public List<AssignStmt> phis;
   public Object tag;

   public LabelStmt() {
      super(Stmt.ST.LABEL);
   }

   public LabelStmt clone(LabelAndLocalMapper mapper) {
      LabelStmt labelStmt = mapper.map(this);
      if (this.phis != null && labelStmt.phis == null) {
         labelStmt.phis = new ArrayList(this.phis.size());
         Iterator var3 = this.phis.iterator();

         while(var3.hasNext()) {
            AssignStmt phi = (AssignStmt)var3.next();
            labelStmt.phis.add((AssignStmt)phi.clone(mapper));
         }
      }

      return labelStmt;
   }

   public String getDisplayName() {
      if (this.displayName != null) {
         return this.displayName;
      } else {
         int x = this.hashCode();
         return String.format("L%08x", x);
      }
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(this.getDisplayName()).append(":");
      if (this.phis != null && this.phis.size() > 0) {
         sb.append(" // ").append(this.phis);
      }

      if (this.lineNumber >= 0) {
         sb.append(" // line ").append(this.lineNumber);
      }

      return sb.toString();
   }
}
