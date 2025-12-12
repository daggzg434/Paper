package com.googlecode.d2j.node;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexMethodNode extends DexMethodVisitor {
   public int access;
   public List<DexAnnotationNode> anns;
   public DexCodeNode codeNode;
   public Method method;
   public List<DexAnnotationNode>[] parameterAnns;

   public DexMethodNode(DexMethodVisitor mv, int access, Method method) {
      super(mv);
      this.access = access;
      this.method = method;
   }

   public DexMethodNode(int access, Method method) {
      this.access = access;
      this.method = method;
   }

   public void accept(DexClassVisitor dcv) {
      DexMethodVisitor mv = dcv.visitMethod(this.access, this.method);
      if (mv != null) {
         this.accept(mv);
         mv.visitEnd();
      }

   }

   public void accept(DexMethodVisitor mv) {
      if (this.anns != null) {
         Iterator var2 = this.anns.iterator();

         while(var2.hasNext()) {
            DexAnnotationNode ann = (DexAnnotationNode)var2.next();
            ann.accept(mv);
         }
      }

      if (this.parameterAnns != null) {
         for(int i = 0; i < this.parameterAnns.length; ++i) {
            List<DexAnnotationNode> ps = this.parameterAnns[i];
            if (ps != null) {
               DexAnnotationAble av = mv.visitParameterAnnotation(i);
               if (av != null) {
                  Iterator var5 = ps.iterator();

                  while(var5.hasNext()) {
                     DexAnnotationNode p = (DexAnnotationNode)var5.next();
                     p.accept(av);
                  }
               }
            }
         }
      }

      if (this.codeNode != null) {
         this.codeNode.accept(mv);
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

   public DexCodeVisitor visitCode() {
      DexCodeNode codeNode = new DexCodeNode(super.visitCode());
      this.codeNode = codeNode;
      return codeNode;
   }

   public DexAnnotationAble visitParameterAnnotation(final int index) {
      if (this.parameterAnns == null) {
         this.parameterAnns = new List[this.method.getParameterTypes().length];
      }

      return new DexAnnotationAble() {
         public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
            List<DexAnnotationNode> pas = DexMethodNode.this.parameterAnns[index];
            if (pas == null) {
               pas = new ArrayList(5);
               DexMethodNode.this.parameterAnns[index] = (List)pas;
            }

            DexAnnotationNode annotation = new DexAnnotationNode(name, visibility);
            ((List)pas).add(annotation);
            return annotation;
         }
      };
   }
}
