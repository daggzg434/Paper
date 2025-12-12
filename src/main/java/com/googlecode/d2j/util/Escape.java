package com.googlecode.d2j.util;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;

public class Escape implements DexConstants {
   static boolean contain(int a, int b) {
      return (a & b) != 0;
   }

   public static String classAcc(int acc) {
      if (acc == 0) {
         return "0";
      } else {
         StringBuilder sb = new StringBuilder();
         if (contain(acc, 1)) {
            sb.append("ACC_PUBLIC|");
         }

         if (contain(acc, 2)) {
            sb.append("ACC_PRIVATE|");
         }

         if (contain(acc, 4)) {
            sb.append("ACC_PROTECTED|");
         }

         if (contain(acc, 8)) {
            sb.append("ACC_STATIC|");
         }

         if (contain(acc, 16)) {
            sb.append("ACC_FINAL|");
         }

         if (contain(acc, 512)) {
            sb.append("ACC_INTERFACE|");
         }

         if (contain(acc, 1024)) {
            sb.append("ACC_ABSTRACT|");
         }

         if (contain(acc, 4096)) {
            sb.append("ACC_SYNTHETIC|");
         }

         if (contain(acc, 8192)) {
            sb.append("ACC_ANNOTATION|");
         }

         if (contain(acc, 16384)) {
            sb.append("ACC_ENUM|");
         }

         if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
         }

         return sb.toString();
      }
   }

   public static String methodAcc(int acc) {
      if (acc == 0) {
         return "0";
      } else {
         StringBuilder sb = new StringBuilder();
         if (contain(acc, 1)) {
            sb.append("ACC_PUBLIC|");
         }

         if (contain(acc, 2)) {
            sb.append("ACC_PRIVATE|");
         }

         if (contain(acc, 4)) {
            sb.append("ACC_PROTECTED|");
         }

         if (contain(acc, 8)) {
            sb.append("ACC_STATIC|");
         }

         if (contain(acc, 16)) {
            sb.append("ACC_FINAL|");
         }

         if (contain(acc, 64)) {
            sb.append("ACC_BRIDGE|");
         }

         if (contain(acc, 128)) {
            sb.append("ACC_VARARGS|");
         }

         if (contain(acc, 256)) {
            sb.append("ACC_NATIVE|");
         }

         if (contain(acc, 1024)) {
            sb.append("ACC_ABSTRACT|");
         }

         if (contain(acc, 2048)) {
            sb.append("ACC_STRICT|");
         }

         if (contain(acc, 4096)) {
            sb.append("ACC_SYNTHETIC|");
         }

         if (contain(acc, 65536)) {
            sb.append("ACC_CONSTRUCTOR|");
         }

         if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
         }

         return sb.toString();
      }
   }

   public static String fieldAcc(int acc) {
      if (acc == 0) {
         return "0";
      } else {
         StringBuilder sb = new StringBuilder();
         if (contain(acc, 1)) {
            sb.append("ACC_PUBLIC|");
         }

         if (contain(acc, 2)) {
            sb.append("ACC_PRIVATE|");
         }

         if (contain(acc, 4)) {
            sb.append("ACC_PROTECTED|");
         }

         if (contain(acc, 8)) {
            sb.append("ACC_STATIC|");
         }

         if (contain(acc, 16)) {
            sb.append("ACC_FINAL|");
         }

         if (contain(acc, 64)) {
            sb.append("ACC_VOLATILE|");
         }

         if (contain(acc, 128)) {
            sb.append("ACC_TRANSIENT|");
         }

         if (contain(acc, 4096)) {
            sb.append("ACC_SYNTHETIC|");
         }

         if (contain(acc, 16384)) {
            sb.append("ACC_ENUM|");
         }

         if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
         }

         return sb.toString();
      }
   }

   public static String v(Field f) {
      return String.format("new Field(%s,%s,%s)", v(f.getOwner()), v(f.getName()), v(f.getType()));
   }

   public static String v(Method m) {
      return String.format("new Method(%s,%s,%s,%s)", v(m.getOwner()), v(m.getName()), v(m.getParameterTypes()), v(m.getReturnType()));
   }

   public static String v(Proto m) {
      return String.format("new Proto(%s,%s)", v(m.getParameterTypes()), v(m.getReturnType()));
   }

   public static String v(MethodHandle m) {
      switch(m.getType()) {
      case 0:
         return String.format("new MethodHandle(MethodHandle.STATIC_PUT,%s)", v(m.getField()));
      case 1:
         return String.format("new MethodHandle(MethodHandle.STATIC_GET,%s)", v(m.getField()));
      case 2:
         return String.format("new MethodHandle(MethodHandle.INSTANCE_PUT,%s)", v(m.getField()));
      case 3:
         return String.format("new MethodHandle(MethodHandle.INSTANCE_GET,%s)", v(m.getField()));
      case 4:
         return String.format("new MethodHandle(MethodHandle.INVOKE_STATIC,%s)", v(m.getMethod()));
      case 5:
         return String.format("new MethodHandle(MethodHandle.INVOKE_INSTANCE,%s)", v(m.getMethod()));
      default:
         throw new RuntimeException();
      }
   }

   public static String v(String s) {
      return s == null ? "null" : "\"" + Utf8Utils.escapeString(s) + "\"";
   }

   public static String v(DexType t) {
      return "new DexType(" + v(t.desc) + ")";
   }

   public static String v(int[] vs) {
      StringBuilder sb = new StringBuilder("new int[]{ ");
      boolean first = true;
      int[] var3 = vs;
      int var4 = vs.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int obj = var3[var5];
         if (first) {
            first = false;
         } else {
            sb.append(",");
         }

         sb.append(obj);
      }

      return sb.append("}").toString();
   }

   public static String v(byte[] vs) {
      StringBuilder sb = new StringBuilder("new byte[]{ ");
      boolean first = true;
      byte[] var3 = vs;
      int var4 = vs.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         byte obj = var3[var5];
         if (first) {
            first = false;
         } else {
            sb.append(",");
         }

         sb.append("(byte)").append(obj);
      }

      return sb.append("}").toString();
   }

   public static String v(String[] vs) {
      if (vs == null) {
         return "null";
      } else {
         StringBuilder sb = new StringBuilder("new String[]{ ");
         boolean first = true;
         String[] var3 = vs;
         int var4 = vs.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String obj = var3[var5];
            if (first) {
               first = false;
            } else {
               sb.append(",");
            }

            sb.append(v(obj));
         }

         return sb.append("}").toString();
      }
   }

   public static String v(Object[] vs) {
      StringBuilder sb = new StringBuilder("new Object[]{ ");
      boolean first = true;
      Object[] var3 = vs;
      int var4 = vs.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Object obj = var3[var5];
         if (first) {
            first = false;
         } else {
            sb.append(",");
         }

         sb.append(v(obj));
      }

      return sb.append("}").toString();
   }

   public static String v(Object obj) {
      if (obj == null) {
         return "null";
      } else if (obj instanceof String) {
         return v((String)obj);
      } else if (obj instanceof DexType) {
         return v((DexType)obj);
      } else if (obj instanceof Method) {
         return v((Method)obj);
      } else if (obj instanceof Field) {
         return v((Field)obj);
      } else if (obj instanceof Proto) {
         return v((Proto)obj);
      } else if (obj instanceof MethodHandle) {
         return v((MethodHandle)obj);
      } else if (obj instanceof Integer) {
         return " Integer.valueOf(" + obj + ")";
      } else if (obj instanceof Long) {
         return "Long.valueOf(" + obj + "L)";
      } else if (obj instanceof Float) {
         return "Float.valueOf(" + obj + "F)";
      } else if (obj instanceof Double) {
         return "Double.valueOf(" + obj + "D)";
      } else if (obj instanceof Short) {
         return "Short.valueOf((short)" + obj + ")";
      } else if (obj instanceof Byte) {
         return "Byte.valueOf((byte)" + obj + ")";
      } else if (obj instanceof Character) {
         return "Character.valueOf('" + obj + "')";
      } else if (obj instanceof Boolean) {
         return "Boolean.valueOf(" + obj + ")";
      } else {
         StringBuilder sb;
         boolean first;
         int var4;
         int var5;
         if (obj instanceof int[]) {
            sb = new StringBuilder("new int[]{ ");
            first = true;
            int[] var10 = (int[])((int[])obj);
            var4 = var10.length;

            for(var5 = 0; var5 < var4; ++var5) {
               int i = var10[var5];
               if (first) {
                  first = false;
               } else {
                  sb.append(",");
               }

               sb.append(i);
            }

            return sb.append("}").toString();
         } else if (obj instanceof short[]) {
            sb = new StringBuilder("new short[]{ ");
            first = true;
            short[] var9 = (short[])((short[])obj);
            var4 = var9.length;

            for(var5 = 0; var5 < var4; ++var5) {
               int i = var9[var5];
               if (first) {
                  first = false;
               } else {
                  sb.append(",");
               }

               sb.append("(short)").append(i);
            }

            return sb.append("}").toString();
         } else if (obj instanceof long[]) {
            sb = new StringBuilder("new long[]{ ");
            first = true;
            long[] var8 = (long[])((long[])obj);
            var4 = var8.length;

            for(var5 = 0; var5 < var4; ++var5) {
               long i = var8[var5];
               if (first) {
                  first = false;
               } else {
                  sb.append(",");
               }

               sb.append(i).append("L");
            }

            return sb.append("}").toString();
         } else if (obj instanceof float[]) {
            sb = new StringBuilder("new float[]{ ");
            first = true;
            float[] var3 = (float[])((float[])obj);
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               float i = var3[var5];
               if (first) {
                  first = false;
               } else {
                  sb.append(",");
               }

               sb.append(i).append("F");
            }

            return sb.append("}").toString();
         } else {
            return null;
         }
      }
   }
}
