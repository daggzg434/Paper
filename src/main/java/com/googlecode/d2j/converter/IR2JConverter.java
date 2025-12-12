package com.googlecode.d2j.converter;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.asm.LdcOptimizeAdapter;
import com.googlecode.d2j.dex.Dex2Asm;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.expr.ArrayExpr;
import com.googlecode.dex2jar.ir.expr.CastExpr;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FieldExpr;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.InvokeCustomExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.InvokePolymorphicExpr;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.NewExpr;
import com.googlecode.dex2jar.ir.expr.NewMutiArrayExpr;
import com.googlecode.dex2jar.ir.expr.StaticFieldExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.GotoStmt;
import com.googlecode.dex2jar.ir.stmt.IfStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.LookupSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.TableSwitchStmt;
import com.googlecode.dex2jar.ir.stmt.UnopStmt;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class IR2JConverter implements Opcodes {
   private boolean optimizeSynchronized = false;

   public IR2JConverter() {
   }

   public IR2JConverter(boolean optimizeSynchronized) {
      this.optimizeSynchronized = optimizeSynchronized;
   }

   public void convert(IrMethod ir, MethodVisitor asm) {
      this.mapLabelStmt(ir);
      this.reBuildInstructions(ir, asm);
      this.reBuildTryCatchBlocks(ir, asm);
   }

   private void mapLabelStmt(IrMethod ir) {
      Iterator var2 = ir.stmts.iterator();

      while(var2.hasNext()) {
         Stmt p = (Stmt)var2.next();
         if (p.st == Stmt.ST.LABEL) {
            LabelStmt labelStmt = (LabelStmt)p;
            labelStmt.tag = new Label();
         }
      }

   }

   private void reBuildTryCatchBlocks(IrMethod ir, MethodVisitor asm) {
      Iterator var3 = ir.traps.iterator();

      while(true) {
         Trap trap;
         boolean needAdd;
         do {
            if (!var3.hasNext()) {
               return;
            }

            trap = (Trap)var3.next();
            needAdd = false;

            for(Stmt p = trap.start.getNext(); p != null && p != trap.end; p = p.getNext()) {
               if (p.st != Stmt.ST.LABEL) {
                  needAdd = true;
                  break;
               }
            }
         } while(!needAdd);

         for(int i = 0; i < trap.handlers.length; ++i) {
            String type = trap.types[i];
            asm.visitTryCatchBlock((Label)trap.start.tag, (Label)trap.end.tag, (Label)trap.handlers[i].tag, type == null ? null : toInternal(type));
         }
      }
   }

   static String toInternal(String n) {
      return Type.getType(n).getInternalName();
   }

   private void reBuildInstructions(IrMethod ir, MethodVisitor asm) {
      MethodVisitor asm = new LdcOptimizeAdapter(asm);
      int maxLocalIndex = 0;

      Local local;
      for(Iterator var4 = ir.locals.iterator(); var4.hasNext(); maxLocalIndex = Math.max(maxLocalIndex, local._ls_index)) {
         local = (Local)var4.next();
      }

      Map<String, Integer> lockMap = new HashMap();
      Iterator var21 = ir.stmts.iterator();

      while(var21.hasNext()) {
         Stmt st = (Stmt)var21.next();
         Value v;
         String key;
         int arraySize;
         int increment;
         Label[] targets;
         Stmt.E2Stmt e2;
         Integer integer;
         switch(st.st) {
         case LABEL:
            LabelStmt labelStmt = (LabelStmt)st;
            Label label = (Label)labelStmt.tag;
            asm.visitLabel(label);
            if (labelStmt.lineNumber >= 0) {
               asm.visitLineNumber(labelStmt.lineNumber, label);
            }
            break;
         case ASSIGN:
            e2 = (Stmt.E2Stmt)st;
            Value v1 = e2.op1;
            Value v2 = e2.op2;
            switch(v1.vt) {
            case LOCAL:
               Local local = (Local)v1;
               int i = local._ls_index;
               boolean skipOrg = false;
               if (v2.vt == Value.VT.LOCAL && i == ((Local)v2)._ls_index) {
                  skipOrg = true;
               } else if (v1.valueType.charAt(0) == 'I') {
                  if (v2.vt == Value.VT.ADD) {
                     if (isLocalWithIndex(v2.getOp1(), i) && v2.getOp2().vt == Value.VT.CONSTANT) {
                        increment = (Integer)((Constant)v2.getOp2()).value;
                        if (increment >= -32768 && increment <= 32767) {
                           asm.visitIincInsn(i, increment);
                           skipOrg = true;
                        }
                     } else if (isLocalWithIndex(v2.getOp2(), i) && v2.getOp1().vt == Value.VT.CONSTANT) {
                        increment = (Integer)((Constant)v2.getOp1()).value;
                        if (increment >= -32768 && increment <= 32767) {
                           asm.visitIincInsn(i, increment);
                           skipOrg = true;
                        }
                     }
                  } else if (v2.vt == Value.VT.SUB && isLocalWithIndex(v2.getOp1(), i) && v2.getOp2().vt == Value.VT.CONSTANT) {
                     increment = -(Integer)((Constant)v2.getOp2()).value;
                     if (increment >= -32768 && increment <= 32767) {
                        asm.visitIincInsn(i, increment);
                        skipOrg = true;
                     }
                  }
               }

               if (!skipOrg) {
                  accept(v2, asm);
                  if (i >= 0) {
                     asm.visitVarInsn(getOpcode((Value)v1, 54), i);
                  } else if (!v1.valueType.equals("V")) {
                     switch(v1.valueType.charAt(0)) {
                     case 'D':
                     case 'J':
                        asm.visitInsn(88);
                        continue;
                     default:
                        asm.visitInsn(87);
                     }
                  }
               }
               continue;
            case STATIC_FIELD:
               StaticFieldExpr fe = (StaticFieldExpr)v1;
               accept(v2, asm);
               insertI2x(v2.valueType, fe.type, asm);
               asm.visitFieldInsn(179, toInternal(fe.owner), fe.name, fe.type);
               continue;
            case FIELD:
               FieldExpr fe = (FieldExpr)v1;
               accept(fe.op, asm);
               accept(v2, asm);
               insertI2x(v2.valueType, fe.type, asm);
               asm.visitFieldInsn(181, toInternal(fe.owner), fe.name, fe.type);
               continue;
            case ARRAY:
               ArrayExpr ae = (ArrayExpr)v1;
               accept(ae.op1, asm);
               accept(ae.op2, asm);
               accept(v2, asm);
               String tp1 = ae.op1.valueType;
               String tp2 = ae.valueType;
               if (tp1.charAt(0) == '[') {
                  String arrayElementType = tp1.substring(1);
                  insertI2x(v2.valueType, arrayElementType, asm);
                  asm.visitInsn(getOpcode((String)arrayElementType, 79));
               } else {
                  asm.visitInsn(getOpcode((String)tp2, 79));
               }
            default:
               continue;
            }
         case IDENTITY:
            e2 = (Stmt.E2Stmt)st;
            if (e2.op2.vt == Value.VT.EXCEPTION_REF) {
               int index = ((Local)e2.op1)._ls_index;
               if (index >= 0) {
                  asm.visitVarInsn(58, index);
               } else {
                  asm.visitInsn(87);
               }
            }
            break;
         case FILL_ARRAY_DATA:
            e2 = (Stmt.E2Stmt)st;
            String elementType;
            int iastoreOP;
            String arrayValueType;
            if (e2.getOp2().vt == Value.VT.CONSTANT) {
               Object arrayData = ((Constant)e2.getOp2()).value;
               arraySize = Array.getLength(arrayData);
               arrayValueType = e2.getOp1().valueType;
               if (arrayValueType.charAt(0) == '[') {
                  elementType = arrayValueType.substring(1);
               } else {
                  elementType = "I";
               }

               iastoreOP = getOpcode((String)elementType, 79);
               accept(e2.getOp1(), asm);

               for(increment = 0; increment < arraySize; ++increment) {
                  asm.visitInsn(89);
                  asm.visitLdcInsn(increment);
                  asm.visitLdcInsn(Array.get(arrayData, increment));
                  asm.visitInsn(iastoreOP);
               }

               asm.visitInsn(87);
               break;
            }

            FilledArrayExpr filledArrayExpr = (FilledArrayExpr)e2.getOp2();
            arraySize = filledArrayExpr.ops.length;
            arrayValueType = e2.getOp1().valueType;
            if (arrayValueType.charAt(0) == '[') {
               elementType = arrayValueType.substring(1);
            } else {
               elementType = "I";
            }

            iastoreOP = getOpcode((String)elementType, 79);
            accept(e2.getOp1(), asm);

            for(increment = 0; increment < arraySize; ++increment) {
               asm.visitInsn(89);
               asm.visitLdcInsn(increment);
               accept(filledArrayExpr.ops[increment], asm);
               asm.visitInsn(iastoreOP);
            }

            asm.visitInsn(87);
            break;
         case GOTO:
            asm.visitJumpInsn(167, (Label)((GotoStmt)st).target.tag);
            break;
         case IF:
            this.reBuildJumpInstructions((IfStmt)st, asm);
            break;
         case LOCK:
            v = ((UnopStmt)st).op;
            accept(v, asm);
            if (this.optimizeSynchronized) {
               switch(v.vt) {
               case LOCAL:
               case CONSTANT:
                  if (v.vt == Value.VT.LOCAL) {
                     key = "L" + ((Local)v)._ls_index;
                  } else {
                     key = "C" + ((Constant)v).value;
                  }

                  integer = (Integer)lockMap.get(key);
                  int var10000;
                  if (integer != null) {
                     var10000 = integer;
                  } else {
                     ++maxLocalIndex;
                     var10000 = maxLocalIndex;
                  }

                  int nIndex = var10000;
                  asm.visitInsn(89);
                  asm.visitVarInsn(getOpcode((Value)v, 54), nIndex);
                  lockMap.put(key, nIndex);
                  break;
               default:
                  throw new RuntimeException();
               }
            }

            asm.visitInsn(194);
            break;
         case UNLOCK:
            v = ((UnopStmt)st).op;
            if (this.optimizeSynchronized) {
               switch(v.vt) {
               case LOCAL:
               case CONSTANT:
                  if (v.vt == Value.VT.LOCAL) {
                     key = "L" + ((Local)v)._ls_index;
                  } else {
                     key = "C" + ((Constant)v).value;
                  }

                  integer = (Integer)lockMap.get(key);
                  if (integer != null) {
                     asm.visitVarInsn(getOpcode((Value)v, 21), integer);
                  } else {
                     accept(v, asm);
                  }
                  break;
               default:
                  accept(v, asm);
               }
            } else {
               accept(v, asm);
            }

            asm.visitInsn(195);
         case NOP:
            break;
         case RETURN:
            v = ((UnopStmt)st).op;
            accept(v, asm);
            insertI2x(v.valueType, ir.ret, asm);
            asm.visitInsn(getOpcode((Value)v, 172));
            break;
         case RETURN_VOID:
            asm.visitInsn(177);
            break;
         case LOOKUP_SWITCH:
            LookupSwitchStmt lss = (LookupSwitchStmt)st;
            accept(lss.op, asm);
            targets = new Label[lss.targets.length];

            for(arraySize = 0; arraySize < targets.length; ++arraySize) {
               targets[arraySize] = (Label)lss.targets[arraySize].tag;
            }

            asm.visitLookupSwitchInsn((Label)lss.defaultTarget.tag, lss.lookupValues, targets);
            break;
         case TABLE_SWITCH:
            TableSwitchStmt tss = (TableSwitchStmt)st;
            accept(tss.op, asm);
            targets = new Label[tss.targets.length];

            for(arraySize = 0; arraySize < targets.length; ++arraySize) {
               targets[arraySize] = (Label)tss.targets[arraySize].tag;
            }

            asm.visitTableSwitchInsn(tss.lowIndex, tss.lowIndex + targets.length - 1, (Label)tss.defaultTarget.tag, targets);
            break;
         case THROW:
            accept(((UnopStmt)st).op, asm);
            asm.visitInsn(191);
            break;
         case VOID_INVOKE:
            v = st.getOp();
            accept(v, asm);
            key = v.valueType;
            if (v.vt == Value.VT.INVOKE_NEW) {
               asm.visitInsn(87);
            } else if (!"V".equals(key)) {
               switch(key.charAt(0)) {
               case 'D':
               case 'J':
                  asm.visitInsn(88);
                  break;
               default:
                  asm.visitInsn(87);
               }
            }
            break;
         default:
            throw new RuntimeException("not support st: " + st.st);
         }
      }

   }

   private static boolean isLocalWithIndex(Value v, int i) {
      return v.vt == Value.VT.LOCAL && ((Local)v)._ls_index == i;
   }

   private static void insertI2x(String tos, String expect, MethodVisitor mv) {
      switch(expect.charAt(0)) {
      case 'B':
         switch(tos.charAt(0)) {
         case 'C':
         case 'I':
         case 'S':
            mv.visitInsn(145);
            return;
         default:
            return;
         }
      case 'C':
         switch(tos.charAt(0)) {
         case 'I':
            mv.visitInsn(146);
            return;
         default:
            return;
         }
      case 'S':
         switch(tos.charAt(0)) {
         case 'C':
         case 'I':
            mv.visitInsn(147);
         }
      }

   }

   static boolean isZeroOrNull(Value v1) {
      if (v1.vt != Value.VT.CONSTANT) {
         return false;
      } else {
         Object v = ((Constant)v1).value;
         return Integer.valueOf(0).equals(v) || Constant.Null.equals(v);
      }
   }

   private void reBuildJumpInstructions(IfStmt st, MethodVisitor asm) {
      Label target = (Label)st.target.tag;
      Value v = st.op;
      Value v1 = v.getOp1();
      Value v2 = v.getOp2();
      String type = v1.valueType;
      switch(type.charAt(0)) {
      case 'L':
      case '[':
         if (!isZeroOrNull(v1) && !isZeroOrNull(v2)) {
            accept(v1, asm);
            accept(v2, asm);
            asm.visitJumpInsn(v.vt == Value.VT.EQ ? 165 : 166, target);
         } else {
            if (isZeroOrNull(v2)) {
               accept(v1, asm);
            } else {
               accept(v2, asm);
            }

            asm.visitJumpInsn(v.vt == Value.VT.EQ ? 198 : 199, target);
         }
         break;
      default:
         if (!isZeroOrNull(v1) && !isZeroOrNull(v2)) {
            accept(v1, asm);
            accept(v2, asm);
            switch(v.vt) {
            case NE:
               asm.visitJumpInsn(160, target);
               break;
            case EQ:
               asm.visitJumpInsn(159, target);
               break;
            case GE:
               asm.visitJumpInsn(162, target);
               break;
            case GT:
               asm.visitJumpInsn(163, target);
               break;
            case LE:
               asm.visitJumpInsn(164, target);
               break;
            case LT:
               asm.visitJumpInsn(161, target);
            }
         } else {
            if (isZeroOrNull(v2)) {
               accept(v1, asm);
            } else {
               accept(v2, asm);
            }

            switch(v.vt) {
            case NE:
               asm.visitJumpInsn(154, target);
               break;
            case EQ:
               asm.visitJumpInsn(153, target);
               break;
            case GE:
               asm.visitJumpInsn(156, target);
               break;
            case GT:
               asm.visitJumpInsn(157, target);
               break;
            case LE:
               asm.visitJumpInsn(158, target);
               break;
            case LT:
               asm.visitJumpInsn(155, target);
            }
         }
      }

   }

   static int getOpcode(Value v, int op) {
      return getOpcode(v.valueType, op);
   }

   static int getOpcode(String v, int op) {
      switch(v.charAt(0)) {
      case 'B':
         return Type.BYTE_TYPE.getOpcode(op);
      case 'C':
         return Type.CHAR_TYPE.getOpcode(op);
      case 'D':
         return Type.DOUBLE_TYPE.getOpcode(op);
      case 'E':
      case 'G':
      case 'H':
      case 'K':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      default:
         return Type.INT_TYPE.getOpcode(op);
      case 'F':
         return Type.FLOAT_TYPE.getOpcode(op);
      case 'I':
         return Type.INT_TYPE.getOpcode(op);
      case 'J':
         return Type.LONG_TYPE.getOpcode(op);
      case 'L':
      case '[':
         return Type.getType("La;").getOpcode(op);
      case 'S':
         return Type.SHORT_TYPE.getOpcode(op);
      case 'Z':
         return Type.BOOLEAN_TYPE.getOpcode(op);
      }
   }

   private static void accept(Value value, MethodVisitor asm) {
      switch(value.et) {
      case E0:
         switch(value.vt) {
         case LOCAL:
            asm.visitVarInsn(getOpcode((Value)value, 21), ((Local)value)._ls_index);
            return;
         case STATIC_FIELD:
            StaticFieldExpr sfe = (StaticFieldExpr)value;
            asm.visitFieldInsn(178, toInternal(sfe.owner), sfe.name, sfe.type);
            return;
         case CONSTANT:
            Constant cst = (Constant)value;
            if (cst.value.equals(Constant.Null)) {
               asm.visitInsn(1);
               return;
            } else {
               if (cst.value instanceof DexType) {
                  asm.visitLdcInsn(Type.getType(((DexType)cst.value).desc));
               } else {
                  asm.visitLdcInsn(cst.value);
               }

               return;
            }
         case NEW:
            asm.visitTypeInsn(187, toInternal(((NewExpr)value).type));
            return;
         default:
            return;
         }
      case E1:
         reBuildE1Expression((Value.E1Expr)value, asm);
         break;
      case E2:
         reBuildE2Expression((Value.E2Expr)value, asm);
         break;
      case En:
         reBuildEnExpression((Value.EnExpr)value, asm);
      }

   }

   private static void reBuildEnExpression(Value.EnExpr value, MethodVisitor asm) {
      int xastore;
      int j;
      if (value.vt == Value.VT.FILLED_ARRAY) {
         FilledArrayExpr fae = (FilledArrayExpr)value;
         reBuildE1Expression(Exprs.nNewArray(fae.type, Exprs.nInt(fae.ops.length)), asm);
         String tp1 = fae.valueType;
         xastore = 79;
         String elementType = null;
         if (tp1.charAt(0) == '[') {
            elementType = tp1.substring(1);
            xastore = getOpcode((String)elementType, 79);
         }

         for(j = 0; j < fae.ops.length; ++j) {
            if (fae.ops[j] != null) {
               asm.visitInsn(89);
               asm.visitLdcInsn(j);
               accept(fae.ops[j], asm);
               String tp2 = fae.ops[j].valueType;
               if (elementType != null) {
                  insertI2x(tp2, elementType, asm);
               }

               asm.visitInsn(xastore);
            }
         }

      } else {
         switch(value.vt) {
         case INVOKE_NEW:
            asm.visitTypeInsn(187, toInternal(((InvokeExpr)value).getOwner()));
            asm.visitInsn(89);
         case INVOKE_VIRTUAL:
         case INVOKE_INTERFACE:
         case INVOKE_SPECIAL:
         case INVOKE_STATIC:
            InvokeExpr ie = (InvokeExpr)value;
            int i = 0;
            if (value.vt != Value.VT.INVOKE_STATIC && value.vt != Value.VT.INVOKE_NEW) {
               i = 1;
               accept(value.ops[0], asm);
            }

            for(j = 0; i < value.ops.length; ++j) {
               Value vb = value.ops[i];
               accept(vb, asm);
               insertI2x(vb.valueType, ie.getArgs()[j], asm);
               ++i;
            }

            short opcode;
            switch(value.vt) {
            case INVOKE_VIRTUAL:
               opcode = 182;
               break;
            case INVOKE_INTERFACE:
               opcode = 185;
               break;
            case INVOKE_NEW:
            case INVOKE_SPECIAL:
               opcode = 183;
               break;
            case INVOKE_STATIC:
               opcode = 184;
               break;
            default:
               opcode = -1;
            }

            Proto p = ie.getProto();
            if (ie.vt == Value.VT.INVOKE_NEW) {
               p = new Proto(p.getParameterTypes(), "V");
            }

            asm.visitMethodInsn(opcode, toInternal(ie.getOwner()), ie.getName(), p.getDesc());
            break;
         case NEW_MUTI_ARRAY:
            Value[] var2 = value.ops;
            int var3 = var2.length;

            for(xastore = 0; xastore < var3; ++xastore) {
               Value vb = var2[xastore];
               accept(vb, asm);
            }

            NewMutiArrayExpr nmae = (NewMutiArrayExpr)value;
            StringBuilder sb = new StringBuilder();

            for(xastore = 0; xastore < nmae.dimension; ++xastore) {
               sb.append('[');
            }

            sb.append(nmae.baseType);
            asm.visitMultiANewArrayInsn(sb.toString(), value.ops.length);
            break;
         case INVOKE_CUSTOM:
            InvokeCustomExpr ice = (InvokeCustomExpr)value;
            String[] argTypes = ice.getProto().getParameterTypes();
            Value[] vbs = ice.getOps();
            int i;
            Value vb;
            if (argTypes.length == vbs.length) {
               for(i = 0; i < vbs.length; ++i) {
                  vb = vbs[i];
                  accept(vb, asm);
                  insertI2x(vb.valueType, argTypes[i], asm);
               }
            } else {
               if (argTypes.length + 1 != vbs.length) {
                  throw new RuntimeException();
               }

               accept(vbs[0], asm);

               for(i = 1; i < vbs.length; ++i) {
                  vb = vbs[i];
                  accept(vb, asm);
                  insertI2x(vb.valueType, argTypes[i - 1], asm);
               }
            }

            asm.visitInvokeDynamicInsn(ice.name, ice.proto.getDesc(), (Handle)Dex2Asm.convertConstantValue(ice.handle), Dex2Asm.convertConstantValues(ice.bsmArgs));
            break;
         case INVOKE_POLYMORPHIC:
            InvokePolymorphicExpr ipe = (InvokePolymorphicExpr)value;
            Method m = ipe.method;
            String[] argTypes = ipe.getProto().getParameterTypes();
            Value[] vbs = ipe.getOps();
            accept(vbs[0], asm);

            for(int i = 1; i < vbs.length; ++i) {
               Value vb = vbs[i];
               accept(vb, asm);
               insertI2x(vb.valueType, argTypes[i - 1], asm);
            }

            asm.visitMethodInsn(182, toInternal(m.getOwner()), m.getName(), ipe.getProto().getDesc(), false);
         }

      }
   }

   private static void box(String provideType, String expectedType, MethodVisitor asm) {
      if (!provideType.equals(expectedType)) {
         if (expectedType.equals("V")) {
            switch(provideType.charAt(0)) {
            case 'D':
            case 'J':
               asm.visitInsn(88);
               break;
            default:
               asm.visitInsn(87);
            }

         } else {
            char p = provideType.charAt(0);
            char e = expectedType.charAt(0);
            if (!expectedType.equals("Ljava/lang/Object;") || p != '[' && p != 'L') {
               if (provideType.equals("Ljava/lang/Object;") && (e == '[' || e == 'L')) {
                  asm.visitTypeInsn(192, toInternal(expectedType));
               } else {
                  String var5 = provideType + expectedType;
                  byte var6 = -1;
                  switch(var5.hashCode()) {
                  case -2016902897:
                     if (var5.equals("CLjava/lang/Character;")) {
                        var6 = 7;
                     }
                     break;
                  case -1884772826:
                     if (var5.equals("Ljava/lang/Object;B")) {
                        var6 = 18;
                     }
                     break;
                  case -1884772825:
                     if (var5.equals("Ljava/lang/Object;C")) {
                        var6 = 22;
                     }
                     break;
                  case -1884772824:
                     if (var5.equals("Ljava/lang/Object;D")) {
                        var6 = 30;
                     }
                     break;
                  case -1884772822:
                     if (var5.equals("Ljava/lang/Object;F")) {
                        var6 = 26;
                     }
                     break;
                  case -1884772819:
                     if (var5.equals("Ljava/lang/Object;I")) {
                        var6 = 24;
                     }
                     break;
                  case -1884772818:
                     if (var5.equals("Ljava/lang/Object;J")) {
                        var6 = 28;
                     }
                     break;
                  case -1884772809:
                     if (var5.equals("Ljava/lang/Object;S")) {
                        var6 = 20;
                     }
                     break;
                  case -1884772802:
                     if (var5.equals("Ljava/lang/Object;Z")) {
                        var6 = 16;
                     }
                     break;
                  case -1799992441:
                     if (var5.equals("Ljava/lang/Float;F")) {
                        var6 = 27;
                     }
                     break;
                  case -1676687262:
                     if (var5.equals("FLjava/lang/Object;")) {
                        var6 = 10;
                     }
                     break;
                  case -1386255498:
                     if (var5.equals("ZLjava/lang/Object;")) {
                        var6 = 0;
                     }
                     break;
                  case -1184590060:
                     if (var5.equals("ILjava/lang/Integer;")) {
                        var6 = 9;
                     }
                     break;
                  case -1075794359:
                     if (var5.equals("JLjava/lang/Long;")) {
                        var6 = 13;
                     }
                     break;
                  case -778092914:
                     if (var5.equals("DLjava/lang/Double;")) {
                        var6 = 15;
                     }
                     break;
                  case -759607450:
                     if (var5.equals("JLjava/lang/Object;")) {
                        var6 = 12;
                     }
                     break;
                  case -615800262:
                     if (var5.equals("Ljava/lang/Double;D")) {
                        var6 = 31;
                     }
                     break;
                  case -611300884:
                     if (var5.equals("Ljava/lang/Integer;I")) {
                        var6 = 25;
                     }
                     break;
                  case 12256480:
                     if (var5.equals("DLjava/lang/Object;")) {
                        var6 = 14;
                     }
                     break;
                  case 84864421:
                     if (var5.equals("ILjava/lang/Object;")) {
                        var6 = 8;
                     }
                     break;
                  case 105678097:
                     if (var5.equals("Ljava/lang/Character;C")) {
                        var6 = 23;
                     }
                     break;
                  case 230080303:
                     if (var5.equals("SLjava/lang/Object;")) {
                        var6 = 4;
                     }
                     break;
                  case 528915737:
                     if (var5.equals("FLjava/lang/Float;")) {
                        var6 = 11;
                     }
                     break;
                  case 820378252:
                     if (var5.equals("SLjava/lang/Short;")) {
                        var6 = 5;
                     }
                     break;
                  case 856728351:
                     if (var5.equals("CLjava/lang/Object;")) {
                        var6 = 6;
                     }
                     break;
                  case 954671719:
                     if (var5.equals("Ljava/lang/Boolean;Z")) {
                        var6 = 17;
                     }
                     break;
                  case 973698357:
                     if (var5.equals("BLjava/lang/Byte;")) {
                        var6 = 3;
                     }
                     break;
                  case 1033610676:
                     if (var5.equals("Ljava/lang/Short;S")) {
                        var6 = 21;
                     }
                     break;
                  case 1502143919:
                     if (var5.equals("Ljava/lang/Byte;B")) {
                        var6 = 19;
                     }
                     break;
                  case 1701200222:
                     if (var5.equals("BLjava/lang/Object;")) {
                        var6 = 2;
                     }
                     break;
                  case 1779023403:
                     if (var5.equals("Ljava/lang/Long;J")) {
                        var6 = 29;
                     }
                     break;
                  case 1891321273:
                     if (var5.equals("ZLjava/lang/Boolean;")) {
                        var6 = 1;
                     }
                  }

                  switch(var6) {
                  case 0:
                  case 1:
                     asm.visitMethodInsn(184, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                     break;
                  case 2:
                  case 3:
                     asm.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                     break;
                  case 4:
                  case 5:
                     asm.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                     break;
                  case 6:
                  case 7:
                     asm.visitMethodInsn(184, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                     break;
                  case 8:
                  case 9:
                     asm.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                     break;
                  case 10:
                  case 11:
                     asm.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                     break;
                  case 12:
                  case 13:
                     asm.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                     break;
                  case 14:
                  case 15:
                     asm.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                     break;
                  case 16:
                     asm.visitTypeInsn(192, "java/lang/Boolean");
                  case 17:
                     asm.visitMethodInsn(182, "java/lang/Boolean", "booleanValue", "()Z", false);
                     break;
                  case 18:
                     asm.visitTypeInsn(192, "java/lang/Byte");
                  case 19:
                     asm.visitMethodInsn(182, "java/lang/Byte", "byteValue", "()B", false);
                     break;
                  case 20:
                     asm.visitTypeInsn(192, "java/lang/Short");
                  case 21:
                     asm.visitMethodInsn(182, "java/lang/Short", "shortValue", "()S", false);
                     break;
                  case 22:
                     asm.visitTypeInsn(192, "java/lang/Character");
                  case 23:
                     asm.visitMethodInsn(182, "java/lang/Character", "charValue", "()C", false);
                     break;
                  case 24:
                     asm.visitTypeInsn(192, "java/lang/Integer");
                  case 25:
                     asm.visitMethodInsn(182, "java/lang/Integer", "intValue", "()I", false);
                     break;
                  case 26:
                     asm.visitTypeInsn(192, "java/lang/Float");
                  case 27:
                     asm.visitMethodInsn(182, "java/lang/Float", "floatValue", "()F", false);
                     break;
                  case 28:
                     asm.visitTypeInsn(192, "java/lang/Long");
                  case 29:
                     asm.visitMethodInsn(182, "java/lang/Long", "longValue", "()J", false);
                     break;
                  case 30:
                     asm.visitTypeInsn(192, "java/lang/Double");
                  case 31:
                     asm.visitMethodInsn(182, "java/lang/Double", "doubleValue", "()D", false);
                     break;
                  default:
                     throw new RuntimeException("i have trouble to auto convert from " + provideType + " to " + expectedType + " currently");
                  }

               }
            }
         }
      }
   }

   private static void reBuildE1Expression(Value.E1Expr e1, MethodVisitor asm) {
      accept(e1.getOp(), asm);
      TypeExpr te;
      FieldExpr fe;
      switch(e1.vt) {
      case STATIC_FIELD:
         fe = (FieldExpr)e1;
         asm.visitFieldInsn(178, toInternal(fe.owner), fe.name, fe.type);
         break;
      case FIELD:
         fe = (FieldExpr)e1;
         asm.visitFieldInsn(180, toInternal(fe.owner), fe.name, fe.type);
      case ARRAY:
      case CONSTANT:
      case NE:
      case EQ:
      case GE:
      case GT:
      case LE:
      case LT:
      case NEW:
      case INVOKE_VIRTUAL:
      case INVOKE_INTERFACE:
      case INVOKE_NEW:
      case INVOKE_SPECIAL:
      case INVOKE_STATIC:
      case NEW_MUTI_ARRAY:
      case INVOKE_CUSTOM:
      case INVOKE_POLYMORPHIC:
      default:
         break;
      case NEW_ARRAY:
         te = (TypeExpr)e1;
         switch(te.type.charAt(0)) {
         case 'B':
            asm.visitIntInsn(188, 8);
            return;
         case 'C':
            asm.visitIntInsn(188, 5);
            return;
         case 'D':
            asm.visitIntInsn(188, 7);
            return;
         case 'E':
         case 'G':
         case 'H':
         case 'K':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'T':
         case 'U':
         case 'V':
         case 'W':
         case 'X':
         case 'Y':
         default:
            return;
         case 'F':
            asm.visitIntInsn(188, 6);
            return;
         case 'I':
            asm.visitIntInsn(188, 10);
            return;
         case 'J':
            asm.visitIntInsn(188, 11);
            return;
         case 'L':
         case '[':
            asm.visitTypeInsn(189, toInternal(te.type));
            return;
         case 'S':
            asm.visitIntInsn(188, 9);
            return;
         case 'Z':
            asm.visitIntInsn(188, 4);
            return;
         }
      case CHECK_CAST:
      case INSTANCE_OF:
         te = (TypeExpr)e1;
         asm.visitTypeInsn(e1.vt == Value.VT.CHECK_CAST ? 192 : 193, toInternal(te.type));
         break;
      case CAST:
         CastExpr te = (CastExpr)e1;
         cast2(e1.op.valueType, te.to, asm);
         break;
      case LENGTH:
         asm.visitInsn(190);
         break;
      case NEG:
         asm.visitInsn(getOpcode((Value)e1, 116));
      }

   }

   private static void reBuildE2Expression(Value.E2Expr e2, MethodVisitor asm) {
      String type = e2.op2.valueType;
      accept(e2.op1, asm);
      String tp2;
      if ((e2.vt == Value.VT.ADD || e2.vt == Value.VT.SUB) && e2.op2.vt == Value.VT.CONSTANT) {
         Constant constant = (Constant)e2.op2;
         tp2 = constant.valueType;
         switch(tp2.charAt(0)) {
         case 'B':
         case 'I':
         case 'S':
            int s = (Integer)constant.value;
            if (s < 0) {
               asm.visitLdcInsn(-s);
               asm.visitInsn(getOpcode(type, e2.vt == Value.VT.ADD ? 100 : 96));
               return;
            }
         case 'C':
         case 'E':
         case 'G':
         case 'H':
         case 'K':
         case 'L':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         default:
            break;
         case 'D':
            double s = (Double)constant.value;
            if (s < 0.0D) {
               asm.visitLdcInsn(-s);
               asm.visitInsn(getOpcode(type, e2.vt == Value.VT.ADD ? 100 : 96));
               return;
            }
            break;
         case 'F':
            float s = (Float)constant.value;
            if (s < 0.0F) {
               asm.visitLdcInsn(-s);
               asm.visitInsn(getOpcode(type, e2.vt == Value.VT.ADD ? 100 : 96));
               return;
            }
            break;
         case 'J':
            long s = (Long)constant.value;
            if (s < 0L) {
               asm.visitLdcInsn(-s);
               asm.visitInsn(getOpcode(type, e2.vt == Value.VT.ADD ? 100 : 96));
               return;
            }
         }
      }

      accept(e2.op2, asm);
      String tp1 = e2.op1.valueType;
      switch(e2.vt) {
      case ARRAY:
         tp2 = e2.valueType;
         if (tp1.charAt(0) == '[') {
            asm.visitInsn(getOpcode((String)tp1.substring(1), 46));
         } else {
            asm.visitInsn(getOpcode((String)tp2, 46));
         }
      case CONSTANT:
      case NE:
      case EQ:
      case GE:
      case GT:
      case LE:
      case LT:
      case NEW:
      case INVOKE_VIRTUAL:
      case INVOKE_INTERFACE:
      case INVOKE_NEW:
      case INVOKE_SPECIAL:
      case INVOKE_STATIC:
      case NEW_MUTI_ARRAY:
      case INVOKE_CUSTOM:
      case INVOKE_POLYMORPHIC:
      case NEW_ARRAY:
      case CHECK_CAST:
      case INSTANCE_OF:
      case CAST:
      case LENGTH:
      case NEG:
      default:
         break;
      case ADD:
         asm.visitInsn(getOpcode((String)type, 96));
         break;
      case SUB:
         asm.visitInsn(getOpcode((String)type, 100));
         break;
      case IDIV:
      case LDIV:
      case FDIV:
      case DDIV:
         asm.visitInsn(getOpcode((String)type, 108));
         break;
      case MUL:
         asm.visitInsn(getOpcode((String)type, 104));
         break;
      case REM:
         asm.visitInsn(getOpcode((String)type, 112));
         break;
      case AND:
         asm.visitInsn(getOpcode((String)type, 126));
         break;
      case OR:
         asm.visitInsn(getOpcode((String)type, 128));
         break;
      case XOR:
         asm.visitInsn(getOpcode((String)type, 130));
         break;
      case SHL:
         asm.visitInsn(getOpcode((String)tp1, 120));
         break;
      case SHR:
         asm.visitInsn(getOpcode((String)tp1, 122));
         break;
      case USHR:
         asm.visitInsn(getOpcode((String)tp1, 124));
         break;
      case LCMP:
         asm.visitInsn(148);
         break;
      case FCMPG:
         asm.visitInsn(150);
         break;
      case DCMPG:
         asm.visitInsn(152);
         break;
      case FCMPL:
         asm.visitInsn(149);
         break;
      case DCMPL:
         asm.visitInsn(151);
      }

   }

   private static void cast2(String t1, String t2, MethodVisitor asm) {
      if (!t1.equals(t2)) {
         switch(t1.charAt(0)) {
         case 'B':
         case 'C':
         case 'I':
         case 'S':
         case 'Z':
            switch(t2.charAt(0)) {
            case 'B':
               asm.visitInsn(145);
               return;
            case 'C':
               asm.visitInsn(146);
               return;
            case 'D':
               asm.visitInsn(135);
               return;
            case 'E':
            case 'G':
            case 'H':
            case 'I':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            default:
               return;
            case 'F':
               asm.visitInsn(134);
               return;
            case 'J':
               asm.visitInsn(133);
               return;
            case 'S':
               asm.visitInsn(147);
               return;
            }
         case 'D':
            switch(t2.charAt(0)) {
            case 'F':
               asm.visitInsn(144);
            case 'G':
            case 'H':
            default:
               break;
            case 'I':
               asm.visitInsn(142);
               break;
            case 'J':
               asm.visitInsn(143);
            }
         case 'E':
         case 'G':
         case 'H':
         case 'K':
         case 'L':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'T':
         case 'U':
         case 'V':
         case 'W':
         case 'X':
         case 'Y':
         default:
            break;
         case 'F':
            switch(t2.charAt(0)) {
            case 'D':
               asm.visitInsn(141);
               return;
            case 'I':
               asm.visitInsn(139);
               return;
            case 'J':
               asm.visitInsn(140);
               return;
            default:
               return;
            }
         case 'J':
            switch(t2.charAt(0)) {
            case 'D':
               asm.visitInsn(138);
               break;
            case 'F':
               asm.visitInsn(137);
               break;
            case 'I':
               asm.visitInsn(136);
            }
         }

      }
   }
}
