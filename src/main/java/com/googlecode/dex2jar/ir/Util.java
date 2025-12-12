package com.googlecode.dex2jar.ir;

import java.util.ArrayList;
import java.util.List;

public class Util {
   public static List<String> listDesc(String desc) {
      List<String> list = new ArrayList(5);
      char[] chars = desc.toCharArray();
      int i = 0;

      while(true) {
         while(i < chars.length) {
            int count;
            switch(chars[i]) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'V':
            case 'Z':
               list.add(Character.toString(chars[i]));
               ++i;
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
            default:
               break;
            case 'L':
               for(count = 1; chars[i + count] != ';'; ++count) {
               }

               ++count;
               list.add(new String(chars, i, count));
               i += count + 1;
               break;
            case '[':
               for(count = 1; chars[i + count] == '['; ++count) {
               }

               if (chars[i + count] == 'L') {
                  ++count;

                  while(chars[i + count] != ';') {
                     ++count;
                  }
               }

               ++count;
               list.add(new String(chars, i, count));
               i += count + 1;
            }
         }

         return list;
      }
   }

   public static void appendString(final StringBuffer buf, final String s) {
      buf.append('"');

      for(int i = 0; i < s.length(); ++i) {
         char c = s.charAt(i);
         if (c == '\n') {
            buf.append("\\n");
         } else if (c == '\r') {
            buf.append("\\r");
         } else if (c == '\\') {
            buf.append("\\\\");
         } else if (c == '"') {
            buf.append("\\\"");
         } else if (c >= ' ' && c <= 127) {
            buf.append(c);
         } else {
            buf.append("\\u");
            if (c < 16) {
               buf.append("000");
            } else if (c < 256) {
               buf.append("00");
            } else if (c < 4096) {
               buf.append('0');
            }

            buf.append(Integer.toString(c, 16));
         }
      }

      buf.append('"');
   }

   public static String toShortClassName(String desc) {
      int d;
      switch(desc.charAt(0)) {
      case 'B':
         return "byte";
      case 'C':
         return "char";
      case 'D':
         return "double";
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
      default:
         throw new UnsupportedOperationException();
      case 'F':
         return "float";
      case 'I':
         return "int";
      case 'J':
         return "long";
      case 'L':
         d = desc.lastIndexOf(47);
         return desc.substring(d < 0 ? 1 : d + 1, desc.length() - 1);
      case 'S':
         return "short";
      case 'V':
         return "void";
      case 'Z':
         return "boolean";
      case '[':
         for(d = 1; d < desc.length() && desc.charAt(d) == '['; ++d) {
         }

         StringBuilder sb = (new StringBuilder()).append(toShortClassName(desc.substring(d)));

         for(int t = 0; t < d; ++t) {
            sb.append("[]");
         }

         return sb.toString();
      }
   }
}
