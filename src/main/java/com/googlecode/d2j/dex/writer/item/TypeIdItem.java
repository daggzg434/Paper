package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class TypeIdItem extends BaseItem implements Comparable<TypeIdItem> {
   public final StringIdItem descriptor;

   public TypeIdItem(StringIdItem stringIdItem) {
      this.descriptor = stringIdItem;
   }

   public int place(int offset) {
      return offset + 4;
   }

   public String toString() {
      return "TypeIdItem [descriptor=" + this.descriptor + "]";
   }

   public void write(DataOut out) {
      out.uint("descriptor_idx", this.descriptor.index);
   }

   public int compareTo(TypeIdItem o) {
      return this.descriptor.compareTo(o.descriptor);
   }
}
