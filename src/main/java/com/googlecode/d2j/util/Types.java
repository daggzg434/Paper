package com.googlecode.d2j.util;

import com.googlecode.d2j.DexException;
import java.util.ArrayList;
import java.util.List;

public class Types {
   public static String[] getParameterTypeDesc(String desc) {
      if (desc.charAt(0) != '(') {
         throw new DexException("not a validate Method Desc %s", new Object[]{desc});
      } else {
         int x = desc.lastIndexOf(41);
         if (x < 0) {
            throw new DexException("not a validate Method Desc %s", new Object[]{desc});
         } else {
            List<String> ps = listDesc(desc.substring(1, x - 1));
            return (String[])ps.toArray(new String[ps.size()]);
         }
      }
   }

   public static String getReturnTypeDesc(String desc) {
      int x = desc.lastIndexOf(41);
      if (x < 0) {
         throw new DexException("not a validate Method Desc %s", new Object[]{desc});
      } else {
         return desc.substring(x + 1);
      }
   }

   public static List<String> listDesc(String desc) {
      List<String> list = new ArrayList(5);
      if (desc == null) {
         return list;
      } else {
         char[] chars = desc.toCharArray();
         int i = 0;

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
               break;
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
               throw new RuntimeException("can't parse type list: " + desc);
            case 'L':
               for(count = 1; chars[i + count] != ';'; ++count) {
               }

               ++count;
               list.add(new String(chars, i, count));
               i += count;
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
               i += count;
            }
         }

         return list;
      }
   }

   public static Object[] buildDexStyleSignature(String signature) {
      int rawLength = signature.length();
      ArrayList<String> pieces = new ArrayList(20);

      int endAt;
      for(int at = 0; at < rawLength; at = endAt) {
         char c = signature.charAt(at);
         endAt = at + 1;
         if (c == 'L') {
            while(endAt < rawLength) {
               c = signature.charAt(endAt);
               if (c == ';') {
                  ++endAt;
                  break;
               }

               if (c == '<') {
                  break;
               }

               ++endAt;
            }
         } else {
            while(endAt < rawLength) {
               c = signature.charAt(endAt);
               if (c == 'L') {
                  break;
               }

               ++endAt;
            }
         }

         pieces.add(signature.substring(at, endAt));
      }

      return pieces.toArray(new Object[pieces.size()]);
   }
}
