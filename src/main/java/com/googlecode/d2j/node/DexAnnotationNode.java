package com.googlecode.d2j.node;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexAnnotationNode extends DexAnnotationVisitor {
   public List<DexAnnotationNode.Item> items = new ArrayList(5);
   public String type;
   public Visibility visibility;

   public static void acceptAnnotationItem(DexAnnotationVisitor dav, String name, Object o) {
      if (o instanceof Object[]) {
         DexAnnotationVisitor arrayVisitor = dav.visitArray(name);
         if (arrayVisitor != null) {
            Object[] array = (Object[])((Object[])o);
            Object[] var5 = array;
            int var6 = array.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Object e = var5[var7];
               acceptAnnotationItem(arrayVisitor, (String)null, e);
            }

            arrayVisitor.visitEnd();
         }
      } else if (o instanceof DexAnnotationNode) {
         DexAnnotationNode ann = (DexAnnotationNode)o;
         DexAnnotationVisitor av = dav.visitAnnotation(name, ann.type);
         if (av != null) {
            Iterator var12 = ann.items.iterator();

            while(var12.hasNext()) {
               DexAnnotationNode.Item item = (DexAnnotationNode.Item)var12.next();
               acceptAnnotationItem(av, item.name, item.value);
            }

            av.visitEnd();
         }
      } else if (o instanceof Field) {
         Field f = (Field)o;
         dav.visitEnum(name, f.getType(), f.getName());
      } else {
         dav.visit(name, o);
      }

   }

   public DexAnnotationNode(String type, Visibility visibility) {
      this.type = type;
      this.visibility = visibility;
   }

   public void accept(DexAnnotationAble av) {
      DexAnnotationVisitor av1 = av.visitAnnotation(this.type, this.visibility);
      if (av1 != null) {
         Iterator var3 = this.items.iterator();

         while(var3.hasNext()) {
            DexAnnotationNode.Item item = (DexAnnotationNode.Item)var3.next();
            acceptAnnotationItem(av1, item.name, item.value);
         }

         av1.visitEnd();
      }

   }

   public void visit(String name, Object value) {
      this.items.add(new DexAnnotationNode.Item(name, value));
   }

   public DexAnnotationVisitor visitAnnotation(String name, String desc) {
      DexAnnotationNode annotation = new DexAnnotationNode(desc, Visibility.RUNTIME);
      this.items.add(new DexAnnotationNode.Item(name, annotation));
      return annotation;
   }

   public DexAnnotationVisitor visitArray(final String name) {
      return new DexAnnotationNode.AV() {
         public void visitEnd() {
            DexAnnotationNode.this.items.add(new DexAnnotationNode.Item(name, this.objs.toArray()));
            super.visitEnd();
         }
      };
   }

   public void visitEnum(String name, String desc, String value) {
      this.items.add(new DexAnnotationNode.Item(name, new Field(desc, value, desc)));
   }

   public static class Item {
      public String name;
      public Object value;

      public Item(String name, Object value) {
         this.name = name;
         this.value = value;
      }
   }

   private abstract static class AV extends DexAnnotationVisitor {
      List<Object> objs;

      private AV() {
         this.objs = new ArrayList();
      }

      public void visit(String name, Object value) {
         this.objs.add(value);
      }

      public DexAnnotationVisitor visitAnnotation(String name, String desc) {
         DexAnnotationNode annotation = new DexAnnotationNode(desc, Visibility.RUNTIME);
         this.objs.add(annotation);
         return annotation;
      }

      public DexAnnotationVisitor visitArray(String name) {
         return new DexAnnotationNode.AV() {
            public void visitEnd() {
               AV.this.objs.add(this.objs.toArray());
               super.visitEnd();
            }
         };
      }

      public void visitEnd() {
         this.objs = null;
         super.visitEnd();
      }

      public void visitEnum(String name, String desc, String value) {
         this.objs.add(new Field((String)null, value, desc));
      }

      // $FF: synthetic method
      AV(Object x0) {
         this();
      }
   }
}
