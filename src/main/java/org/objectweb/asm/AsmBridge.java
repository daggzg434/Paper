package org.objectweb.asm;

import org.objectweb.asm.tree.MethodNode;

public class AsmBridge {
   public static MethodVisitor searchMethodWriter(MethodVisitor mv) {
      while(mv != null && !(mv instanceof MethodWriter)) {
         mv = mv.mv;
      }

      return mv;
   }

   public static int sizeOfMethodWriter(MethodVisitor mv) {
      MethodWriter mw = (MethodWriter)mv;
      return mw.getSize();
   }

   private static void removeMethodWriter(MethodWriter mw) {
      ClassWriter cw = mw.cw;
      MethodWriter p = cw.firstMethod;
      if (p == mw) {
         cw.firstMethod = null;
         if (cw.lastMethod == mw) {
            cw.lastMethod = null;
         }
      } else {
         while(p != null) {
            if (p.mv == mw) {
               p.mv = mw.mv;
               if (cw.lastMethod == mw) {
                  cw.lastMethod = p;
               }
               break;
            }

            p = (MethodWriter)p.mv;
         }
      }

   }

   public static void replaceMethodWriter(MethodVisitor mv, MethodNode mn) {
      MethodWriter mw = (MethodWriter)mv;
      ClassWriter cw = mw.cw;
      mn.accept(cw);
      removeMethodWriter(mw);
   }
}
