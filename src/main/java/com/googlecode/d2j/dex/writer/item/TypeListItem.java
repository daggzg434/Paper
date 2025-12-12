package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.Iterator;
import java.util.List;

public class TypeListItem extends BaseItem implements Comparable<TypeListItem> {
   public final List<TypeIdItem> items;

   public TypeListItem(List<TypeIdItem> items) {
      this.items = items;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.items == null ? 0 : this.items.hashCode());
      return result;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         TypeListItem other = (TypeListItem)obj;
         if (this.items == null) {
            if (other.items != null) {
               return false;
            }
         } else if (!this.items.equals(other.items)) {
            return false;
         }

         return true;
      }
   }

   public int place(int offset) {
      return offset + 4 + this.items.size() * 2;
   }

   public void write(DataOut out) {
      out.uint("size", this.items.size());
      Iterator var2 = this.items.iterator();

      while(var2.hasNext()) {
         TypeIdItem idItem = (TypeIdItem)var2.next();
         out.ushort("type_idx", idItem.index);
      }

   }

   public int compareTo(TypeListItem o) {
      int min = Math.min(this.items.size(), o.items.size());

      for(int i = 0; i < min; ++i) {
         int x = ((TypeIdItem)this.items.get(i)).compareTo((TypeIdItem)o.items.get(i));
         if (x != 0) {
            return x;
         }
      }

      return this.items.size() == o.items.size() ? 0 : (this.items.size() < o.items.size() ? -1 : 1);
   }
}
