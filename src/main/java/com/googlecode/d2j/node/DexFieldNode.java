package com.googlecode.d2j.node;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexFieldNode extends DexFieldVisitor {
   public int access;
   public List<DexAnnotationNode> anns;
   public Object cst;
   public Field field;

   public DexFieldNode(DexFieldVisitor visitor, int access, Field field, Object cst) {
      super(visitor);
      this.access = access;
      this.field = field;
      this.cst = cst;
   }

   public DexFieldNode(int access, Field field, Object cst) {
      this.access = access;
      this.field = field;
      this.cst = cst;
   }

   public void accept(DexClassVisitor dcv) {
      DexFieldVisitor fv = dcv.visitField(this.access, this.field, this.cst);
      if (fv != null) {
         this.accept(fv);
         fv.visitEnd();
      }

   }

   public void accept(DexFieldVisitor fv) {
      if (this.anns != null) {
         Iterator var2 = this.anns.iterator();

         while(var2.hasNext()) {
            DexAnnotationNode ann = (DexAnnotationNode)var2.next();
            ann.accept(fv);
         }
      }

   }

   public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
      if (this.anns == null) {
         this.anns = new ArrayList(5);
      }

      DexAnnotationNode annotation = new DexAnnotationNode(name, visibility);
      this.anns.add(annotation);
      return annotation;
   }
}
