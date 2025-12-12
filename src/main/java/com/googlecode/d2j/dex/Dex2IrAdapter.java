package com.googlecode.d2j.dex;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexCodeNode;
import com.googlecode.d2j.node.TryCatchNode;
import com.googlecode.d2j.node.insn.DexLabelStmtNode;
import com.googlecode.d2j.node.insn.DexStmtNode;
import com.googlecode.d2j.node.insn.FilledNewArrayStmtNode;
import com.googlecode.d2j.node.insn.MethodStmtNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.TypeClass;
import com.googlecode.dex2jar.ir.expr.BinopExpr;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Opcodes;

public class Dex2IrAdapter extends DexCodeVisitor implements Opcodes, DexConstants {
   protected IrMethod irMethod;
   private Method method;
   private boolean isStatic;
   private StmtList list;
   private Local[] locals;
   Map<DexLabel, LabelStmt> labelStmtMap = new HashMap();
   private Local tmpLocal;
   boolean lastIsInvokeOrFilledNewArray = false;

   public Dex2IrAdapter(boolean isStatic, Method method) {
      IrMethod irMethod = new IrMethod();
      irMethod.args = method.getParameterTypes();
      irMethod.ret = method.getReturnType();
      irMethod.owner = method.getOwner();
      irMethod.name = method.getName();
      irMethod.isStatic = isStatic;
      this.irMethod = irMethod;
      this.list = irMethod.stmts;
      this.irMethod = irMethod;
      this.method = method;
      this.isStatic = isStatic;
   }

   private LabelStmt toLabelStmt(DexLabel label) {
      LabelStmt ls = (LabelStmt)this.labelStmtMap.get(label);
      if (ls == null) {
         ls = new LabelStmt();
         this.labelStmtMap.put(label, ls);
      }

      return ls;
   }

   static int countParameterRegisters(Method m, boolean isStatic) {
      int a = isStatic ? 0 : 1;
      String[] var3 = m.getParameterTypes();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String t = var3[var5];
         switch(t.charAt(0)) {
         case 'D':
         case 'J':
            a += 2;
            break;
         default:
            ++a;
         }
      }

