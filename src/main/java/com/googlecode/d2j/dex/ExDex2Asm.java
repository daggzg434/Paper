package com.googlecode.d2j.dex;

import com.googlecode.d2j.DexException;
import com.googlecode.d2j.node.DexMethodNode;
import org.objectweb.asm.AsmBridge;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

public class ExDex2Asm extends Dex2Asm {
   protected final DexExceptionHandler exceptionHandler;

   public ExDex2Asm(DexExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
   }

   public void convertCode(DexMethodNode methodNode, MethodVisitor mv) {
      MethodVisitor mw = AsmBridge.searchMethodWriter(mv);
      MethodNode mn = new MethodNode(327680, methodNode.access, methodNode.method.getName(), methodNode.method.getDesc(), (String)null, (String[])null);

      try {
         super.convertCode(methodNode, mn);
      } catch (Exception var7) {
         if (this.exceptionHandler == null) {
            throw new DexException(var7, "fail convert code for %s", new Object[]{methodNode.method});
         }

         mn.instructions.clear();
         mn.tryCatchBlocks.clear();
         this.exceptionHandler.handleMethodTranslateException(methodNode.method, methodNode, mn, var7);
      }

      mn.accept(mv);
      if (mw != null) {
         try {
            AsmBridge.sizeOfMethodWriter(mw);
         } catch (Exception var6) {
            mn.instructions.clear();
            mn.tryCatchBlocks.clear();
            this.exceptionHandler.handleMethodTranslateException(methodNode.method, methodNode, mn, var6);
            AsmBridge.replaceMethodWriter(mw, mn);
         }
      }

   }
}
