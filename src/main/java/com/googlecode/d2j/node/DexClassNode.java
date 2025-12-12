package com.googlecode.d2j.node;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexClassNode extends DexClassVisitor {
   public int access;
   public List<DexAnnotationNode> anns;
   public String className;
   public List<DexFieldNode> fields;
   public String[] interfaceNames;
   public List<DexMethodNode> methods;
   public String source;
   public String superClass;

   public DexClassNode(DexClassVisitor v, int access, String className, String superClass, String[] interfaceNames) {
      super(v);
      this.access = access;
      this.className = className;
      this.superClass = superClass;
      this.interfaceNames = interfaceNames;
   }

   public DexClassNode(int access, String className, String superClass, String[] interfaceNames) {
      this.access = access;
      this.className = className;
      this.superClass = superClass;
      this.interfaceNames = interfaceNames;
   }

   public void accept(DexClassVisitor dcv) {
      Iterator var2;
      if (this.anns != null) {
         var2 = this.anns.iterator();

         while(var2.hasNext()) {
            DexAnnotationNode ann = (DexAnnotationNode)var2.next();
            ann.accept(dcv);
         }
      }

      if (this.methods != null) {
         var2 = this.methods.iterator();

         while(var2.hasNext()) {
            DexMethodNode m = (DexMethodNode)var2.next();
            m.accept(dcv);
         }
      }

      if (this.fields != null) {
         var2 = this.fields.iterator();

         while(var2.hasNext()) {
            DexFieldNode f = (DexFieldNode)var2.next();
            f.accept(dcv);
         }
      }

      if (this.source != null) {
         dcv.visitSource(this.source);
      }

   }

   public void accept(DexFileVisitor dfv) {
      DexClassVisitor dcv = dfv.visit(this.access, this.className, this.superClass, this.interfaceNames);
      if (dcv != null) {
         this.accept(dcv);
         dcv.visitEnd();
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

   public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
      if (this.fields == null) {
         this.fields = new ArrayList();
      }

      DexFieldNode fieldNode = new DexFieldNode(accessFlags, field, value);
      this.fields.add(fieldNode);
      return fieldNode;
   }

   public DexMethodVisitor visitMethod(int accessFlags, Method method) {
      if (this.methods == null) {
         this.methods = new ArrayList();
      }

      DexMethodNode methodNode = new DexMethodNode(accessFlags, method);
      this.methods.add(methodNode);
      return methodNode;
   }

   public void visitSource(String file) {
      this.source = file;
   }
}
