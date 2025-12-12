package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class ProtoIdItem extends BaseItem implements Comparable<ProtoIdItem> {
   public final StringIdItem shorty;
   public final TypeIdItem ret;
   public final TypeListItem parameters;

   public ProtoIdItem(TypeListItem parameters, TypeIdItem ret, StringIdItem shorty) {
      this.parameters = parameters;
      this.ret = ret;
      this.shorty = shorty;
   }

   public int hashCode() {
      int prime = true;
      int result = 1;
      int result = 31 * result + (this.parameters == null ? 0 : this.parameters.hashCode());
      result = 31 * result + (this.ret == null ? 0 : this.ret.hashCode());
      result = 31 * result + (this.shorty == null ? 0 : this.shorty.hashCode());
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
         ProtoIdItem other = (ProtoIdItem)obj;
         if (this.parameters == null) {
            if (other.parameters != null) {
               return false;
            }
         } else if (!this.parameters.equals(other.parameters)) {
            return false;
         }

         if (this.ret == null) {
            if (other.ret != null) {
               return false;
            }
         } else if (!this.ret.equals(other.ret)) {
            return false;
         }

         if (this.shorty == null) {
            if (other.shorty != null) {
               return false;
            }
         } else if (!this.shorty.equals(other.shorty)) {
            return false;
         }

         return true;
      }
   }

   public int place(int offset) {
      return offset + 12;
   }

   public int compareTo(ProtoIdItem o) {
      int x = this.ret.compareTo(o.ret);
      return x != 0 ? x : this.parameters.compareTo(o.parameters);
   }

   public void write(DataOut out) {
      out.uint("shorty_idx", this.shorty.index);
      out.uint("return_type_idx", this.ret.index);
      out.uint("parameters_off", this.parameters != null && this.parameters.items.size() != 0 ? this.parameters.offset : 0);
   }
}
