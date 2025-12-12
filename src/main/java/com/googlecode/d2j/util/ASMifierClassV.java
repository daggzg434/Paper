package com.googlecode.d2j.util;

import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ASMifierClassV extends DexClassVisitor {
   protected ArrayOut out = new ArrayOut();
   private List<ArrayOut> methodOuts = new ArrayList();
   private List<ArrayOut> fieldOuts = new ArrayList();
   int fCount = 0;
   int mCount = 0;

   public ASMifierClassV(String pkgName, String javaClassName, int access_flags, String className, String superClass, String[] interfaceNames) {
      this.out.s("package %s;", pkgName);
      this.out.s("import com.googlecode.d2j.*;");
      this.out.s("import com.googlecode.d2j.visitors.*;");
      this.out.s("import static com.googlecode.d2j.DexConstants.*;");
      this.out.s("import static com.googlecode.d2j.reader.Op.*;");
      this.out.s("public class %s {", javaClassName);
      this.out.push();
      this.out.s("public static void accept(DexFileVisitor v) {");
      this.out.push();
      this.out.s("DexClassVisitor cv=v.visit(%s,%s,%s,%s);", Escape.classAcc(access_flags), Escape.v(className), Escape.v(superClass), Escape.v(interfaceNames));
      this.out.s("if(cv!=null) {");
      this.out.push();
      this.out.s("accept(cv);");
      this.out.s("cv.visitEnd();");
      this.out.pop();
      this.out.s("}");
      this.out.pop();
      this.out.s("}");
      this.out.s("public static void accept(DexClassVisitor cv) {");
      this.out.push();
   }

   public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
      return new ASMifierAnnotationV("cv", this.out, name, visibility);
   }

   public void visitSource(String file) {
      this.out.s("cv.visitSource(\"%s\");", Utf8Utils.escapeString(file));
   }

   public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
      String fieldName = String.format("f%03d_%s", this.fCount++, field.getName());
      this.out.s("%s(cv);", fieldName);
      final ArrayOut f = new ArrayOut();
      this.fieldOuts.add(f);
      f.s("public static void %s(DexClassVisitor cv) {", fieldName);
      f.push();
      f.s("DexFieldVisitor fv=cv.visitField(%s, %s, %s);", Escape.fieldAcc(accessFlags), Escape.v(field), Escape.v(value));
      f.s("if(fv != null) {");
      f.push();
      return new DexFieldVisitor() {
         public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
            return new ASMifierAnnotationV("fv", f, name, visibility);
         }

         public void visitEnd() {
            f.s("fv.visitEnd();");
            f.pop();
            f.s("}");
            f.pop();
            f.s("}");
         }
      };
   }

   public DexMethodVisitor visitMethod(int accessFlags, Method method) {
      String methodName = String.format("m%03d_%s", this.mCount++, method.getName().replace('<', '_').replace('>', '_'));
      this.out.s("%s(cv);", methodName);
      final ArrayOut m = new ArrayOut();
      this.methodOuts.add(m);
      m.s("public static void %s(DexClassVisitor cv) {", methodName);
      m.push();
      m.s("DexMethodVisitor mv=cv.visitMethod(%s, %s);", Escape.methodAcc(accessFlags), Escape.v(method));
      m.s("if(mv != null) {");
      m.push();
      return new DexMethodVisitor() {
         public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
            return new ASMifierAnnotationV("mv", m, name, visibility);
         }

         public DexAnnotationAble visitParameterAnnotation(final int index) {
            m.s("DexAnnotationAble pv%02d = mv.visitParameterAnnotation(%s);", index, index);
            return new DexAnnotationAble() {
               public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
                  return new ASMifierAnnotationV(String.format("pv%02d", index), m, name, visibility);
               }
            };
         }

         public DexCodeVisitor visitCode() {
            m.s("DexCodeVisitor code=mv.visitCode();");
            m.s("if(code != null) {");
            m.push();
            return new ASMifierCodeV(m) {
               public void visitEnd() {
                  super.visitEnd();
                  this.m.pop();
                  this.m.s("}");
               }
            };
         }

         public void visitEnd() {
            m.s("mv.visitEnd();");
            m.pop();
            m.s("}");
            m.pop();
            m.s("}");
         }
      };
   }

   public void visitEnd() {
      this.out.pop();
      this.out.s("}");
      Iterator var1 = this.fieldOuts.iterator();

      ArrayOut o;
      Iterator var3;
      int i;
      while(var1.hasNext()) {
         o = (ArrayOut)var1.next();
         this.out.array.addAll(o.array);
         var3 = o.is.iterator();

         while(var3.hasNext()) {
            i = (Integer)var3.next();
            this.out.is.add(this.out.i + i);
         }
      }

      this.fieldOuts = null;
      var1 = this.methodOuts.iterator();

      while(var1.hasNext()) {
         o = (ArrayOut)var1.next();
         this.out.array.addAll(o.array);
         var3 = o.is.iterator();

         while(var3.hasNext()) {
            i = (Integer)var3.next();
            this.out.is.add(this.out.i + i);
         }
      }

      this.methodOuts = null;
      this.out.pop();
      this.out.s("}");
   }
}