      return a;
   }

   void x(Stmt stmt) {
      this.list.add(stmt);
   }

   public void visitRegister(int total) {
      Local[] locals = new Local[total];
      this.locals = locals;
      this.tmpLocal = new Local(total);

      int nextReg;
      for(nextReg = 0; nextReg < locals.length; ++nextReg) {
         locals[nextReg] = new Local(nextReg);
      }

      nextReg = total - countParameterRegisters(this.method, this.isStatic);
      int nextReg0 = nextReg;
      if (!this.isStatic) {
         this.x(Stmts.nIdentity(locals[nextReg], Exprs.nThisRef(this.method.getOwner())));
         ++nextReg;
      }

      String[] args = this.method.getParameterTypes();

      int i;
      for(i = 0; i < args.length; ++i) {
         String t = args[i];
         this.x(Stmts.nIdentity(locals[nextReg], Exprs.nParameterRef(t, i)));
         ++nextReg;
         if (t.equals("J") || t.equals("D")) {
            ++nextReg;
         }
      }

      for(i = 0; i < nextReg0; ++i) {
         this.x(Stmts.nAssign(locals[i], Exprs.nInt(0)));
      }

      this.x(Stmts.nAssign(this.tmpLocal, Exprs.nInt(0)));
   }

   public void visitStmt2R1N(Op op, int a, int b, int content) {
      Local va = this.locals[a];
      Local vb = this.locals[b];
      BinopExpr to;
      switch(op) {
      case ADD_INT_LIT16:
      case ADD_INT_LIT8:
         to = Exprs.nAdd(vb, Exprs.nInt(content), "I");
         break;
      case RSUB_INT_LIT8:
      case RSUB_INT:
         to = Exprs.nSub(Exprs.nInt(content), vb, "I");
         break;
      case MUL_INT_LIT8:
      case MUL_INT_LIT16:
         to = Exprs.nMul(vb, Exprs.nInt(content), "I");
         break;
      case DIV_INT_LIT16:
      case DIV_INT_LIT8:
         to = Exprs.nDiv(vb, Exprs.nInt(content), "I");
         break;
      case REM_INT_LIT16:
      case REM_INT_LIT8:
         to = Exprs.nRem(vb, Exprs.nInt(content), "I");
         break;
      case AND_INT_LIT16:
      case AND_INT_LIT8:
         to = Exprs.nAnd(vb, Exprs.nInt(content), content >= 0 && content <= 1 ? TypeClass.ZI.name : "I");
         break;
      case OR_INT_LIT16:
      case OR_INT_LIT8:
         to = Exprs.nOr(vb, Exprs.nInt(content), content >= 0 && content <= 1 ? TypeClass.ZI.name : "I");
         break;
      case XOR_INT_LIT16:
      case XOR_INT_LIT8:
         to = Exprs.nXor(vb, Exprs.nInt(content), content >= 0 && content <= 1 ? TypeClass.ZI.name : "I");
         break;
      case SHL_INT_LIT8:
         to = Exprs.nShl(vb, Exprs.nInt(content), "I");
         break;
      case SHR_INT_LIT8:
         to = Exprs.nShr(vb, Exprs.nInt(content), "I");
         break;
      case USHR_INT_LIT8:
         to = Exprs.nUshr(vb, Exprs.nInt(content), "I");
         break;
      default:
         throw new RuntimeException();
      }

      this.x(Stmts.nAssign(va, to));
   }

   public void visitStmt3R(Op op, int a, int b, int c) {
      Value va = this.locals[a];
      Value vb = this.locals[b];
      Value vc = this.locals[c];
      switch(op) {
      case APUT:
         this.x(Stmts.nAssign(Exprs.nArray(vb, vc, TypeClass.IF.name), va));
         break;
      case APUT_BOOLEAN:
         this.x(Stmts.nAssign(Exprs.nArray(vb, vc, "Z"), va));
         break;
      case APUT_BYTE:
         this.x(Stmts.nAssign(Exprs.nArray(vb, vc, "B"), va));
         break;
      case APUT_CHAR:
         this.x(Stmts.nAssign(Exprs.nArray(vb, vc, "C"), va));
         break;
      case APUT_OBJECT:
         this.x(Stmts.nAssign(Exprs.nArray(vb, vc, "L"), va));
         break;
      case APUT_SHORT:
         this.x(Stmts.nAssign(Exprs.nArray(vb, vc, "S"), va));
         break;
      case APUT_WIDE:
         this.x(Stmts.nAssign(Exprs.nArray(vb, vc, TypeClass.JD.name), va));
         break;
      case AGET:
         this.x(Stmts.nAssign(va, Exprs.nArray(vb, vc, TypeClass.IF.name)));
         break;
      case AGET_BOOLEAN:
         this.x(Stmts.nAssign(va, Exprs.nArray(vb, vc, "Z")));
         break;
      case AGET_BYTE:
         this.x(Stmts.nAssign(va, Exprs.nArray(vb, vc, "B")));
         break;
      case AGET_CHAR:
         this.x(Stmts.nAssign(va, Exprs.nArray(vb, vc, "C")));
         break;
      case AGET_OBJECT:
         this.x(Stmts.nAssign(va, Exprs.nArray(vb, vc, "L")));
         break;
      case AGET_SHORT:
         this.x(Stmts.nAssign(va, Exprs.nArray(vb, vc, "S")));
         break;
      case AGET_WIDE:
         this.x(Stmts.nAssign(va, Exprs.nArray(vb, vc, TypeClass.JD.name)));
         break;
      case CMP_LONG:
         this.x(Stmts.nAssign(va, Exprs.nLCmp(vb, vc)));
         break;
      case CMPG_DOUBLE:
         this.x(Stmts.nAssign(va, Exprs.nDCmpg(vb, vc)));
         break;
      case CMPG_FLOAT:
         this.x(Stmts.nAssign(va, Exprs.nFCmpg(vb, vc)));
         break;
      case CMPL_DOUBLE:
         this.x(Stmts.nAssign(va, Exprs.nDCmpl(vb, vc)));
         break;
      case CMPL_FLOAT:
         this.x(Stmts.nAssign(va, Exprs.nFCmpl(vb, vc)));
         break;
      case ADD_DOUBLE:
         this.x(Stmts.nAssign(va, Exprs.nAdd(vb, vc, "D")));
         break;
      case ADD_FLOAT:
         this.x(Stmts.nAssign(va, Exprs.nAdd(vb, vc, "F")));
         break;
      case ADD_INT:
         this.x(Stmts.nAssign(va, Exprs.nAdd(vb, vc, "I")));
         break;
      case ADD_LONG:
         this.x(Stmts.nAssign(va, Exprs.nAdd(vb, vc, "J")));
         break;
      case SUB_DOUBLE:
         this.x(Stmts.nAssign(va, Exprs.nSub(vb, vc, "D")));
         break;
      case SUB_FLOAT:
         this.x(Stmts.nAssign(va, Exprs.nSub(vb, vc, "F")));
         break;
      case SUB_INT:
         this.x(Stmts.nAssign(va, Exprs.nSub(vb, vc, "I")));
         break;
      case SUB_LONG:
         this.x(Stmts.nAssign(va, Exprs.nSub(vb, vc, "J")));
         break;
      case MUL_DOUBLE:
         this.x(Stmts.nAssign(va, Exprs.nMul(vb, vc, "D")));
         break;
      case MUL_FLOAT:
         this.x(Stmts.nAssign(va, Exprs.nMul(vb, vc, "F")));
         break;
      case MUL_INT:
         this.x(Stmts.nAssign(va, Exprs.nMul(vb, vc, "I")));
         break;
      case MUL_LONG:
         this.x(Stmts.nAssign(va, Exprs.nMul(vb, vc, "J")));
         break;
      case DIV_DOUBLE:
         this.x(Stmts.nAssign(va, Exprs.nDiv(vb, vc, "D")));
         break;
      case DIV_FLOAT:
         this.x(Stmts.nAssign(va, Exprs.nDiv(vb, vc, "F")));
         break;
      case DIV_INT:
         this.x(Stmts.nAssign(va, Exprs.nDiv(vb, vc, "I")));
         break;
      case DIV_LONG:
         this.x(Stmts.nAssign(va, Exprs.nDiv(vb, vc, "J")));
         break;
      case REM_DOUBLE:
         this.x(Stmts.nAssign(va, Exprs.nRem(vb, vc, "D")));
         break;
      case REM_FLOAT:
         this.x(Stmts.nAssign(va, Exprs.nRem(vb, vc, "F")));
         break;
      case REM_INT:
         this.x(Stmts.nAssign(va, Exprs.nRem(vb, vc, "I")));
         break;
      case REM_LONG:
         this.x(Stmts.nAssign(va, Exprs.nRem(vb, vc, "J")));
         break;
      case AND_INT:
         this.x(Stmts.nAssign(va, Exprs.nAnd(vb, vc, TypeClass.ZI.name)));
         break;
      case AND_LONG:
         this.x(Stmts.nAssign(va, Exprs.nAnd(vb, vc, "J")));
         break;
      case OR_INT:
         this.x(Stmts.nAssign(va, Exprs.nOr(vb, vc, TypeClass.ZI.name)));
         break;
      case OR_LONG:
         this.x(Stmts.nAssign(va, Exprs.nOr(vb, vc, "J")));
         break;
      case XOR_INT:
         this.x(Stmts.nAssign(va, Exprs.nXor(vb, vc, TypeClass.ZI.name)));
         break;
      case XOR_LONG:
         this.x(Stmts.nAssign(va, Exprs.nXor(vb, vc, "J")));
         break;
      case SHL_INT:
         this.x(Stmts.nAssign(va, Exprs.nShl(vb, vc, "I")));
         break;
      case SHL_LONG:
         this.x(Stmts.nAssign(va, Exprs.nShl(vb, vc, "J")));
         break;
      case SHR_INT:
         this.x(Stmts.nAssign(va, Exprs.nShr(vb, vc, "I")));
         break;
      case SHR_LONG:
         this.x(Stmts.nAssign(va, Exprs.nShr(vb, vc, "J")));
         break;
      case USHR_INT:
         this.x(Stmts.nAssign(va, Exprs.nUshr(vb, vc, "I")));
         break;
      case USHR_LONG:
         this.x(Stmts.nAssign(va, Exprs.nUshr(vb, vc, "J")));
         break;
      default:
         throw new RuntimeException();
      }

   }

   public void visitTypeStmt(Op op, int a, int b, String type) {
      switch(op) {
      case INSTANCE_OF:
         this.list.add(Stmts.nAssign(this.locals[a], Exprs.nInstanceOf(this.locals[b], type)));
         break;
      case NEW_ARRAY:
         this.list.add(Stmts.nAssign(this.locals[a], Exprs.nNewArray(type.substring(1), this.locals[b])));
         break;
      case CHECK_CAST:
         this.list.add(Stmts.nAssign(this.locals[a], Exprs.nCheckCast(this.locals[a], type)));
         break;
      case NEW_INSTANCE:
         this.list.add(Stmts.nAssign(this.locals[a], Exprs.nNew(type)));
         break;
      default:
         throw new RuntimeException();
      }

   }

   public void visitFillArrayDataStmt(Op op, int ra, Object array) {
      this.x(Stmts.nFillArrayData(this.locals[ra], Exprs.nArrayValue(array)));
   }

   public void visitConstStmt(Op op, int toReg, Object value) {
      switch(op) {
      case CONST:
      case CONST_16:
      case CONST_4:
      case CONST_HIGH16:
         this.x(Stmts.nAssign(this.locals[toReg], Exprs.nInt((Integer)value)));
         break;
      case CONST_WIDE:
      case CONST_WIDE_16:
      case CONST_WIDE_32:
      case CONST_WIDE_HIGH16:
         this.x(Stmts.nAssign(this.locals[toReg], Exprs.nLong((Long)value)));
         break;
      case CONST_CLASS:
         this.x(Stmts.nAssign(this.locals[toReg], Exprs.nType((DexType)value)));
         break;
      case CONST_STRING:
      case CONST_STRING_JUMBO:
         this.x(Stmts.nAssign(this.locals[toReg], Exprs.nString((String)value)));
         break;
      default:
         throw new RuntimeException();
      }

   }

   public void visitEnd() {
      this.irMethod.locals.addAll(Arrays.asList(this.locals));
      this.irMethod.locals.add(this.tmpLocal);
      this.locals = null;
   }

   public void visitFieldStmt(Op op, int a, int b, Field field) {
      switch(op) {
      case IGET:
      case IGET_BOOLEAN:
      case IGET_BYTE:
      case IGET_CHAR:
      case IGET_OBJECT:
      case IGET_SHORT:
      case IGET_WIDE:
         this.list.add(Stmts.nAssign(this.locals[a], Exprs.nField(this.locals[b], field.getOwner(), field.getName(), field.getType())));
         break;
      case IPUT:
      case IPUT_BOOLEAN:
      case IPUT_BYTE:
      case IPUT_CHAR:
      case IPUT_OBJECT:
      case IPUT_SHORT:
      case IPUT_WIDE:
         this.list.add(Stmts.nAssign(Exprs.nField(this.locals[b], field.getOwner(), field.getName(), field.getType()), this.locals[a]));
         break;
      case SGET:
      case SGET_BOOLEAN:
      case SGET_BYTE:
      case SGET_CHAR:
      case SGET_OBJECT:
      case SGET_SHORT:
      case SGET_WIDE:
         this.list.add(Stmts.nAssign(this.locals[a], Exprs.nStaticField(field.getOwner(), field.getName(), field.getType())));
         break;
      case SPUT:
      case SPUT_BOOLEAN:
      case SPUT_BYTE:
      case SPUT_CHAR:
      case SPUT_OBJECT:
      case SPUT_SHORT:
      case SPUT_WIDE:
         this.list.add(Stmts.nAssign(Exprs.nStaticField(field.getOwner(), field.getName(), field.getType()), this.locals[a]));
         break;
      default:
         throw new RuntimeException();
      }

   }

   public void visitFilledNewArrayStmt(Op opc, int[] args, String type) {
      Local array = this.tmpLocal;
      String elem = type.substring(1);
      this.list.add(Stmts.nAssign(array, Exprs.nNewArray(elem, Exprs.nInt(args.length))));

      for(int i = 0; i < args.length; ++i) {
         this.list.add(Stmts.nAssign(Exprs.nArray(array, Exprs.nInt(i), elem), this.locals[args[i]]));
      }

   }

   public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
      switch(op) {
      case GOTO:
      case GOTO_16:
      case GOTO_32:
         this.x(Stmts.nGoto(this.toLabelStmt(label)));
         break;
      case IF_EQ:
         this.x(Stmts.nIf(Exprs.nEq(this.locals[a], this.locals[b], TypeClass.ZIL.name), this.toLabelStmt(label)));
         break;
      case IF_GE:
         this.x(Stmts.nIf(Exprs.nGe(this.locals[a], this.locals[b], "I"), this.toLabelStmt(label)));
         break;
      case IF_GT:
         this.x(Stmts.nIf(Exprs.nGt(this.locals[a], this.locals[b], "I"), this.toLabelStmt(label)));
         break;
      case IF_LE:
         this.x(Stmts.nIf(Exprs.nLe(this.locals[a], this.locals[b], "I"), this.toLabelStmt(label)));
         break;
      case IF_LT:
         this.x(Stmts.nIf(Exprs.nLt(this.locals[a], this.locals[b], "I"), this.toLabelStmt(label)));
         break;
      case IF_NE:
         this.x(Stmts.nIf(Exprs.nNe(this.locals[a], this.locals[b], TypeClass.ZIL.name), this.toLabelStmt(label)));
         break;
      case IF_EQZ:
         this.x(Stmts.nIf(Exprs.nEq(this.locals[a], Exprs.nInt(0), TypeClass.ZIL.name), this.toLabelStmt(label)));
         break;
      case IF_GEZ:
         this.x(Stmts.nIf(Exprs.nGe(this.locals[a], Exprs.nInt(0), "I"), this.toLabelStmt(label)));
         break;
      case IF_GTZ:
         this.x(Stmts.nIf(Exprs.nGt(this.locals[a], Exprs.nInt(0), "I"), this.toLabelStmt(label)));
         break;
      case IF_LEZ:
         this.x(Stmts.nIf(Exprs.nLe(this.locals[a], Exprs.nInt(0), "I"), this.toLabelStmt(label)));
         break;
      case IF_LTZ:
         this.x(Stmts.nIf(Exprs.nLt(this.locals[a], Exprs.nInt(0), "I"), this.toLabelStmt(label)));
         break;
      case IF_NEZ:
         this.x(Stmts.nIf(Exprs.nNe(this.locals[a], Exprs.nInt(0), TypeClass.ZIL.name), this.toLabelStmt(label)));
         break;
      default:
         throw new RuntimeException();
      }

   }

   public void visitLabel(DexLabel label) {
      this.list.add(this.toLabelStmt(label));
   }

   public void visitSparseSwitchStmt(Op op, int aA, int[] cases, DexLabel[] labels) {
      LabelStmt[] lss = new LabelStmt[cases.length];

      for(int i = 0; i < cases.length; ++i) {
         lss[i] = this.toLabelStmt(labels[i]);
      }

      LabelStmt d = new LabelStmt();
      this.x(Stmts.nLookupSwitch(this.locals[aA], cases, lss, d));
      this.x(d);
   }

   public void visitMethodStmt(Op op, int[] args, Method method) {
      Value[] vs;
      if (args.length > 0) {
         int i = 0;
         List<Local> ps = new ArrayList(args.length);
         if (op != Op.INVOKE_STATIC && op != Op.INVOKE_STATIC_RANGE) {
            ps.add(this.locals[args[i]]);
            ++i;
         }

         String[] var7 = method.getParameterTypes();
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String t = var7[var9];
            ps.add(this.locals[args[i]]);
            if (!t.equals("J") && !t.equals("D")) {
               ++i;
            } else {
               i += 2;
            }
         }

         vs = (Value[])ps.toArray(new Value[ps.size()]);
      } else {
         vs = new Value[0];
      }

      Value invoke = null;
      switch(op) {
      case INVOKE_VIRTUAL_RANGE:
      case INVOKE_VIRTUAL:
         invoke = Exprs.nInvokeVirtual(vs, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
         break;
      case INVOKE_SUPER_RANGE:
      case INVOKE_DIRECT_RANGE:
      case INVOKE_SUPER:
      case INVOKE_DIRECT:
         invoke = Exprs.nInvokeSpecial(vs, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
         break;
      case INVOKE_STATIC_RANGE:
      case INVOKE_STATIC:
         invoke = Exprs.nInvokeStatic(vs, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
         break;
      case INVOKE_INTERFACE_RANGE:
      case INVOKE_INTERFACE:
         invoke = Exprs.nInvokeInterface(vs, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
         break;
      default:
         throw new RuntimeException();
      }

      if ("V".equals(method.getReturnType())) {
         this.x(Stmts.nVoidInvoke(invoke));
      } else {
         this.x(Stmts.nAssign(this.tmpLocal, invoke));
      }

   }

   public void visitStmt1R(Op op, int reg) {
      Local va = this.locals[reg];
      switch(op) {
      case MONITOR_ENTER:
         this.x(Stmts.nLock(va));
         break;
      case MONITOR_EXIT:
         this.x(Stmts.nUnLock(va));
         break;
      case RETURN:
      case RETURN_WIDE:
      case RETURN_OBJECT:
         this.x(Stmts.nReturn(va));
         break;
      case THROW:
         this.x(Stmts.nThrow(va));
         break;
      case MOVE_RESULT:
      case MOVE_RESULT_WIDE:
      case MOVE_RESULT_OBJECT:
         if (this.lastIsInvokeOrFilledNewArray) {
            this.x(Stmts.nAssign(va, this.tmpLocal));
         } else {
            System.err.println("WARN: find wrong position of " + op + " in method " + this.method);
            this.x(Stmts.nThrow(Exprs.nInvokeNew(new Value[]{Exprs.nString("d2j: wrong position of " + op)}, new String[]{"Ljava/lang/String;"}, "Ljava/lang/RuntimeException;")));
         }
         break;
      case MOVE_EXCEPTION:
         this.x(Stmts.nIdentity(va, Exprs.nExceptionRef("Ljava/lang/Throwable;")));
         break;
      default:
         throw new RuntimeException();
      }

   }

   public void visitStmt2R(Op op, int a, int b) {
      Local va = this.locals[a];
      Local vb = this.locals[b];
      Value to = null;
      switch(op) {
      case MOVE:
      case MOVE_16:
      case MOVE_FROM16:
      case MOVE_OBJECT:
      case MOVE_OBJECT_16:
      case MOVE_OBJECT_FROM16:
      case MOVE_WIDE:
      case MOVE_WIDE_FROM16:
      case MOVE_WIDE_16:
         to = vb;
         break;
      case ARRAY_LENGTH:
         to = Exprs.nLength(vb);
         break;
      case ADD_DOUBLE_2ADDR:
         to = Exprs.nAdd(va, vb, "D");
         break;
      case ADD_FLOAT_2ADDR:
         to = Exprs.nAdd(va, vb, "F");
         break;
      case ADD_INT_2ADDR:
         to = Exprs.nAdd(va, vb, "I");
         break;
      case ADD_LONG_2ADDR:
         to = Exprs.nAdd(va, vb, "J");
         break;
      case SUB_DOUBLE_2ADDR:
         to = Exprs.nSub(va, vb, "D");
         break;
      case SUB_FLOAT_2ADDR:
         to = Exprs.nSub(va, vb, "F");
         break;
      case SUB_INT_2ADDR:
         to = Exprs.nSub(va, vb, "I");
         break;
      case SUB_LONG_2ADDR:
         to = Exprs.nSub(va, vb, "J");
         break;
      case MUL_DOUBLE_2ADDR:
         to = Exprs.nMul(va, vb, "D");
         break;
      case MUL_FLOAT_2ADDR:
         to = Exprs.nMul(va, vb, "F");
         break;
      case MUL_INT_2ADDR:
         to = Exprs.nMul(va, vb, "I");
         break;
      case MUL_LONG_2ADDR:
         to = Exprs.nMul(va, vb, "J");
         break;
      case DIV_DOUBLE_2ADDR:
         to = Exprs.nDiv(va, vb, "D");
         break;
      case DIV_FLOAT_2ADDR:
         to = Exprs.nDiv(va, vb, "F");
         break;
      case DIV_INT_2ADDR:
         to = Exprs.nDiv(va, vb, "I");
         break;
      case DIV_LONG_2ADDR:
         to = Exprs.nDiv(va, vb, "J");
         break;
      case REM_DOUBLE_2ADDR:
         to = Exprs.nRem(va, vb, "D");
         break;
      case REM_FLOAT_2ADDR:
         to = Exprs.nRem(va, vb, "F");
         break;
      case REM_INT_2ADDR:
         to = Exprs.nRem(va, vb, "I");
         break;
      case REM_LONG_2ADDR:
         to = Exprs.nRem(va, vb, "J");
         break;
      case AND_INT_2ADDR:
         to = Exprs.nAnd(va, vb, TypeClass.ZI.name);
         break;
      case AND_LONG_2ADDR:
         to = Exprs.nAnd(va, vb, "J");
         break;
      case OR_INT_2ADDR:
         to = Exprs.nOr(va, vb, TypeClass.ZI.name);
         break;
      case OR_LONG_2ADDR:
         to = Exprs.nOr(va, vb, "J");
         break;
      case XOR_INT_2ADDR:
         to = Exprs.nXor(va, vb, TypeClass.ZI.name);
         break;
      case XOR_LONG_2ADDR:
         to = Exprs.nXor(va, vb, "J");
         break;
      case SHL_INT_2ADDR:
         to = Exprs.nShl(va, vb, "I");
         break;
      case SHL_LONG_2ADDR:
         to = Exprs.nShl(va, vb, "J");
         break;
      case SHR_INT_2ADDR:
         to = Exprs.nShr(va, vb, "I");
         break;
      case SHR_LONG_2ADDR:
         to = Exprs.nShr(va, vb, "J");
         break;
      case USHR_INT_2ADDR:
         to = Exprs.nUshr(va, vb, "I");
         break;
      case USHR_LONG_2ADDR:
         to = Exprs.nUshr(va, vb, "J");
         break;
      case NOT_INT:
         to = Exprs.nNot(vb, "I");
         break;
      case NOT_LONG:
         to = Exprs.nNot(vb, "J");
         break;
      case NEG_DOUBLE:
         to = Exprs.nNeg(vb, "D");
         break;
      case NEG_FLOAT:
         to = Exprs.nNeg(vb, "F");
         break;
      case NEG_INT:
         to = Exprs.nNeg(vb, "I");
         break;
      case NEG_LONG:
         to = Exprs.nNeg(vb, "J");
         break;
      case INT_TO_BYTE:
         to = Exprs.nCast(vb, "I", "B");
         break;
      case INT_TO_CHAR:
         to = Exprs.nCast(vb, "I", "C");
         break;
      case INT_TO_DOUBLE:
         to = Exprs.nCast(vb, "I", "D");
         break;
      case INT_TO_FLOAT:
         to = Exprs.nCast(vb, "I", "F");
         break;
      case INT_TO_LONG:
         to = Exprs.nCast(vb, "I", "J");
         break;
      case INT_TO_SHORT:
         to = Exprs.nCast(vb, "I", "S");
         break;
      case FLOAT_TO_DOUBLE:
         to = Exprs.nCast(vb, "F", "D");
         break;
      case FLOAT_TO_INT:
         to = Exprs.nCast(vb, "F", "I");
         break;
      case FLOAT_TO_LONG:
         to = Exprs.nCast(vb, "F", "J");
         break;
      case DOUBLE_TO_FLOAT:
         to = Exprs.nCast(vb, "D", "F");
         break;
      case DOUBLE_TO_INT:
         to = Exprs.nCast(vb, "D", "I");
         break;
      case DOUBLE_TO_LONG:
         to = Exprs.nCast(vb, "D", "J");
         break;
      case LONG_TO_DOUBLE:
         to = Exprs.nCast(vb, "J", "D");
         break;
      case LONG_TO_FLOAT:
         to = Exprs.nCast(vb, "J", "F");
         break;
      case LONG_TO_INT:
         to = Exprs.nCast(vb, "J", "I");
         break;
      default:
         throw new RuntimeException();
      }

      this.x(Stmts.nAssign(va, (Value)to));
   }

   public void visitStmt0R(Op op) {
      switch(op) {
      case RETURN_VOID:
         this.x(Stmts.nReturnVoid());
      case NOP:
         break;
      case BAD_OP:
         this.x(Stmts.nThrow(Exprs.nInvokeNew(new Value[]{Exprs.nString("bad dex opcode")}, new String[]{"Ljava/lang/String;"}, "Ljava/lang/VerifyError;")));
         break;
      default:
         throw new RuntimeException();
      }

   }

   public void visitPackedSwitchStmt(Op op, int aA, int first_case, DexLabel[] labels) {
      LabelStmt[] lss = new LabelStmt[labels.length];

      for(int i = 0; i < labels.length; ++i) {
         lss[i] = this.toLabelStmt(labels[i]);
      }

      LabelStmt d = new LabelStmt();
      this.x(Stmts.nTableSwitch(this.locals[aA], first_case, lss, d));
      this.x(d);
   }

   public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handlers, String[] types) {
      LabelStmt[] xlabelStmts = new LabelStmt[types.length];

      for(int i = 0; i < types.length; ++i) {
         xlabelStmts[i] = this.toLabelStmt(handlers[i]);
      }

      this.irMethod.traps.add(new Trap(this.toLabelStmt(start), this.toLabelStmt(end), xlabelStmts, types));
   }

   public IrMethod convert(DexCodeNode codeNode) {
      Iterator var2;
      if (codeNode.tryStmts != null) {
         var2 = codeNode.tryStmts.iterator();

         while(var2.hasNext()) {
            TryCatchNode n = (TryCatchNode)var2.next();
            n.accept(this);
         }
      }

      if (codeNode.debugNode != null) {
         DexDebugVisitor ddv = this.visitDebug();
         if (ddv != null) {
            codeNode.debugNode.accept(ddv);
            ddv.visitEnd();
         }
      }

      this.lastIsInvokeOrFilledNewArray = false;
      if (codeNode.totalRegister >= 0) {
         this.visitRegister(codeNode.totalRegister);
      }

      var2 = codeNode.stmts.iterator();

      while(var2.hasNext()) {
         DexStmtNode n = (DexStmtNode)var2.next();
         n.accept(this);
         if (n instanceof FilledNewArrayStmtNode) {
            this.lastIsInvokeOrFilledNewArray = true;
         } else if (n instanceof MethodStmtNode) {
            this.lastIsInvokeOrFilledNewArray = !((MethodStmtNode)n).method.getReturnType().equals("V");
         } else if (!(n instanceof DexLabelStmtNode)) {
            this.lastIsInvokeOrFilledNewArray = false;
         }
      }

      this.visitEnd();
      return this.irMethod;
   }
}
