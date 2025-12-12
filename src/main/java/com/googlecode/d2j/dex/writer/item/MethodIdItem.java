package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class MethodIdItem extends BaseItem implements Comparable<MethodIdItem> {
   public final StringIdItem name;
   public final TypeIdItem clazz;
   public final ProtoIdItem proto;

   public MethodIdItem(TypeIdItem typeItem, StringIdItem nameItem, ProtoIdItem protoIdItem) {
      this.clazz = typeItem;
      this.name = nameItem;
      this.proto = protoIdItem;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
      result = 31 * result + (this.proto == null ? 0 : this.proto.hashCode());
      result = 31 * result + (this.clazz == null ? 0 : this.clazz.hashCode());
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
         MethodIdItem other = (MethodIdItem)obj;
         if (this.name == null) {
            if (other.name != null) {
               return false;
            }
         } else if (!this.name.equals(other.name)) {
            return false;
         }

         if (this.proto == null) {
            if (other.proto != null) {
               return false;
            }
         } else if (!this.proto.equals(other.proto)) {
            return false;
         }

         if (this.clazz == null) {
            if (other.clazz != null) {
               return false;
            }
         } else if (!this.clazz.equals(other.clazz)) {
            return false;
         }

         return true;
      }
   }

   public int place(int offset) {
      return offset + 8;
   }

   public int compareTo(MethodIdItem o) {
      int x = this.clazz.compareTo(o.clazz);
      if (x != 0) {
         return x;
      } else {
         x = this.name.compareTo(o.name);
         return x != 0 ? x : this.proto.compareTo(o.proto);
      }
   }

   public void write(DataOut out) {
      out.ushort("class_idx", this.clazz.index);
      out.ushort("proto_idx", this.proto.index);
      out.uint("name_idx", this.name.index);
   }
}
