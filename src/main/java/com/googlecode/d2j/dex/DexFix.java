package com.googlecode.d2j.dex;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFieldNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DexFix {
   private static final int ACC_STATIC_FINAL = 24;

   public static void fixStaticFinalFieldValue(final DexFileNode dex) {
      if (dex.clzs != null) {
         Iterator var1 = dex.clzs.iterator();

         while(var1.hasNext()) {
            DexClassNode classNode = (DexClassNode)var1.next();
            fixStaticFinalFieldValue(classNode);
         }
      }

   }

   public static void fixStaticFinalFieldValue(final DexClassNode classNode) {
      if (classNode.fields != null) {
         final Map<String, DexFieldNode> fs = new HashMap();
         final Map<String, DexFieldNode> shouldNotBeAssigned = new HashMap();
         Iterator var3 = classNode.fields.iterator();

         while(var3.hasNext()) {
            DexFieldNode fn = (DexFieldNode)var3.next();
            if ((fn.access & 24) == 24) {
               if (fn.cst == null) {
                  char t = fn.field.getType().charAt(0);
                  if (t != 'L' && t != '[') {
                     fs.put(fn.field.getName() + ":" + fn.field.getType(), fn);
                  }
               } else if (isPrimitiveZero(fn.field.getType(), fn.cst)) {
                  shouldNotBeAssigned.put(fn.field.getName() + ":" + fn.field.getType(), fn);
               }
            }
         }

         if (!fs.isEmpty() || !shouldNotBeAssigned.isEmpty()) {
            DexMethodNode node = null;
            Iterator var7;
            if (classNode.methods != null) {
               var7 = classNode.methods.iterator();

               while(var7.hasNext()) {
                  DexMethodNode mn = (DexMethodNode)var7.next();
                  if (mn.method.getName().equals("<clinit>")) {
                     node = mn;
                     break;
                  }
               }
            }

            if (node != null) {
               if (node.codeNode == null) {
                  return;
               }

               node.codeNode.accept(new DexCodeVisitor() {
                  public void visitFieldStmt(Op op, int a, int b, Field field) {
                     switch(op) {
                     case SPUT:
                     case SPUT_BOOLEAN:
                     case SPUT_BYTE:
                     case SPUT_CHAR:
                     case SPUT_OBJECT:
                     case SPUT_SHORT:
                     case SPUT_WIDE:
                        if (field.getOwner().equals(classNode.className)) {
                           String key = field.getName() + ":" + field.getType();
                           fs.remove(key);
                           DexFieldNode dn = (DexFieldNode)shouldNotBeAssigned.get(key);
                           if (dn != null) {
                              dn.cst = null;
                           }
                        }
                     default:
                     }
                  }
               });
            }

            DexFieldNode fn;
            for(var7 = fs.values().iterator(); var7.hasNext(); fn.cst = getDefaultValueOfType(fn.field.getType().charAt(0))) {
               fn = (DexFieldNode)var7.next();
            }

         }
      }
   }

   private static Object getDefaultValueOfType(char t) {
      switch(t) {
      case 'B':
         return 0;
      case 'C':
         return '\u0000';
      case 'D':
         return 0.0D;
      case 'E':
      case 'G':
      case 'H':
      case 'K':
      case 'L':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      case '[':
      default:
         return null;
      case 'F':
         return 0.0F;
      case 'I':
         return 0;
      case 'J':
         return 0L;
      case 'S':
         return Short.valueOf((short)0);
      case 'Z':
         return Boolean.FALSE;
      }
   }

   static boolean isPrimitiveZero(String desc, Object value) {
      if (value != null && desc != null && desc.length() > 0) {
         switch(desc.charAt(0)) {
         case 'B':
            return (Byte)value == 0;
         case 'C':
            return (Character)value == 0;
         case 'D':
            return (Double)value == 0.0D;
         case 'E':
         case 'G':
         case 'H':
         case 'K':
         case 'L':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'T':
         case 'U':
         case 'V':
         case 'W':
         case 'X':
         case 'Y':
         default:
            break;
         case 'F':
            return (Float)value == 0.0F;
         case 'I':
            return (Integer)value == 0;
         case 'J':
            return (Long)value == 0L;
         case 'S':
            return (Short)value == 0;
         case 'Z':
            return !(Boolean)value;
         }
      }

      return false;
   }
}
