package com.googlecode.d2j;

import java.util.Arrays;

public class Proto {
   private String desc;
   private String[] parameterTypes;
   private String returnType;

   public Proto(String[] parameterTypes, String returnType) {
      this.parameterTypes = parameterTypes;
      this.returnType = returnType;
   }

   public String[] getParameterTypes() {
      return this.parameterTypes;
   }

   public String getReturnType() {
      return this.returnType;
   }

   public String getDesc() {
      if (this.desc == null) {
         StringBuilder ps = new StringBuilder("(");
         if (this.parameterTypes != null) {
            String[] var2 = this.parameterTypes;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               String t = var2[var4];
               ps.append(t);
            }
         }

         ps.append(")").append(this.returnType);
         this.desc = ps.toString();
      }

      return this.desc;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Proto proto = (Proto)o;
         if (!Arrays.equals(this.parameterTypes, proto.parameterTypes)) {
            return false;
         } else {
            return this.returnType != null ? this.returnType.equals(proto.returnType) : proto.returnType == null;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = Arrays.hashCode(this.parameterTypes);
      result = 31 * result + (this.returnType != null ? this.returnType.hashCode() : 0);
      return result;
   }
}
