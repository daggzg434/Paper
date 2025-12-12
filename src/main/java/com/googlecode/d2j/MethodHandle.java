package com.googlecode.d2j;

public class MethodHandle {
   public static final int STATIC_PUT = 0;
   public static final int STATIC_GET = 1;
   public static final int INSTANCE_PUT = 2;
   public static final int INSTANCE_GET = 3;
   public static final int INVOKE_STATIC = 4;
   public static final int INVOKE_INSTANCE = 5;
   private int type;
   private Field field;
   private Method method;

   public MethodHandle(int type, Field field) {
      this.type = type;
      this.field = field;
   }

   public MethodHandle(int type, Method method) {
      this.type = type;
      this.method = method;
   }

   public MethodHandle(int type, Field field, Method method) {
      this.type = type;
      this.field = field;
      this.method = method;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         MethodHandle that = (MethodHandle)o;
         if (this.type != that.type) {
            return false;
         } else {
            if (this.field != null) {
               if (this.field.equals(that.field)) {
                  return this.method != null ? this.method.equals(that.method) : that.method == null;
               }
            } else if (that.field == null) {
               return this.method != null ? this.method.equals(that.method) : that.method == null;
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.type;
      result = 31 * result + (this.field != null ? this.field.hashCode() : 0);
      result = 31 * result + (this.method != null ? this.method.hashCode() : 0);
      return result;
   }

   public int getType() {
      return this.type;
   }

   public Field getField() {
      return this.field;
   }

   public Method getMethod() {
      return this.method;
   }
}
