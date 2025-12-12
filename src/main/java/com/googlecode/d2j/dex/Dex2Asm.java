package com.googlecode.d2j.dex;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.converter.Dex2IRConverter;
import com.googlecode.d2j.converter.IR2JConverter;
import com.googlecode.d2j.node.DexAnnotationNode;
import com.googlecode.d2j.node.DexClassNode;
import com.googlecode.d2j.node.DexFieldNode;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.ts.AggTransformer;
import com.googlecode.dex2jar.ir.ts.CleanLabel;
import com.googlecode.dex2jar.ir.ts.DeadCodeTransformer;
import com.googlecode.dex2jar.ir.ts.EndRemover;
import com.googlecode.dex2jar.ir.ts.ExceptionHandlerTrim;
import com.googlecode.dex2jar.ir.ts.Ir2JRegAssignTransformer;
import com.googlecode.dex2jar.ir.ts.MultiArrayTransformer;
import com.googlecode.dex2jar.ir.ts.NewTransformer;
import com.googlecode.dex2jar.ir.ts.NpeTransformer;
import com.googlecode.dex2jar.ir.ts.RemoveConstantFromSSA;
import com.googlecode.dex2jar.ir.ts.RemoveLocalFromSSA;
import com.googlecode.dex2jar.ir.ts.TypeTransformer;
import com.googlecode.dex2jar.ir.ts.UnSSATransformer;
import com.googlecode.dex2jar.ir.ts.VoidInvokeTransformer;
import com.googlecode.dex2jar.ir.ts.ZeroTransformer;
import com.googlecode.dex2jar.ir.ts.array.FillArrayTransformer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InnerClassNode;

public class Dex2Asm {
   protected static final int ACC_INTERFACE_ABSTRACT = 1536;
   private static final int NO_CODE_MASK = 9472;
   protected static final CleanLabel T_cleanLabel = new CleanLabel();
   protected static final EndRemover T_endRemove = new EndRemover();
   protected static final Ir2JRegAssignTransformer T_ir2jRegAssign = new Ir2JRegAssignTransformer();
   protected static final NewTransformer T_new = new NewTransformer();
   protected static final RemoveConstantFromSSA T_removeConst = new RemoveConstantFromSSA();
   protected static final RemoveLocalFromSSA T_removeLocal = new RemoveLocalFromSSA();
   protected static final ExceptionHandlerTrim T_trimEx = new ExceptionHandlerTrim();
   protected static final TypeTransformer T_type = new TypeTransformer();
   protected static final DeadCodeTransformer T_deadCode = new DeadCodeTransformer();
   protected static final FillArrayTransformer T_fillArray = new FillArrayTransformer();
   protected static final AggTransformer T_agg = new AggTransformer();
   protected static final UnSSATransformer T_unssa = new UnSSATransformer();
   protected static final ZeroTransformer T_zero = new ZeroTransformer();
   protected static final VoidInvokeTransformer T_voidInvoke = new VoidInvokeTransformer();
   protected static final NpeTransformer T_npe = new NpeTransformer();
   protected static final MultiArrayTransformer T_multiArray = new MultiArrayTransformer();
   private static final Comparator<InnerClassNode> INNER_CLASS_NODE_COMPARATOR = new Comparator<InnerClassNode>() {
      public int compare(InnerClassNode o1, InnerClassNode o2) {
         return o1.name.compareTo(o2.name);
      }
   };

   private static int clearClassAccess(boolean isInner, int access) {
      if ((access & 512) == 0) {
         access |= 32;
      }

      access &= -11;
      if (isInner && (access & 4) != 0) {
         access &= -5;
         access |= 1;
      }

      access &= -131073;
      return access;
   }

   private static int clearInnerAccess(int access) {
      access &= -33;
      if (0 != (access & 2)) {
         access &= -6;
      } else if (0 != (access & 4)) {
         access &= -2;
      }

      return access;
   }

   protected static String toInternalName(DexType type) {
      return toInternalName(type.desc);
   }

   protected static String toInternalName(String desc) {
      return Type.getType(desc).getInternalName();
   }

