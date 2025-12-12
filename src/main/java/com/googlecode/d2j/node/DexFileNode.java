package com.googlecode.d2j.node;

import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexFileNode extends DexFileVisitor {
   public List<DexClassNode> clzs = new ArrayList();
   public int dexVersion = 3158837;

   public void visitDexFileVersion(int version) {
      this.dexVersion = version;
      super.visitDexFileVersion(version);
   }

   public DexClassVisitor visit(int access_flags, String className, String superClass, String[] interfaceNames) {
      DexClassNode cn = new DexClassNode(access_flags, className, superClass, interfaceNames);
      this.clzs.add(cn);
      return cn;
   }

   public void accept(DexClassVisitor dcv) {
      Iterator var2 = this.clzs.iterator();

      while(var2.hasNext()) {
         DexClassNode cn = (DexClassNode)var2.next();
         cn.accept(dcv);
      }

   }

   public void accept(DexFileVisitor dfv) {
      Iterator var2 = this.clzs.iterator();

      while(var2.hasNext()) {
         DexClassNode cn = (DexClassNode)var2.next();
         cn.accept(dfv);
      }

   }
}
