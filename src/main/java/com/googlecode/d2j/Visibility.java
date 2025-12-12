package com.googlecode.d2j;

public enum Visibility {
   BUILD(0),
   RUNTIME(1),
   SYSTEM(2);

   public int value;

   private Visibility(int v) {
      this.value = v;
   }

   public String displayName() {
      return this.name().toLowerCase();
   }
}
