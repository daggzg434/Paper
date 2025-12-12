package com.googlecode.d2j.util;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;

public class ASMifierAnnotationV extends DexAnnotationVisitor implements DexConstants {
   ArrayOut out;
   int i = 0;

   public ASMifierAnnotationV(String objName, ArrayOut out, String name, Visibility visibility) {
      this.out = out;
      out.s("if(%s!=null){", objName);
      out.push();
      out.s("DexAnnotationVisitor av%02d = %s.visitAnnotation(%s, Visibility.%s);", this.i, objName, Escape.v(name), visibility.name());
      out.s("if(av%02d != null) {", this.i);
      out.push();
   }

   public void visit(String name, Object value) {
      this.out.s("av%02d.visit(%s, %s);", this.i, Escape.v(name), Escape.v(value));
   }

   public void visitEnum(String name, String desc, String value) {
      this.out.s("av%02d.visitEnum(%s, %s, %s);", this.i, Escape.v(name), Escape.v(desc), Escape.v(value));
   }

   public DexAnnotationVisitor visitAnnotation(String name, String desc) {
      this.out.s("{");
      this.out.push();
      int old = this.i;
      int n = ++this.i;
      this.out.s("DexAnnotationVisitor av%02d = av%02d.visitAnnotation(%s, %s);", n, old, Escape.v(name), Escape.v(desc));
      this.out.s("if(av%02d != null) {", this.i);
      this.out.push();
      return this;
   }

   public DexAnnotationVisitor visitArray(String name) {
      this.out.s("{");
      this.out.push();
      int old = this.i;
      int n = ++this.i;
      this.out.s("DexAnnotationVisitor av%02d = av%02d.visitArray(%s);", n, old, Escape.v(name));
      this.out.s("if(av%02d != null) {", this.i);
      this.out.push();
      return this;
   }

   public void visitEnd() {
      this.out.s("av%02d.visitEnd();", this.i);
      --this.i;
      this.out.pop();
      this.out.s("}");
      this.out.pop();
      this.out.s("}");
   }
}