   public static void accept(DexAnnotationNode ann, ClassVisitor v) {
      AnnotationVisitor av = v.visitAnnotation(ann.type, ann.visibility != Visibility.BUILD);
      if (av != null) {
         accept(ann.items, av);
         av.visitEnd();
      }

   }

   public static void accept(List<DexAnnotationNode> anns, ClassVisitor cv) {
      if (anns != null) {
         Iterator var2 = anns.iterator();

         while(var2.hasNext()) {
            DexAnnotationNode ann = (DexAnnotationNode)var2.next();
            if (ann.visibility != Visibility.SYSTEM) {
               accept(ann, cv);
            }
         }
      }

   }

   public static void accept(List<DexAnnotationNode> anns, FieldVisitor fv) {
      if (anns != null) {
         Iterator var2 = anns.iterator();

         while(var2.hasNext()) {
            DexAnnotationNode ann = (DexAnnotationNode)var2.next();
            if (ann.visibility != Visibility.SYSTEM) {
               accept(ann, fv);
            }
         }
      }

   }

   public static void accept(List<DexAnnotationNode> anns, MethodVisitor mv) {
      if (anns != null) {
         Iterator var2 = anns.iterator();

         while(var2.hasNext()) {
            DexAnnotationNode ann = (DexAnnotationNode)var2.next();
            if (ann.visibility != Visibility.SYSTEM) {
               accept(ann, mv);
            }
         }
      }

   }

   public static void accept(DexAnnotationNode ann, MethodVisitor v) {
      AnnotationVisitor av = v.visitAnnotation(ann.type, ann.visibility != Visibility.BUILD);
      if (av != null) {
         accept(ann.items, av);
         av.visitEnd();
      }

   }

   public static void acceptParameter(DexAnnotationNode ann, int index, MethodVisitor v) {
      AnnotationVisitor av = v.visitParameterAnnotation(index, ann.type, ann.visibility != Visibility.BUILD);
      if (av != null) {
         accept(ann.items, av);
         av.visitEnd();
      }

   }

   public static void accept(DexAnnotationNode ann, FieldVisitor v) {
      AnnotationVisitor av = v.visitAnnotation(ann.type, ann.visibility != Visibility.BUILD);
      if (av != null) {
         accept(ann.items, av);
         av.visitEnd();
      }

   }

   public static void accept(List<DexAnnotationNode.Item> items, AnnotationVisitor av) {
      Iterator var2 = items.iterator();

      while(var2.hasNext()) {
         DexAnnotationNode.Item item = (DexAnnotationNode.Item)var2.next();
         accept(av, item.name, item.value);
      }

   }

