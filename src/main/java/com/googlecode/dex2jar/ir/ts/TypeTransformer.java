package com.googlecode.dex2jar.ir.ts;

import com.googlecode.d2j.DexType;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.TypeClass;
import com.googlecode.dex2jar.ir.expr.AbstractInvokeExpr;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.BinopExpr;
import com.googlecode.dex2jar.ir.expr.CastExpr;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.NewExpr;
import com.googlecode.dex2jar.ir.expr.NewMutiArrayExpr;
import com.googlecode.dex2jar.ir.expr.RefExpr;
import com.googlecode.dex2jar.ir.expr.StaticFieldExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.expr.UnopExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TypeTransformer implements Transformer {
   private static final String[] possibleIntTypes = new String[]{"B", "S", "C", "I"};

   public void transform(IrMethod irMethod) {
      TypeTransformer.TypeAnalyze ta = new TypeTransformer.TypeAnalyze(irMethod);
      List<TypeTransformer.TypeRef> refs = ta.analyze();
      Iterator var4 = refs.iterator();

      while(true) {
         while(var4.hasNext()) {
            TypeTransformer.TypeRef ref = (TypeTransformer.TypeRef)var4.next();
            String type = ref.getType();
            if (type == null) {
               System.err.println(ref);
            } else {
               if (ref.value.vt == Value.VT.CONSTANT) {
                  Constant cst = (Constant)ref.value;
                  switch(type.charAt(0)) {
                  case 'D':
                     if (!(cst.value instanceof Double)) {
                        cst.value = Double.longBitsToDouble(((Number)cst.value).longValue());
                     }
                     break;
                  case 'F':
                     if (!(cst.value instanceof Float)) {
                        cst.value = Float.intBitsToFloat(((Number)cst.value).intValue());
                     }
                     break;
                  case 'L':
                  case '[':
                     if (Integer.valueOf(0).equals(cst.value)) {
                        cst.value = Constant.Null;
                     }

                     int i;
                     if (type.equals("[F") && cst.value instanceof int[]) {
                        int[] x = (int[])((int[])cst.value);
                        float[] f = new float[x.length];

                        for(i = 0; i < x.length; ++i) {
                           f[i] = Float.intBitsToFloat(x[i]);
                        }

                        cst.value = f;
                     }

                     if (type.equals("[D") && cst.value instanceof long[]) {
                        long[] x = (long[])((long[])cst.value);
                        double[] f = new double[x.length];

                        for(i = 0; i < x.length; ++i) {
                           f[i] = Double.longBitsToDouble(x[i]);
                        }

                        cst.value = f;
                     }
                  }
               }

               Value value = ref.value;
               value.valueType = type;
               value.tag = null;
               ref.clear();
            }
         }

         return;
      }
   }

   private static class TypeAnalyze {
      protected IrMethod method;
      private List<TypeTransformer.TypeRef> refs = new ArrayList();

      public TypeAnalyze(IrMethod method) {
         this.method = method;
      }

      public List<TypeTransformer.TypeRef> analyze() {
         this.sxStmt();
         this.fixTypes();
         return this.refs;
      }

      private void fixTypes() {
         Set<TypeTransformer.TypeRef> arrayRoots = new HashSet();
         Iterator var2 = this.refs.iterator();

         TypeTransformer.TypeRef ref;
         while(var2.hasNext()) {
            ref = (TypeTransformer.TypeRef)var2.next();
            ref = ref.getReal();
            if (ref.gArrayValues != null || ref.sArrayValues != null) {
               arrayRoots.add(ref);
            }

            this.mergeArrayRelation(ref, TypeTransformer.Relation.R_gArrayValues);
            this.mergeArrayRelation(ref, TypeTransformer.Relation.R_sArrayValues);
            this.mergeArrayRelation(ref, TypeTransformer.Relation.R_arrayRoots);
         }

         UniqueQueue<TypeTransformer.TypeRef> q = new UniqueQueue();
         q.addAll(this.refs);

         label77:
         while(!q.isEmpty()) {
            while(!q.isEmpty()) {
               ref = (TypeTransformer.TypeRef)q.poll();
               this.copyTypes(q, ref);
            }

            Iterator var11 = arrayRoots.iterator();

            while(true) {
               TypeTransformer.TypeRef ref;
               String ele;
               TypeClass clz;
               Iterator var8;
               TypeTransformer.TypeRef p;
               do {
                  String provideDesc;
                  do {
                     do {
                        if (!var11.hasNext()) {
                           continue label77;
                        }

                        ref = (TypeTransformer.TypeRef)var11.next();
                        ref = ref.getReal();
                        provideDesc = ref.provideDesc;
                     } while(provideDesc == null);
                  } while(provideDesc.charAt(0) != '[');

                  ele = provideDesc.substring(1);
                  clz = TypeClass.clzOf(ele);
                  if (ref.gArrayValues != null) {
                     for(var8 = ref.gArrayValues.iterator(); var8.hasNext(); mergeTypeToArrayGetValue(ele, p, q)) {
                        p = (TypeTransformer.TypeRef)var8.next();
                        p = p.getReal();
                        if (p.updateTypeClass(clz)) {
                           q.add(p);
                        }
                     }
                  }
               } while(ref.sArrayValues == null);

               var8 = ref.sArrayValues.iterator();

               while(var8.hasNext()) {
                  p = (TypeTransformer.TypeRef)var8.next();
                  p = p.getReal();
                  if (p.updateTypeClass(clz)) {
                     q.add(p);
                  }

                  if (p.addUses(ele)) {
                     q.add(p);
                  }
               }
            }
         }

      }

      private void mergeArrayRelation(TypeTransformer.TypeRef ref, TypeTransformer.Relation r) {
         Set<TypeTransformer.TypeRef> v = r.get(ref);
         if (v != null && v.size() > 1) {
            List<TypeTransformer.TypeRef> copy = new ArrayList(v);
            TypeTransformer.TypeRef mergeTo = ((TypeTransformer.TypeRef)copy.get(0)).getReal();

            for(int i = 1; i < copy.size(); ++i) {
               mergeTo.merge((TypeTransformer.TypeRef)copy.get(i));
            }
         }

      }

      private static void mergeTypeToArrayGetValue(String type, TypeTransformer.TypeRef target, UniqueQueue<TypeTransformer.TypeRef> q) {
         target = target.getReal();
         if (target.provideDesc == null) {
            target.provideDesc = type;
            q.add(target);
         } else {
            String mergedType = mergeTypeEx(type, target.provideDesc);
            if (!mergedType.equals(target.provideDesc)) {
               target.provideDesc = mergedType;
               q.add(target);
            }
         }

      }

      private static void mergeTypeToSubRef(String type, TypeTransformer.TypeRef target, UniqueQueue<TypeTransformer.TypeRef> q) {
         if (target.provideDesc == null) {
            target.provideDesc = type;
            q.add(target);
         } else {
            String mergedType = mergeProviderType(type, target.provideDesc);
            if (!mergedType.equals(target.provideDesc)) {
               target.provideDesc = mergedType;
               q.add(target);
            }
         }

      }

      private static String mergeTypeEx(String a, String b) {
         if (a.equals(b)) {
            return a;
         } else {
            int as = countArrayDim(a);
            int bs = countArrayDim(b);
            if (as > bs) {
               return a;
            } else if (bs > as) {
               return b;
            } else {
               String elementTypeA = a.substring(as);
               String elementTypeB = a.substring(bs);
               TypeClass ta = TypeClass.clzOf(elementTypeA);
               TypeClass tb = TypeClass.clzOf(elementTypeB);
               if (ta.fixed && !tb.fixed) {
                  return a;
               } else if (!ta.fixed && tb.fixed) {
                  return b;
               } else if (ta.fixed && tb.fixed) {
                  if (ta != tb) {
                     if (as == 0) {
                        throw new RuntimeException();
                     } else {
                        return buildArray(as - 1, "L");
                     }
                  } else if (ta != TypeClass.INT) {
                     return buildArray(as, "L");
                  } else {
                     String chooseType = "I";

                     for(int i = TypeTransformer.possibleIntTypes.length - 1; i >= 0; --i) {
                        String t = TypeTransformer.possibleIntTypes[i];
                        if (a.equals(t) || b.equals(t)) {
                           chooseType = t;
                           break;
                        }
                     }

                     return buildArray(as, chooseType);
                  }
               } else {
                  return buildArray(as, TypeClass.merge(ta, tb).name);
               }
            }
         }
      }

      private void copyTypes(UniqueQueue<TypeTransformer.TypeRef> q, TypeTransformer.TypeRef ref) {
         ref = ref.getReal();
         TypeClass clz = ref.clz;
         switch(clz) {
         case BOOLEAN:
         case FLOAT:
         case LONG:
         case DOUBLE:
         case VOID:
            ref.provideDesc = clz.name;
         default:
            String provideDesc = ref.provideDesc;
            if (provideDesc == null && ref.parents != null && ref.parents.size() > 1 && this.isAllParentSetted(ref)) {
               ref.provideDesc = provideDesc = this.mergeParentType(ref.parents);
            }

            Iterator var5;
            TypeTransformer.TypeRef p;
            if (ref.parents != null) {
               var5 = ref.parents.iterator();

               while(var5.hasNext()) {
                  p = (TypeTransformer.TypeRef)var5.next();
                  p = p.getReal();
                  if (p.updateTypeClass(clz)) {
                     q.add(p);
                  }

                  if (ref.uses != null && p.addAllUses(ref.uses)) {
                     q.add(p);
                  }
               }
            }

            if (ref.children != null) {
               var5 = ref.children.iterator();

               while(var5.hasNext()) {
                  p = (TypeTransformer.TypeRef)var5.next();
                  p = p.getReal();
                  if (p.updateTypeClass(clz)) {
                     q.add(p);
                  }

                  if (provideDesc != null) {
                     mergeTypeToSubRef(provideDesc, p, q);
                  }
               }
            }

            if (ref.sameValues != null) {
               var5 = ref.sameValues.iterator();

               while(var5.hasNext()) {
                  p = (TypeTransformer.TypeRef)var5.next();
                  p = p.getReal();
                  if (p.updateTypeClass(clz)) {
                     q.add(p);
                  }
               }
            }

         }
      }

      private boolean isAllParentSetted(TypeTransformer.TypeRef ref) {
         boolean allAreSet = true;
         Iterator var3 = ref.parents.iterator();

         while(var3.hasNext()) {
            TypeTransformer.TypeRef p = (TypeTransformer.TypeRef)var3.next();
            if (p.getProvideDesc() == null) {
               allAreSet = false;
               break;
            }
         }

         return allAreSet;
      }

      private static String mergeObjectType(String a, String b) {
         if (a.equals(b)) {
            return a;
         } else if ("L".endsWith(a)) {
            return b;
         } else if ("L".equals(b)) {
            return a;
         } else {
            return a.compareTo(b) > 0 ? a : b;
         }
      }

      private static String mergeProviderType(String a, String b) {
         if (a.equals(b)) {
            return a;
         } else {
            TypeClass ta = TypeClass.clzOf(a);
            TypeClass tb = TypeClass.clzOf(b);
            if (ta.fixed && !tb.fixed) {
               return a;
            } else if (!ta.fixed && tb.fixed) {
               return b;
            } else if (ta.fixed && tb.fixed) {
               if (ta == TypeClass.INT && tb == TypeClass.BOOLEAN || tb == TypeClass.INT && ta == TypeClass.BOOLEAN) {
                  return "I";
               } else if (ta != tb) {
                  throw new RuntimeException();
               } else {
                  int as;
                  if (ta != TypeClass.INT) {
                     if (ta == TypeClass.OBJECT) {
                        as = countArrayDim(a);
                        int bs = countArrayDim(b);
                        if (as != 0 && bs != 0) {
                           String elementTypeA = a.substring(as);
                           String elementTypeB = a.substring(bs);
                           if (as < bs) {
                              return buildArray(elementTypeB.charAt(0) == 'L' ? bs : bs - 1, "L");
                           } else if (bs > as) {
                              return buildArray(elementTypeA.charAt(0) == 'L' ? as : as - 1, "L");
                           } else {
                              return elementTypeA.charAt(0) == 'L' && elementTypeB.charAt(0) == 'L' ? buildArray(as, "L") : buildArray(as - 1, "L");
                           }
                        } else {
                           return mergeObjectType(a, b);
                        }
                     } else {
                        throw new RuntimeException();
                     }
                  } else {
                     for(as = TypeTransformer.possibleIntTypes.length - 1; as >= 0; --as) {
                        String t = TypeTransformer.possibleIntTypes[as];
                        if (a.equals(t) || b.equals(t)) {
                           return t;
                        }
                     }

                     return "I";
                  }
               }
            } else {
               return TypeClass.merge(ta, tb).name;
            }
         }
      }

      private static String buildArray(int dim, String s) {
         if (dim == 0) {
            return s;
         } else {
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < dim; ++i) {
               sb.append('[');
            }

            sb.append(s);
            return sb.toString();
         }
      }

      private static int countArrayDim(String a) {
         int i;
         for(i = 0; a.charAt(i) == '['; ++i) {
         }

         return i;
      }

      private String mergeParentType(Set<TypeTransformer.TypeRef> parents) {
         Iterator<TypeTransformer.TypeRef> it = parents.iterator();

         String a;
         for(a = ((TypeTransformer.TypeRef)it.next()).getProvideDesc(); it.hasNext(); a = mergeProviderType(a, ((TypeTransformer.TypeRef)it.next()).getProvideDesc())) {
         }

         return a;
      }

      private void e0expr(Value.E0Expr op, boolean getValue) {
         switch(op.vt) {
         case LOCAL:
         default:
            break;
         case NEW:
            NewExpr newExpr = (NewExpr)op;
            this.provideAs(newExpr, newExpr.type);
            break;
         case THIS_REF:
         case PARAMETER_REF:
         case EXCEPTION_REF:
            RefExpr refExpr = (RefExpr)op;
            String refType = refExpr.type;
            if (refType == null && op.vt == Value.VT.EXCEPTION_REF) {
               refType = "Ljava/lang/Throwable;";
            }

            this.provideAs(refExpr, refType);
            break;
         case STATIC_FIELD:
            StaticFieldExpr fe = (StaticFieldExpr)op;
            if (getValue) {
               this.provideAs(fe, fe.type);
            } else {
               this.useAs(fe, fe.type);
            }
            break;
         case CONSTANT:
            Constant cst = (Constant)op;
            Object value = cst.value;
            if (value instanceof String) {
               this.provideAs(cst, "Ljava/lang/String;");
            } else if (value instanceof DexType) {
               this.provideAs(cst, "Ljava/lang/Class;");
            } else if (value instanceof Number) {
               if (!(value instanceof Integer) && !(value instanceof Byte) && !(value instanceof Short)) {
                  if (value instanceof Long) {
                     this.provideAs(cst, "w");
                  } else if (value instanceof Float) {
                     this.provideAs(cst, "F");
                  } else if (value instanceof Double) {
                     this.provideAs(cst, "D");
                  }
               } else {
                  int a = ((Number)value).intValue();
                  if (a == 0) {
                     this.provideAs(cst, TypeClass.ZIFL.name);
                  } else if (a == 1) {
                     this.provideAs(cst, TypeClass.ZIF.name);
                  } else {
                     this.provideAs(cst, TypeClass.IF.name);
                  }
               }
            } else if (value instanceof Character) {
               this.provideAs(cst, "C");
            } else {
               this.provideAs(cst, "L");
            }
         }

      }

      private void e1expr(Value.E1Expr e1, boolean getValue) {
         Value v = e1.op;
         UnopExpr ue;
         TypeExpr te;
         switch(e1.vt) {
         case CAST:
            CastExpr ce = (CastExpr)e1;
            if (ce.to.equals("B")) {
               this.useAs(v, TypeClass.ZI.name);
               this.provideAs(e1, TypeClass.ZI.name);
            } else {
               this.useAs(v, ce.from);
               this.provideAs(e1, ce.to);
            }
            break;
         case FIELD:
            FieldExpr fe = (FieldExpr)e1;
            if (getValue) {
               this.provideAs(fe, fe.type);
            } else {
               this.useAs(fe, fe.type);
            }

            if (v != null) {
               this.useAs(v, fe.owner);
            }
            break;
         case CHECK_CAST:
            te = (TypeExpr)e1;
            this.provideAs(te, te.type);
            this.useAs(v, "L");
            break;
         case INSTANCE_OF:
            te = (TypeExpr)e1;
            this.provideAs(te, "Z");
            this.useAs(v, "L");
            break;
         case NEW_ARRAY:
            te = (TypeExpr)e1;
            this.provideAs(te, "[" + te.type);
            this.useAs(v, "I");
            break;
         case LENGTH:
            ue = (UnopExpr)e1;
            this.provideAs(ue, "I");
            this.useAs(v, "[?");
            break;
         case NEG:
         case NOT:
            ue = (UnopExpr)e1;
            this.provideAs(ue, ue.type);
            this.useAs(v, ue.type);
         }

         if (v != null) {
            this.exExpr(v);
         }

      }

      private void e2expr(Value.E2Expr e2, boolean getValue) {
         Value a = e2.op1.trim();
         Value b = e2.op2.trim();
         BinopExpr be;
         switch(e2.vt) {
         case ARRAY:
            this.useAs(b, "I");
            String elementType = ((ArrayExpr)e2).elementType;
            this.useAs(a, "[" + elementType);
            if (getValue) {
               this.provideAs(e2, elementType);
               this.linkGetArray(a, e2);
            } else {
               this.useAs(e2, elementType);
               this.linkSetArray(a, e2);
            }
            break;
         case LCMP:
         case FCMPG:
         case FCMPL:
         case DCMPG:
         case DCMPL:
            be = (BinopExpr)e2;
            this.useAs(a, be.type);
            this.useAs(b, be.type);
            this.provideAs(e2, "I");
            break;
         case EQ:
         case NE:
            this.useAs(e2.getOp2(), TypeClass.ZIL.name);
            this.useAs(e2.getOp1(), TypeClass.ZIL.name);
            this.linkSameAs(e2.getOp1(), e2.getOp2());
            this.provideAs(e2, "Z");
            break;
         case GE:
         case GT:
         case LE:
         case LT:
            be = (BinopExpr)e2;
            this.useAs(a, be.type);
            this.useAs(b, be.type);
            this.provideAs(e2, "Z");
            break;
         case ADD:
         case SUB:
         case IDIV:
         case LDIV:
         case FDIV:
         case DDIV:
         case MUL:
         case REM:
            be = (BinopExpr)e2;
            this.useAs(a, be.type);
            this.useAs(b, be.type);
            this.provideAs(e2, be.type);
            break;
         case OR:
         case AND:
         case XOR:
            be = (BinopExpr)e2;
            this.useAs(a, be.type);
            this.useAs(b, be.type);
            if (!"J".equals(be.type) && !"w".equals(be.type)) {
               this.provideAs(e2, TypeClass.ZI.name);
            } else {
               this.provideAs(e2, be.type);
            }
            break;
         case SHL:
         case SHR:
         case USHR:
            be = (BinopExpr)e2;
            this.useAs(a, be.type);
            this.useAs(b, "I");
            this.provideAs(e2, be.type);
            break;
         default:
            throw new UnsupportedOperationException();
         }

         if (a != null) {
            this.exExpr(a);
         }

         if (b != null) {
            this.exExpr(b);
         }

      }

      private void linkSameAs(Value a, Value b) {
         TypeTransformer.TypeRef aa = this.getDefTypeRef(a);
         TypeTransformer.TypeRef bb = this.getDefTypeRef(b);
         if (aa.sameValues == null) {
            aa.sameValues = new HashSet(3);
         }

         if (bb.sameValues == null) {
            bb.sameValues = new HashSet(3);
         }

         aa.sameValues.add(bb);
         bb.sameValues.add(aa);
      }

      private void enexpr(Value.EnExpr enExpr) {
         int var16;
         Value[] vbs = enExpr.ops;
         int var7;
         int i;
         label78:
         switch(enExpr.vt) {
         case INVOKE_NEW:
         case INVOKE_INTERFACE:
         case INVOKE_SPECIAL:
         case INVOKE_STATIC:
         case INVOKE_VIRTUAL:
         case INVOKE_POLYMORPHIC:
         case INVOKE_CUSTOM:
            AbstractInvokeExpr ice = (AbstractInvokeExpr)enExpr;
            String type = ice.getProto().getReturnType();
            this.provideAs(enExpr, type);
            this.useAs(enExpr, type);
            String[] argTypes = ice.getProto().getParameterTypes();
            if (argTypes.length == vbs.length) {
               i = 0;

               while(true) {
                  if (i >= vbs.length) {
                     break label78;
                  }

                  this.useAs(vbs[i], argTypes[i]);
                  ++i;
               }
            } else {
               if (argTypes.length + 1 != vbs.length) {
                  throw new RuntimeException();
               }

               this.useAs(vbs[0], "L");
               i = 1;

               while(true) {
                  if (i >= vbs.length) {
                     break label78;
                  }

                  this.useAs(vbs[i], argTypes[i - 1]);
                  ++i;
               }
            }
         case FILLED_ARRAY:
            FilledArrayExpr fae = (FilledArrayExpr)enExpr;
            Value[] var12 = vbs;
            var16 = vbs.length;

            for(i = 0; i < var16; ++i) {
               Value vb = var12[i];
               this.useAs(vb, fae.type);
            }

            this.provideAs(fae, "[" + fae.type);
            break;
         case NEW_MUTI_ARRAY:
            NewMutiArrayExpr nmae = (NewMutiArrayExpr)enExpr;
            Value[] var5 = vbs;
            i = vbs.length;

            for(var7 = 0; var7 < i; ++var7) {
               Value vb = var5[var7];
               this.useAs(vb, "I");
            }

            StringBuilder sb = new StringBuilder();

            for(i = 0; i < nmae.dimension; ++i) {
               sb.append('[');
            }

            sb.append(nmae.baseType);
            this.provideAs(nmae, sb.toString());
            break;
         case PHI:
            Value[] var6 = vbs;
            var7 = vbs.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               Value vb = var6[var8];
               this.linkFromTo(vb, enExpr);
            }
         }

         Value[] var11 = enExpr.ops;
         int var14 = var11.length;

         for(var16 = 0; var16 < var14; ++var16) {
            Value vb = var11[var16];
            this.exExpr(vb);
         }

      }

      private void exExpr(Value op) {
         this.exExpr(op, true);
      }

      private void exExpr(Value op, boolean getValue) {
         switch(op.et) {
         case E0:
            this.e0expr((Value.E0Expr)op, getValue);
            break;
         case E1:
            this.e1expr((Value.E1Expr)op, getValue);
            break;
         case E2:
            this.e2expr((Value.E2Expr)op, getValue);
            break;
         case En:
            this.enexpr((Value.EnExpr)op);
         }

      }

      private TypeTransformer.TypeRef getDefTypeRef(Value v) {
         Object object = v.tag;
         TypeTransformer.TypeRef typeRef;
         if (object != null && object instanceof TypeTransformer.TypeRef) {
            typeRef = (TypeTransformer.TypeRef)object;
         } else {
            typeRef = new TypeTransformer.TypeRef(v);
            this.refs.add(typeRef);
            v.tag = typeRef;
         }

         return typeRef;
      }

      private void linkGetArray(Value array, Value v) {
         TypeTransformer.TypeRef root = this.getDefTypeRef(array);
         TypeTransformer.TypeRef value = this.getDefTypeRef(v);
         if (root.gArrayValues == null) {
            root.gArrayValues = new HashSet(3);
         }

         root.gArrayValues.add(value);
         if (value.arrayRoots == null) {
            value.arrayRoots = new HashSet(3);
         }

         value.arrayRoots.add(root);
      }

      private void linkSetArray(Value array, Value v) {
         TypeTransformer.TypeRef root = this.getDefTypeRef(array);
         TypeTransformer.TypeRef value = this.getDefTypeRef(v);
         if (root.sArrayValues == null) {
            root.sArrayValues = new HashSet(3);
         }

         root.sArrayValues.add(value);
         if (value.arrayRoots == null) {
            value.arrayRoots = new HashSet(3);
         }

         value.arrayRoots.add(root);
      }

      private void linkFromTo(Value from, Value to) {
         TypeTransformer.TypeRef tFrom = this.getDefTypeRef(from);
         TypeTransformer.TypeRef tTo = this.getDefTypeRef(to);
         if (tFrom.children == null) {
            tFrom.children = new HashSet();
         }

         tFrom.children.add(tTo);
         if (tTo.parents == null) {
            tTo.parents = new HashSet();
         }

         tTo.parents.add(tFrom);
      }

      private void provideAs(Value op, String type) {
         TypeTransformer.TypeRef typeRef = this.getDefTypeRef(op).getReal();
         typeRef.provideDesc = type;
         typeRef.updateTypeClass(TypeClass.clzOf(type));
      }

      private void s1stmt(Stmt.E1Stmt s) {
         if (s.st != Stmt.ST.GOTO) {
            Value op = s.op;
            switch(s.st) {
            case LOOKUP_SWITCH:
            case TABLE_SWITCH:
               this.useAs(op, "I");
            case GOTO:
            default:
               break;
            case IF:
               this.useAs(op, "Z");
               break;
            case LOCK:
            case UNLOCK:
               this.useAs(op, "L");
               break;
            case THROW:
               this.useAs(op, "Ljava/lang/Throwable;");
               break;
            case RETURN:
               this.useAs(op, this.method.ret);
            }

            this.exExpr(op);
         }
      }

      private void s2stmt(Stmt.E2Stmt s) {
         if (s.st == Stmt.ST.FILL_ARRAY_DATA) {
            this.linkFromTo(s.op1, s.op2);
         } else {
            Value from = s.op2;
            Value to = s.op1;
            this.linkFromTo(from, to);
            this.exExpr(from);
            this.exExpr(to, false);
         }

      }

      private void sxStmt() {
         label32:
         for(Stmt p = this.method.stmts.getFirst(); p != null; p = p.getNext()) {
            switch(p.et) {
            case E0:
               if (p.st != Stmt.ST.LABEL) {
                  break;
               }

               LabelStmt labelStmt = (LabelStmt)p;
               if (labelStmt.phis == null) {
                  break;
               }

               Iterator var3 = labelStmt.phis.iterator();

               while(true) {
                  if (!var3.hasNext()) {
                     continue label32;
                  }

                  AssignStmt phi = (AssignStmt)var3.next();
                  this.s2stmt(phi);
               }
            case E1:
               this.s1stmt((Stmt.E1Stmt)p);
               break;
            case E2:
               this.s2stmt((Stmt.E2Stmt)p);
            case En:
            }
         }

      }

      public String toString() {
         StringBuilder sb = new StringBuilder();
         Iterator var2 = this.refs.iterator();

         while(var2.hasNext()) {
            TypeTransformer.TypeRef ref = (TypeTransformer.TypeRef)var2.next();
            sb.append(ref).append("\n");
         }

         return sb.toString();
      }

      private void useAs(Value op, String type) {
         TypeTransformer.TypeRef typeRef = this.getDefTypeRef(op);
         typeRef.addUses(type);
         typeRef.updateTypeClass(TypeClass.clzOf(type));
      }
   }

   public static class TypeRef {
      public final Value value;
      public Set<TypeTransformer.TypeRef> sameValues = null;
      public Set<TypeTransformer.TypeRef> gArrayValues = null;
      public Set<TypeTransformer.TypeRef> sArrayValues = null;
      public Set<TypeTransformer.TypeRef> arrayRoots = null;
      public Set<TypeTransformer.TypeRef> parents = null;
      public Set<TypeTransformer.TypeRef> children = null;
      public TypeClass clz;
      public String provideDesc;
      public Set<String> uses;
      private TypeTransformer.TypeRef next;

      public void merge(TypeTransformer.TypeRef other) {
         assert this.next == null;

         TypeTransformer.TypeRef b = other.getReal();
         if (this != b) {
            b.next = this;
            relationMerge(this, b, TypeTransformer.Relation.R_sameValues);
            relationMerge(this, b, TypeTransformer.Relation.R_gArrayValues);
            relationMerge(this, b, TypeTransformer.Relation.R_sArrayValues);
            relationMerge(this, b, TypeTransformer.Relation.R_arrayRoots);
            relationMerge(this, b, TypeTransformer.Relation.R_parents);
            relationMerge(this, b, TypeTransformer.Relation.R_children);
            if (this.provideDesc == null) {
               this.provideDesc = b.provideDesc;
            } else if (b.provideDesc != null) {
               this.provideDesc = TypeTransformer.TypeAnalyze.mergeProviderType(this.provideDesc, b.provideDesc);
            }

            b.provideDesc = null;
            if (b.uses != null) {
               if (this.uses == null) {
                  this.uses = b.uses;
               } else {
                  this.uses.addAll(b.uses);
               }

               b.uses = null;
            }

         }
      }

      private static void relationMerge(TypeTransformer.TypeRef a, TypeTransformer.TypeRef b, TypeTransformer.Relation r) {
         Set<TypeTransformer.TypeRef> bv = r.get(b);
         if (bv != null) {
            Set<TypeTransformer.TypeRef> av = r.get(a);
            Set merged;
            if (av == null) {
               merged = bv;
               r.set(a, bv);
            } else {
               merged = av;
               av.addAll(bv);
            }

            merged.remove(a);
            merged.remove(b);
            r.set(b, (Set)null);
         }

      }

      private TypeTransformer.TypeRef getReal() {
         TypeTransformer.TypeRef x;
         for(x = this; x.next != null; x = x.next) {
         }

         if (x != this) {
            this.next = x;
         }

         return x;
      }

      public TypeRef(Value value) {
         this.clz = TypeClass.UNKNOWN;
         this.provideDesc = null;
         this.value = value;
      }

      public String toString() {
         TypeTransformer.TypeRef real = this.getReal();
         String p = real.uses == null ? "[]" : real.uses.toString();
         return real.clz + "::" + this.value + ": " + real.provideDesc + " > {" + p.substring(1, p.length() - 1) + "}";
      }

      public String getType() {
         TypeTransformer.TypeRef thiz = this.getReal();
         TypeClass clz = thiz.clz;
         if (clz == TypeClass.OBJECT) {
            return thiz.provideDesc.length() == 1 ? "Ljava/lang/Object;" : thiz.provideDesc;
         } else if (clz.fixed && clz != TypeClass.INT) {
            if (thiz.provideDesc == null) {
               throw new RuntimeException();
            } else {
               return thiz.provideDesc;
            }
         } else if (clz == TypeClass.JD) {
            return "J";
         } else {
            if (thiz.uses != null) {
               String[] var3 = TypeTransformer.possibleIntTypes;
               int var4 = var3.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                  String t = var3[var5];
                  if (thiz.uses.contains(t)) {
                     return t;
                  }
               }
            }

            switch(clz) {
            case ZI:
               return "I";
            case ZIFL:
            case ZIF:
            case ZIL:
               return "Z";
            case INT:
            case IF:
               return "I";
            default:
               throw new RuntimeException();
            }
         }
      }

      public boolean updateTypeClass(TypeClass clz) {
         assert this.next == null;

         TypeClass thizClz = this.clz;
         TypeClass merged = TypeClass.merge(thizClz, clz);
         if (merged == thizClz) {
            return false;
         } else {
            this.clz = merged;
            return true;
         }
      }

      public void clear() {
         this.sArrayValues = null;
         this.gArrayValues = null;
         this.arrayRoots = null;
         this.parents = null;
         this.children = null;
         this.sameValues = null;
      }

      String getProvideDesc() {
         return this.getReal().provideDesc;
      }

      public boolean addUses(String ele) {
         assert this.next == null;

         if (this.uses != null) {
            return this.uses.add(ele);
         } else {
            this.uses = new HashSet();
            return this.uses.add(ele);
         }
      }

      public boolean addAllUses(Set<String> uses) {
         assert this.next == null;

         if (uses != null) {
            return uses.addAll(uses);
         } else {
            Set<String> uses = new HashSet();
            return uses.addAll(uses);
         }
      }
   }

   static enum Relation {
      R_sameValues {
         Set<TypeTransformer.TypeRef> get(TypeTransformer.TypeRef obj) {
            return obj.sameValues;
         }

         void set(TypeTransformer.TypeRef obj, Set<TypeTransformer.TypeRef> v) {
            obj.sameValues = v;
         }
      },
      R_gArrayValues {
         Set<TypeTransformer.TypeRef> get(TypeTransformer.TypeRef obj) {
            return obj.gArrayValues;
         }

         void set(TypeTransformer.TypeRef obj, Set<TypeTransformer.TypeRef> v) {
            obj.gArrayValues = v;
         }
      },
      R_sArrayValues {
         Set<TypeTransformer.TypeRef> get(TypeTransformer.TypeRef obj) {
            return obj.sArrayValues;
         }

         void set(TypeTransformer.TypeRef obj, Set<TypeTransformer.TypeRef> v) {
            obj.sArrayValues = v;
         }
      },
      R_arrayRoots {
         Set<TypeTransformer.TypeRef> get(TypeTransformer.TypeRef obj) {
            return obj.arrayRoots;
         }

         void set(TypeTransformer.TypeRef obj, Set<TypeTransformer.TypeRef> v) {
            obj.arrayRoots = v;
         }
      },
      R_parents {
         Set<TypeTransformer.TypeRef> get(TypeTransformer.TypeRef obj) {
            return obj.parents;
         }

         void set(TypeTransformer.TypeRef obj, Set<TypeTransformer.TypeRef> v) {
            obj.parents = v;
         }
      },
      R_children {
         Set<TypeTransformer.TypeRef> get(TypeTransformer.TypeRef obj) {
            return obj.children;
         }

         void set(TypeTransformer.TypeRef obj, Set<TypeTransformer.TypeRef> v) {
            obj.children = v;
         }
      };

      private Relation() {
      }

      abstract Set<TypeTransformer.TypeRef> get(TypeTransformer.TypeRef obj);

      abstract void set(TypeTransformer.TypeRef obj, Set<TypeTransformer.TypeRef> v);

      // $FF: synthetic method
      Relation(Object x2) {
         this();
      }
   }
}
