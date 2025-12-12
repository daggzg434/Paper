package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class FieldIdItem extends BaseItem implements Comparable<FieldIdItem> {
   public final TypeIdItem clazz;
   public final TypeIdItem type;
   public final StringIdItem name;

   public String getTypeString() {
      return this.type.descriptor.stringData.string;
   }

   public FieldIdItem(TypeIdItem clazz, StringIdItem name, TypeIdItem type) {
      this.clazz = clazz;
      this.name = name;
      this.type = type;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.clazz == null ? 0 : this.clazz.hashCode());
      result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
      result = 31 * result + (this.type == null ? 0 : this.type.hashCode());
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
         FieldIdItem other = (FieldIdItem)obj;
         if (this.clazz == null) {
            if (other.clazz != null) {
               return false;
            }
         } else if (!this.clazz.equals(other.clazz)) {
            return false;
         }

         if (this.name == null) {
            if (other.name != null) {
               return false;
            }
         } else if (!this.name.equals(other.name)) {
            return false;
         }

         if (this.type == null) {
            if (other.type != null) {
               return false;
            }
         } else if (!this.type.equals(other.type)) {
            return false;
         }

         return true;
      }
   }

   public int place(int offset) {
      return offset + 8;
   }

   public int compareTo(FieldIdItem o) {
      int x = this.clazz.compareTo(o.clazz);
      if (x != 0) {
         return x;
      } else {
         x = this.name.compareTo(o.name);
         return x != 0 ? x : this.type.compareTo(o.type);
      }
   }

   public void write(DataOut out) {
      out.ushort("class_idx", this.clazz.index);
      out.ushort("proto_idx", this.type.index);
      out.uint("name_idx", this.name.index);
   }
}
