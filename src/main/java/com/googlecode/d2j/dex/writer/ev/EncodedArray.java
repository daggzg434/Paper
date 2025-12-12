package com.googlecode.d2j.dex.writer.ev;

import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EncodedArray {
   public List<EncodedValue> values = new ArrayList(5);

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EncodedArray that = (EncodedArray)o;
         return this.values.equals(that.values);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.values.hashCode();
   }

   public int place(int offset) {
      offset += BaseItem.lengthOfUleb128(this.values.size());

      EncodedValue ev;
      for(Iterator var2 = this.values.iterator(); var2.hasNext(); offset = ev.place(offset)) {
         ev = (EncodedValue)var2.next();
      }

      return offset;
   }

   public void write(DataOut out) {
      out.uleb128("size", this.values.size());
      Iterator var2 = this.values.iterator();

      while(var2.hasNext()) {
         EncodedValue ev = (EncodedValue)var2.next();
         ev.write(out);
      }

   }
}
