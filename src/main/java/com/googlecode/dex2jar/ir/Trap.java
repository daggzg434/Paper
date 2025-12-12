package com.googlecode.dex2jar.ir;

import com.googlecode.dex2jar.ir.stmt.LabelStmt;

public class Trap {
   public LabelStmt start;
   public LabelStmt end;
   public LabelStmt[] handlers;
   public String[] types;

   public Trap() {
   }

   public Trap(LabelStmt start, LabelStmt end, LabelStmt[] handlers, String[] types) {
      this.start = start;
      this.end = end;
      this.handlers = handlers;
      this.types = types;
   }

   public Trap clone(LabelAndLocalMapper mapper) {
      int size = this.handlers.length;
      LabelStmt[] cloneHandlers = new LabelStmt[size];
      String[] cloneTypes = new String[size];

      for(int i = 0; i < size; ++i) {
         cloneHandlers[i] = this.handlers[i].clone(mapper);
         cloneTypes[i] = this.types[i];
      }

      return new Trap(this.start.clone(mapper), this.end.clone(mapper), cloneHandlers, cloneTypes);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(String.format(".catch %s - %s : ", this.start.getDisplayName(), this.end.getDisplayName()));

      for(int i = 0; i < this.handlers.length; ++i) {
         sb.append(this.types[i] == null ? "all" : this.types[i]).append(" > ").append(this.handlers[i].getDisplayName()).append(",");
      }

      return sb.toString();
   }
}
