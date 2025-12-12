package com.googlecode.d2j.util;

public class AccUtils {
   public static boolean isBridge(int acc) {
      return (acc & 64) != 0;
   }

   public static boolean isEnum(int acc) {
      return (acc & 16384) != 0;
   }

   public static boolean isFinal(int acc) {
      return (acc & 16) != 0;
   }

   public static boolean isPrivate(int acc) {
      return (acc & 2) != 0;
   }

   public static boolean isProtected(int acc) {
      return (acc & 4) != 0;
   }

   public static boolean isPublic(int acc) {
      return (acc & 1) != 0;
   }

   public static boolean isStatic(int acc) {
      return (acc & 8) != 0;
   }

   public static boolean isSynthetic(int acc) {
      return (acc & 4096) != 0;
   }
}
