package com.googlecode.d2j;

public class DexLabel {
   public String displayName;
   private int offset = -1;

   public DexLabel(int offset) {
      this.offset = offset;
   }

   public DexLabel() {
   }

   public String toString() {
      if (this.displayName != null) {
         return this.displayName;
      } else {
         return this.offset >= 0 ? String.format("L%04x", this.offset) : String.format("L%08x", this.hashCode());
      }
   }
}
