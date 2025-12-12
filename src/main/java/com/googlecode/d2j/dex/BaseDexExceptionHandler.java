package com.googlecode.d2j.dex;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexMethodNode;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.objectweb.asm.MethodVisitor;

public class BaseDexExceptionHandler implements DexExceptionHandler {
   public void handleFileException(Exception e) {
      e.printStackTrace(System.err);
   }

   public void handleMethodTranslateException(Method method, DexMethodNode methodNode, MethodVisitor mv, Exception e) {
      StringWriter s = new StringWriter();
      s.append("d2j fail translate: ");
      e.printStackTrace(new PrintWriter(s));
      String msg = s.toString();
      mv.visitTypeInsn(187, "java/lang/RuntimeException");
      mv.visitInsn(89);
      mv.visitLdcInsn(msg);
      mv.visitMethodInsn(183, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V");
      mv.visitInsn(191);
   }
}
