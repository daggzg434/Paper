package com.googlecode.dex2jar.ir;

public enum TypeClass {
   BOOLEAN("Z", true),
   INT("I", true),
   FLOAT("F", true),
   DOUBLE("D", true),
   LONG("J", true),
   OBJECT("L", true),
   VOID("V", true),
   UNKNOWN("?"),
   ZIL("s"),
   ZIFL("z"),
   ZIF("m"),
   ZI("n"),
   IF("i"),
   JD("w");

   public String name;
   public boolean fixed;

   private TypeClass(String use, boolean fixed) {
      this.name = use;
      this.fixed = fixed;
   }

   private TypeClass(String use) {
      this.name = use;
      this.fixed = false;
   }

   public static TypeClass clzOf(String desc) {
      switch(desc.charAt(0)) {
      case 'B':
      case 'C':
      case 'I':
      case 'S':
         return INT;
      case 'D':
         return DOUBLE;
      case 'E':
      case 'G':
      case 'H':
      case 'K':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'T':
      case 'U':
      case 'W':
      case 'X':
      case 'Y':
      case '\\':
      case ']':
      case '^':
      case '_':
      case '`':
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      case 'g':
      case 'h':
      case 'j':
      case 'k':
      case 'l':
      case 'o':
      case 'p':
      case 'q':
      case 'r':
      case 't':
      case 'u':
      case 'v':
      case 'x':
      case 'y':
      default:
         return UNKNOWN;
      case 'F':
         return FLOAT;
      case 'J':
         return LONG;
      case 'L':
      case '[':
         return OBJECT;
      case 'V':
         return VOID;
      case 'Z':
         return BOOLEAN;
      case 'i':
         return IF;
      case 'm':
         return ZIF;
      case 'n':
         return ZI;
      case 's':
         return ZIL;
      case 'w':
         return JD;
      case 'z':
         return ZIFL;
      }
   }

   public static TypeClass merge(TypeClass thizCls, TypeClass clz) {
      if (thizCls == clz) {
         return thizCls;
      } else if (thizCls == UNKNOWN) {
         return clz;
      } else if (clz == UNKNOWN) {
         return thizCls;
      } else if (thizCls.fixed) {
         if (!clz.fixed) {
            return thizCls;
         } else if ((thizCls != INT || clz != BOOLEAN) && thizCls != BOOLEAN && clz != INT) {
            throw new RuntimeException("can not merge " + thizCls + " and " + clz);
         } else {
            return INT;
         }
      } else {
         return clz.fixed ? clz : merge0(thizCls, clz);
      }
   }

   private static TypeClass merge0(TypeClass a, TypeClass b) {
      if (a != JD && b != JD) {
         switch(a) {
         case IF:
            switch(b) {
            case ZIFL:
            case ZIF:
               return IF;
            case IF:
            default:
               break;
            case ZI:
            case ZIL:
               return INT;
            }
         case ZIF:
            switch(b) {
            case ZIFL:
               return ZIF;
            case IF:
               return IF;
            case ZIF:
            default:
               break;
            case ZI:
            case ZIL:
               return ZI;
            }
         case ZI:
            if (b == IF) {
               return INT;
            }

            return ZI;
         case ZIL:
            switch(b) {
            case ZIFL:
               return ZIL;
            case IF:
               return INT;
            case ZIF:
            case ZI:
               return ZI;
            }
         case ZIFL:
            return b;
         default:
            throw new RuntimeException();
         }
      } else {
         throw new RuntimeException("can not merge " + a + " and " + b);
      }
   }

   public String toString() {
      return this.name;
   }
}
