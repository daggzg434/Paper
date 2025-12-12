package com.googlecode.d2j.dex;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

public class LambadaNameSafeClassAdapter extends RemappingClassAdapter {
   public String getClassName() {
      return this.remapper.mapType(this.className);
   }

   public LambadaNameSafeClassAdapter(ClassVisitor cv) {
      super(cv, new Remapper() {
         public String mapType(String type) {
            return type == null ? null : type.replace('-', '_');
         }
      });
   }
}
