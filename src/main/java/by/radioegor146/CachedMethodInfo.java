package by.radioegor146;

import java.util.Objects;

public class CachedMethodInfo {
   public String clazz;
   public String name;
   public String desc;
   public boolean isStatic;

   public CachedMethodInfo(String clazz, String name, String desc, boolean isStatic) {
      this.clazz = clazz;
      this.name = name;
      this.desc = desc;
      this.isStatic = isStatic;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         CachedMethodInfo other = (CachedMethodInfo)obj;
         if (!Objects.equals(this.clazz, other.clazz)) {
            return false;
         } else if (!Objects.equals(this.name, other.name)) {
            return false;
         } else {
            return !Objects.equals(this.desc, other.desc) ? false : Objects.equals(this.isStatic, other.isStatic);
         }
      }
   }

   public int hashCode() {
      int hash = 7;
      int hash = 97 * hash + Objects.hashCode(this.clazz);
      hash = 97 * hash + Objects.hashCode(this.name);
      hash = 97 * hash + Objects.hashCode(this.desc);
      hash = 97 * hash + Objects.hashCode(this.isStatic);
      return hash;
   }
}