   private static void accept(AnnotationVisitor dav, String name, Object o) {
      if (o instanceof Object[]) {
         AnnotationVisitor arrayVisitor = dav.visitArray(name);
         if (arrayVisitor != null) {
            Object[] array = (Object[])((Object[])o);
            Object[] var5 = array;
            int var6 = array.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Object e = var5[var7];
               accept(arrayVisitor, (String)null, e);
            }

            arrayVisitor.visitEnd();
         }
      } else if (o instanceof DexAnnotationNode) {
         DexAnnotationNode ann = (DexAnnotationNode)o;
         AnnotationVisitor av = dav.visitAnnotation(name, ann.type);
         if (av != null) {
            Iterator var12 = ann.items.iterator();

            while(var12.hasNext()) {
               DexAnnotationNode.Item item = (DexAnnotationNode.Item)var12.next();
               accept(av, item.name, item.value);
            }

            av.visitEnd();
         }
      } else if (o instanceof Field) {
         Field f = (Field)o;
         dav.visitEnum(name, f.getType(), f.getName());
      } else if (o instanceof DexType) {
         dav.visit(name, Type.getType(((DexType)o).desc));
      } else if (o instanceof Method) {
         System.err.println("WARN: ignored method annotation value");
      } else if (o == null) {
         System.err.println("WARN: ignored null annotation value");
      } else {
         dav.visit(name, o);
      }

   }

   private static MethodVisitor collectBasicMethodInfo(DexMethodNode methodNode, ClassVisitor cv) {
      String[] xthrows = null;
      String signature = null;
      if (methodNode.anns != null) {
         Iterator var4 = methodNode.anns.iterator();

         label63:
         while(true) {
            Object[] strs;
            label55:
            do {
               label51:
               while(true) {
                  DexAnnotationNode ann;
                  do {
                     if (!var4.hasNext()) {
                        break label63;
                     }

                     ann = (DexAnnotationNode)var4.next();
                  } while(ann.visibility != Visibility.SYSTEM);

                  String var6 = ann.type;
                  byte var7 = -1;
                  switch(var6.hashCode()) {
                  case -858881176:
                     if (var6.equals("Ldalvik/annotation/Throws;")) {
                        var7 = 0;
                     }
                     break;
                  case 1664436329:
                     if (var6.equals("Ldalvik/annotation/Signature;")) {
                        var7 = 1;
                     }
                  }

                  switch(var7) {
                  case 0:
                     strs = (Object[])((Object[])findAnnotationAttribute(ann, "value"));
                     if (strs == null) {
                        break;
                     }

                     xthrows = new String[strs.length];
                     int i = 0;

                     while(true) {
                        if (i >= strs.length) {
                           continue label51;
                        }

                        DexType type = (DexType)strs[i];
                        xthrows[i] = toInternalName(type);
                        ++i;
                     }
                  case 1:
                     strs = (Object[])((Object[])findAnnotationAttribute(ann, "value"));
                     continue label55;
                  }
               }
            } while(strs == null);

            StringBuilder sb = new StringBuilder();
            Object[] var10 = strs;
            int var11 = strs.length;

            for(int var12 = 0; var12 < var11; ++var12) {
               Object str = var10[var12];
               sb.append(str);
            }

            signature = sb.toString();
         }
      }

      int access = methodNode.access;
      int cleanFlag = -196609;
      access &= -196609;
      return cv.visitMethod(access, methodNode.method.getName(), methodNode.method.getDesc(), signature, xthrows);
   }

   protected static Map<String, Dex2Asm.Clz> collectClzInfo(DexFileNode fileNode) {
      Map<String, Dex2Asm.Clz> classes = new HashMap();
      Iterator var2 = fileNode.clzs.iterator();

      label78:
      while(true) {
         DexClassNode classNode;
         Dex2Asm.Clz clz;
         do {
            if (!var2.hasNext()) {
               return classes;
            }

            classNode = (DexClassNode)var2.next();
            clz = get(classes, classNode.className);
            clz.access = clz.access & -1537 | classNode.access;
         } while(classNode.anns == null);

         Iterator var5 = classNode.anns.iterator();

         while(true) {
            label69:
            while(true) {
               DexAnnotationNode ann;
               do {
                  if (!var5.hasNext()) {
                     continue label78;
                  }

                  ann = (DexAnnotationNode)var5.next();
               } while(ann.visibility != Visibility.SYSTEM);

               String var7 = ann.type;
               byte var8 = -1;
               switch(var7.hashCode()) {
               case -1911645549:
                  if (var7.equals("Ldalvik/annotation/InnerClass;")) {
                     var8 = 2;
                  }
                  break;
               case -1225017687:
                  if (var7.equals("Ldalvik/annotation/EnclosingClass;")) {
                     var8 = 0;
                  }
                  break;
               case 150988917:
                  if (var7.equals("Ldalvik/annotation/MemberClasses;")) {
                     var8 = 3;
                  }
                  break;
               case 781072212:
                  if (var7.equals("Ldalvik/annotation/EnclosingMethod;")) {
                     var8 = 1;
                  }
               }

               Dex2Asm.Clz enclosingClass;
               switch(var8) {
               case 0:
                  DexType type = (DexType)findAnnotationAttribute(ann, "value");
                  enclosingClass = get(classes, type.desc);
                  clz.enclosingClass = enclosingClass;
                  enclosingClass.addInner(clz);
                  break;
               case 1:
                  Method m = (Method)findAnnotationAttribute(ann, "value");
                  enclosingClass = get(classes, m.getOwner());
                  clz.enclosingClass = enclosingClass;
                  clz.enclosingMethod = m;
                  enclosingClass.addInner(clz);
                  break;
               case 2:
                  Iterator var16 = ann.items.iterator();

                  while(true) {
                     if (!var16.hasNext()) {
                        continue label69;
                     }

                     DexAnnotationNode.Item it = (DexAnnotationNode.Item)var16.next();
                     if ("accessFlags".equals(it.name)) {
                        clz.access |= (Integer)it.value & -1537;
                     } else if ("name".equals(it.name)) {
                        clz.innerName = (String)it.value;
                     }
                  }
               case 3:
                  Object[] ts = (Object[])((Object[])findAnnotationAttribute(ann, "value"));
                  Object[] var10 = ts;
                  int var11 = ts.length;

                  for(int var12 = 0; var12 < var11; ++var12) {
                     Object v = var10[var12];
                     DexType type = (DexType)v;
                     Dex2Asm.Clz inner = get(classes, type.desc);
                     clz.addInner(inner);
                     inner.enclosingClass = clz;
                  }
               }
            }
         }
      }
   }

   public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf, DexFileNode fileNode) {
      this.convertClass(fileNode.dexVersion, classNode, cvf, collectClzInfo(fileNode));
   }

   public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf) {
      this.convertClass(3158837, classNode, cvf);
   }

   public void convertClass(int dexVersion, DexClassNode classNode, ClassVisitorFactory cvf) {
      this.convertClass(dexVersion, classNode, cvf, new HashMap());
   }

   private static boolean isJavaIdentifier(String str) {
      if (str.length() < 1) {
         return false;
      } else if (!Character.isJavaIdentifierStart(str.charAt(0))) {
         return false;
      } else {
         for(int i = 1; i < str.length(); ++i) {
            if (!Character.isJavaIdentifierPart(str.charAt(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public void convertClass(DexClassNode classNode, ClassVisitorFactory cvf, Map<String, Dex2Asm.Clz> classes) {
      this.convertClass(3158837, classNode, cvf, classes);
   }

   public void convertClass(DexFileNode dfn, DexClassNode classNode, ClassVisitorFactory cvf, Map<String, Dex2Asm.Clz> classes) {
      this.convertClass(dfn.dexVersion, classNode, cvf, classes);
   }

   public void convertClass(int dexVersion, DexClassNode classNode, ClassVisitorFactory cvf, Map<String, Dex2Asm.Clz> classes) {
      ClassVisitor cv = cvf.create(toInternalName(classNode.className));
      if (cv != null) {
         DexFix.fixStaticFinalFieldValue(classNode);
         String signature = null;
         if (classNode.anns != null) {
            Iterator var7 = classNode.anns.iterator();

            label138:
            while(true) {
               Object[] strs;
               label130:
               do {
                  while(true) {
                     DexAnnotationNode ann;
                     do {
                        if (!var7.hasNext()) {
                           break label138;
                        }

                        ann = (DexAnnotationNode)var7.next();
                     } while(ann.visibility != Visibility.SYSTEM);

                     String var9 = ann.type;
                     byte var10 = -1;
                     switch(var9.hashCode()) {
                     case 1664436329:
                        if (var9.equals("Ldalvik/annotation/Signature;")) {
                           var10 = 0;
                        }
                     }

                     switch(var10) {
                     case 0:
                        strs = (Object[])((Object[])findAnnotationAttribute(ann, "value"));
                        continue label130;
                     }
                  }
               } while(strs == null);

               StringBuilder sb = new StringBuilder();
               Object[] var13 = strs;
               int var14 = strs.length;

               for(int var15 = 0; var15 < var14; ++var15) {
                  Object str = var13[var15];
                  sb.append(str);
               }

               signature = sb.toString();
            }
         }

         String[] interfaceInterNames = null;
         if (classNode.interfaceNames != null) {
            interfaceInterNames = new String[classNode.interfaceNames.length];

            for(int i = 0; i < classNode.interfaceNames.length; ++i) {
               interfaceInterNames[i] = toInternalName(classNode.interfaceNames[i]);
            }
         }

         Dex2Asm.Clz clzInfo = (Dex2Asm.Clz)classes.get(classNode.className);
         int access = classNode.access;
         boolean isInnerClass = false;
         if (clzInfo != null) {
            isInnerClass = clzInfo.enclosingClass != null || clzInfo.enclosingMethod != null;
         }

         access = clearClassAccess(isInnerClass, access);
         int version = dexVersion >= 3158839 ? 52 : 50;
         cv.visit(version, access, toInternalName(classNode.className), signature, classNode.superClass == null ? null : toInternalName(classNode.superClass), interfaceInterNames);
         List<InnerClassNode> innerClassNodes = new ArrayList(5);
         if (clzInfo != null) {
            searchInnerClass(clzInfo, innerClassNodes, classNode.className);
         }

         if (isInnerClass) {
            if (clzInfo.innerName == null) {
               Method enclosingMethod = clzInfo.enclosingMethod;
               if (enclosingMethod != null) {
                  cv.visitOuterClass(toInternalName(enclosingMethod.getOwner()), enclosingMethod.getName(), enclosingMethod.getDesc());
               } else {
                  Dex2Asm.Clz enclosingClass = clzInfo.enclosingClass;
                  cv.visitOuterClass(toInternalName(enclosingClass.name), (String)null, (String)null);
               }
            }

            searchEnclosing(clzInfo, innerClassNodes);
         }

         Collections.sort(innerClassNodes, INNER_CLASS_NODE_COMPARATOR);

         Iterator var25;
         InnerClassNode icn;
         for(var25 = innerClassNodes.iterator(); var25.hasNext(); icn.accept(cv)) {
            icn = (InnerClassNode)var25.next();
            if (icn.innerName != null && !isJavaIdentifier(icn.innerName)) {
               icn.innerName = null;
               icn.outerName = null;
            }
         }

         accept(classNode.anns, cv);
         if (classNode.fields != null) {
            var25 = classNode.fields.iterator();

            while(var25.hasNext()) {
               DexFieldNode fieldNode = (DexFieldNode)var25.next();
               this.convertField(classNode, fieldNode, cv);
            }
         }

         if (classNode.methods != null) {
            var25 = classNode.methods.iterator();

            while(var25.hasNext()) {
               DexMethodNode methodNode = (DexMethodNode)var25.next();
               this.convertMethod(classNode, methodNode, cv);
            }
         }

         cv.visitEnd();
      }
   }

   public void convertCode(DexMethodNode methodNode, MethodVisitor mv) {
      IrMethod irMethod = this.dex2ir(methodNode);
      this.optimize(irMethod);
      this.ir2j(irMethod, mv);
   }

   public void convertDex(DexFileNode fileNode, ClassVisitorFactory cvf) {
      if (fileNode.clzs != null) {
         Map<String, Dex2Asm.Clz> classes = collectClzInfo(fileNode);
         Iterator var4 = fileNode.clzs.iterator();

         while(var4.hasNext()) {
            DexClassNode classNode = (DexClassNode)var4.next();
            this.convertClass(fileNode, classNode, cvf, classes);
         }
      }

   }

   public void convertField(DexClassNode classNode, DexFieldNode fieldNode, ClassVisitor cv) {
      String signature = null;
      if (fieldNode.anns != null) {
         Iterator var5 = fieldNode.anns.iterator();

         label53:
         while(true) {
            Object[] strs;
            label45:
            do {
               while(true) {
                  DexAnnotationNode ann;
                  do {
                     if (!var5.hasNext()) {
                        break label53;
                     }

                     ann = (DexAnnotationNode)var5.next();
                  } while(ann.visibility != Visibility.SYSTEM);

                  String var7 = ann.type;
                  byte var8 = -1;
                  switch(var7.hashCode()) {
                  case 1664436329:
                     if (var7.equals("Ldalvik/annotation/Signature;")) {
                        var8 = 0;
                     }
                  }

                  switch(var8) {
                  case 0:
                     strs = (Object[])((Object[])findAnnotationAttribute(ann, "value"));
                     continue label45;
                  }
               }
            } while(strs == null);

            StringBuilder sb = new StringBuilder();
            Object[] var11 = strs;
            int var12 = strs.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               Object str = var11[var13];
               sb.append(str);
            }

            signature = sb.toString();
         }
      }

      Object value = convertConstantValue(fieldNode.cst);
      int FieldCleanFlag = -131073;
      FieldVisitor fv = cv.visitField(fieldNode.access & -131073, fieldNode.field.getName(), fieldNode.field.getType(), signature, value);
      if (fv != null) {
         accept(fieldNode.anns, fv);
         fv.visitEnd();
      }
   }

   public static Object[] convertConstantValues(Object[] v) {
      Object[] copy = Arrays.copyOf(v, v.length);

      for(int i = 0; i < copy.length; ++i) {
         Object ele = copy[i];
         ele = convertConstantValue(ele);
         copy[i] = ele;
      }

      return copy;
   }

   public static Object convertConstantValue(Object ele) {
      if (ele instanceof DexType) {
         ele = Type.getType(((DexType)ele).desc);
      } else if (ele instanceof MethodHandle) {
         Handle h = null;
         MethodHandle mh = (MethodHandle)ele;
         switch(mh.getType()) {
         case 0:
            h = new Handle(3, toInternalName(mh.getField().getOwner()), mh.getField().getName(), mh.getField().getType());
            break;
         case 1:
            h = new Handle(1, toInternalName(mh.getField().getOwner()), mh.getField().getName(), mh.getField().getType());
            break;
         case 2:
            h = new Handle(3, toInternalName(mh.getField().getOwner()), mh.getField().getName(), mh.getField().getType());
            break;
         case 3:
            h = new Handle(1, toInternalName(mh.getField().getOwner()), mh.getField().getName(), mh.getField().getType());
            break;
         case 4:
            h = new Handle(6, toInternalName(mh.getMethod().getOwner()), mh.getMethod().getName(), mh.getMethod().getDesc());
            break;
         case 5:
            h = new Handle(5, toInternalName(mh.getMethod().getOwner()), mh.getMethod().getName(), mh.getMethod().getDesc());
         }

         ele = h;
      }

      return ele;
   }

   public void convertMethod(DexClassNode classNode, DexMethodNode methodNode, ClassVisitor cv) {
      MethodVisitor mv = collectBasicMethodInfo(methodNode, cv);
      if (mv != null) {
         DexAnnotationNode ann;
         if (0 != (classNode.access & 8192)) {
            Object defaultValue = null;
            if (classNode.anns != null) {
               Iterator var6 = classNode.anns.iterator();

               while(var6.hasNext()) {
                  DexAnnotationNode ann = (DexAnnotationNode)var6.next();
                  if (ann.visibility == Visibility.SYSTEM && ann.type.equals("Ldalvik/annotation/AnnotationDefault;")) {
                     ann = (DexAnnotationNode)findAnnotationAttribute(ann, "value");
                     if (ann != null) {
                        defaultValue = findAnnotationAttribute(ann, methodNode.method.getName());
                     }
                     break;
                  }
               }
            }

            if (defaultValue != null) {
               AnnotationVisitor av = mv.visitAnnotationDefault();
               if (av != null) {
                  accept(av, (String)null, defaultValue);
                  av.visitEnd();
               }
            }
         }

         accept(methodNode.anns, mv);
         if (methodNode.parameterAnns != null) {
            for(int i = 0; i < methodNode.parameterAnns.length; ++i) {
               List<DexAnnotationNode> anns = methodNode.parameterAnns[i];
               if (anns != null) {
                  Iterator var12 = anns.iterator();

                  while(var12.hasNext()) {
                     ann = (DexAnnotationNode)var12.next();
                     if (ann.visibility != Visibility.SYSTEM) {
                        acceptParameter(ann, i, mv);
                     }
                  }
               }
            }
         }

         if ((9472 & methodNode.access) == 0 && methodNode.codeNode != null) {
            mv.visitCode();
            this.convertCode(methodNode, mv);
         }

         mv.visitEnd();
      }
   }

   public IrMethod dex2ir(DexMethodNode methodNode) {
      return (new Dex2IRConverter()).convert(0 != (methodNode.access & 8), methodNode.method, methodNode.codeNode);
   }

   protected static Object findAnnotationAttribute(DexAnnotationNode ann, String name) {
      Iterator var2 = ann.items.iterator();

      DexAnnotationNode.Item item;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         item = (DexAnnotationNode.Item)var2.next();
      } while(!item.name.equals(name));

      return item.value;
   }

   private static Dex2Asm.Clz get(Map<String, Dex2Asm.Clz> classes, String name) {
      Dex2Asm.Clz clz = (Dex2Asm.Clz)classes.get(name);
      if (clz == null) {
         clz = new Dex2Asm.Clz(name);
         classes.put(name, clz);
      }

      return clz;
   }

   public void ir2j(IrMethod irMethod, MethodVisitor mv) {
      (new IR2JConverter(false)).convert(irMethod, mv);
      mv.visitMaxs(-1, -1);
   }

   public void optimize(IrMethod irMethod) {
      T_cleanLabel.transform(irMethod);
      T_deadCode.transform(irMethod);
      T_removeLocal.transform(irMethod);
      T_removeConst.transform(irMethod);
      T_zero.transform(irMethod);
      if (T_npe.transformReportChanged(irMethod)) {
         T_deadCode.transform(irMethod);
         T_removeLocal.transform(irMethod);
         T_removeConst.transform(irMethod);
      }

      T_new.transform(irMethod);
      T_fillArray.transform(irMethod);
      T_agg.transform(irMethod);
      T_multiArray.transform(irMethod);
      T_voidInvoke.transform(irMethod);
      T_type.transform(irMethod);
      T_unssa.transform(irMethod);
      T_trimEx.transform(irMethod);
      T_ir2jRegAssign.transform(irMethod);
   }

   private static void searchEnclosing(Dex2Asm.Clz clz, List<InnerClassNode> innerClassNodes) {
      Set<Dex2Asm.Clz> visitedClz = new HashSet();

      for(Dex2Asm.Clz p = clz; p != null && visitedClz.add(p); p = p.enclosingClass) {
         Dex2Asm.Clz enclosingClass = p.enclosingClass;
         if (enclosingClass == null || enclosingClass == clz) {
            break;
         }

         int accessInInner = clearInnerAccess(p.access);
         if (p.innerName != null) {
            innerClassNodes.add(new InnerClassNode(toInternalName(p.name), toInternalName(enclosingClass.name), p.innerName, accessInInner));
         } else {
            innerClassNodes.add(new InnerClassNode(toInternalName(p.name), (String)null, (String)null, accessInInner));
         }
      }

   }

   private static void searchInnerClass(Dex2Asm.Clz clz, List<InnerClassNode> innerClassNodes, String className) {
      Set<Dex2Asm.Clz> visited = new HashSet();
      Stack<Dex2Asm.Clz> stack = new Stack();
      stack.push(clz);

      while(true) {
         do {
            do {
               if (stack.empty()) {
                  return;
               }

               clz = (Dex2Asm.Clz)stack.pop();
            } while(visited.contains(clz));

            visited.add(clz);
         } while(clz.inners == null);

         Dex2Asm.Clz inner;
         for(Iterator var5 = clz.inners.iterator(); var5.hasNext(); stack.push(inner)) {
            inner = (Dex2Asm.Clz)var5.next();
            if (inner.innerName == null) {
               innerClassNodes.add(new InnerClassNode(toInternalName(inner.name), (String)null, (String)null, clearInnerAccess(inner.access)));
            } else {
               innerClassNodes.add(new InnerClassNode(toInternalName(inner.name), toInternalName(className), inner.innerName, clearInnerAccess(inner.access)));
            }
         }
      }
   }

   protected static class Clz {
      public int access;
      public Dex2Asm.Clz enclosingClass;
      public Method enclosingMethod;
      public String innerName;
      public Set<Dex2Asm.Clz> inners = null;
      public final String name;

      public Clz(String name) {
         this.name = name;
      }

      void addInner(Dex2Asm.Clz clz) {
         if (this.inners == null) {
            this.inners = new HashSet();
         }

         this.inners.add(clz);
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            Dex2Asm.Clz other = (Dex2Asm.Clz)obj;
            if (this.name == null) {
               if (other.name != null) {
                  return false;
               }
            } else if (!this.name.equals(other.name)) {
               return false;
            }

            return true;
         }
      }

      public int hashCode() {
         int prime = true;
         int result = 1;
         int result = 31 * result + (this.name == null ? 0 : this.name.hashCode());
         return result;
      }

      public String toString() {
         return "" + this.name;
      }
   }
}
