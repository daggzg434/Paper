package com.googlecode.d2j;

public class Method {
   private String name;
   private String owner;
   private Proto proto;

   public Proto getProto() {
      return this.proto;
   }

   public Method(String owner, String name, String[] parameterTypes, String returnType) {
      this.owner = owner;
      this.name = name;
      this.proto = new Proto(parameterTypes, returnType);
   }

   public Method(String owner, String name, Proto proto) {
      this.owner = owner;
      this.name = name;
      this.proto = proto;
   }

   public String getDesc() {
      return this.proto.getDesc();
   }

   public String getName() {
      return this.name;
   }

   public String getOwner() {
      return this.owner;
   }

   public String[] getParameterTypes() {
      return this.proto.getParameterTypes();
   }

   public String getReturnType() {
      return this.proto.getReturnType();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Method method = (Method)o;
         if (this.name != null) {
            if (!this.name.equals(method.name)) {
               return false;
            }
         } else if (method.name != null) {
            return false;
         }

         if (this.owner != null) {
            if (this.owner.equals(method.owner)) {
               return this.proto.equals(method.proto);
            }
         } else if (method.owner == null) {
            return this.proto.equals(method.proto);
         }

         return false;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.name != null ? this.name.hashCode() : 0;
      result = 31 * result + (this.owner != null ? this.owner.hashCode() : 0);
      result = 31 * result + this.proto.hashCode();
      return result;
   }

   public String toString() {
      return this.getOwner() + "." + this.getName() + this.getDesc();
   }
}
