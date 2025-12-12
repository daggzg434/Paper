package com.googlecode.d2j.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class LdcOptimizeAdapter extends MethodVisitor implements Opcodes {
   public LdcOptimizeAdapter(MethodVisitor mv) {
      super(262144, mv);
   }

   public void visitLdcInsn(Object cst) {
      if (cst == null) {
         this.visitInsn(1);
      } else if (cst instanceof Integer) {
         int value = (Integer)cst;
         if (value >= -1 && value <= 5) {
            super.visitInsn(3 + value);
         } else if (value <= 127 && value >= -128) {
            super.visitIntInsn(16, value);
         } else if (value <= 32767 && value >= -32768) {
            super.visitIntInsn(17, value);
         } else {
            super.visitLdcInsn(cst);
         }
      } else if (cst instanceof Long) {
         long value = (Long)cst;
         if (value != 0L && value != 1L) {
            super.visitLdcInsn(cst);
         } else {
            super.visitInsn(9 + (int)value);
         }
      } else if (cst instanceof Float) {
         float value = (Float)cst;
         if (value == 0.0F) {
            super.visitInsn(11);
         } else if (value == 1.0F) {
            super.visitInsn(12);
         } else if (value == 2.0F) {
            super.visitInsn(13);
         } else {
            super.visitLdcInsn(cst);
         }
      } else if (cst instanceof Double) {
         double value = (Double)cst;
         if (value == 0.0D) {
            super.visitInsn(14);
         } else if (value == 1.0D) {
            super.visitInsn(15);
         } else {
            super.visitLdcInsn(cst);
         }
      } else if (cst instanceof Type) {
         Type t = (Type)cst;
         switch(t.getSort()) {
         case 1:
            super.visitFieldInsn(178, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
            break;
         case 2:
            super.visitFieldInsn(178, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
            break;
         case 3:
            super.visitFieldInsn(178, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
            break;
         case 4:
            super.visitFieldInsn(178, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
            break;
         case 5:
            super.visitFieldInsn(178, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            break;
         case 6:
            super.visitFieldInsn(178, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
            break;
         case 7:
            super.visitFieldInsn(178, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
            break;
         case 8:
            super.visitFieldInsn(178, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
            break;
         default:
            super.visitLdcInsn(cst);
         }
      } else {
         super.visitLdcInsn(cst);
      }

   }

   public static MethodVisitor wrap(MethodVisitor mv) {
      return mv == null ? null : new LdcOptimizeAdapter(mv);
   }

   public static ClassVisitor wrap(ClassVisitor cv) {
      return cv == null ? null : new ClassVisitor(262144, cv) {
         public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return LdcOptimizeAdapter.wrap(super.visitMethod(access, name, desc, signature, exceptions));
         }
      };
   }
}
