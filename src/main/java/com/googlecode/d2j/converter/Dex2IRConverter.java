package com.googlecode.d2j.converter;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.node.DexCodeNode;
import com.googlecode.d2j.node.TryCatchNode;
import com.googlecode.d2j.node.analysis.DvmFrame;
import com.googlecode.d2j.node.analysis.DvmInterpreter;
import com.googlecode.d2j.node.insn.BaseSwitchStmtNode;
import com.googlecode.d2j.node.insn.ConstStmtNode;
import com.googlecode.d2j.node.insn.DexLabelStmtNode;
import com.googlecode.d2j.node.insn.DexStmtNode;
import com.googlecode.d2j.node.insn.FieldStmtNode;
import com.googlecode.d2j.node.insn.FillArrayDataStmtNode;
import com.googlecode.d2j.node.insn.FilledNewArrayStmtNode;
import com.googlecode.d2j.node.insn.JumpStmtNode;
import com.googlecode.d2j.node.insn.MethodCustomStmtNode;
import com.googlecode.d2j.node.insn.MethodPolymorphicStmtNode;
import com.googlecode.d2j.node.insn.MethodStmtNode;
import com.googlecode.d2j.node.insn.PackedSwitchStmtNode;
import com.googlecode.d2j.node.insn.SparseSwitchStmtNode;
import com.googlecode.d2j.node.insn.Stmt2R1NNode;
import com.googlecode.d2j.node.insn.TypeStmtNode;
import com.googlecode.d2j.reader.Op;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
import com.googlecode.dex2jar.ir.TypeClass;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.stmt.StmtList;
import com.googlecode.dex2jar.ir.stmt.Stmts;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class Dex2IRConverter {
   Map<DexLabel, DexLabelStmtNode> labelMap = new HashMap();
   List<DexStmtNode> insnList;
   int[] parentCount;
   IrMethod target;
   DexCodeNode dexCodeNode;
   List<Stmt> preEmit = new ArrayList();
   List<Stmt> currentEmit;
   Map<DexLabel, LabelStmt> map = new HashMap();
   private Dex2IRConverter.Dex2IrFrame[] frames;
   private ArrayList<Stmt>[] emitStmts;
   boolean initAllToZero = true;

   static int sizeofType(String s) {
      char t = s.charAt(0);
      return t != 'J' && t != 'D' ? 1 : 2;
   }

   static int methodArgCount(String[] args) {
      int i = 0;
      String[] var2 = args;
      int var3 = args.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String s = var2[var4];
         i += sizeofType(s);
      }

      return i;
   }

   public IrMethod convert(boolean isStatic, Method method, DexCodeNode dexCodeNode) {
      this.dexCodeNode = dexCodeNode;
      IrMethod irMethod = new IrMethod();
      irMethod.args = method.getParameterTypes();
      irMethod.ret = method.getReturnType();
      irMethod.owner = method.getOwner();
      irMethod.name = method.getName();
      irMethod.isStatic = isStatic;
      this.target = irMethod;
      this.insnList = dexCodeNode.stmts;

      for(int i = 0; i < this.insnList.size(); ++i) {
         DexStmtNode stmtNode = (DexStmtNode)this.insnList.get(i);
         stmtNode.__index = i;
         if (stmtNode instanceof DexLabelStmtNode) {
            DexLabelStmtNode dexLabelStmtNode = (DexLabelStmtNode)stmtNode;
            this.labelMap.put(dexLabelStmtNode.label, dexLabelStmtNode);
         }
      }

      this.fixExceptionHandlers();
      BitSet[] exBranch = new BitSet[this.insnList.size()];
      this.parentCount = new int[this.insnList.size()];
      this.initParentCount(this.parentCount);
      BitSet handlers = new BitSet(this.insnList.size());
      this.initExceptionHandlers(dexCodeNode, exBranch, handlers);
      DvmInterpreter<Dex2IRConverter.DvmValue> interpreter = this.buildInterpreter();
      this.frames = new Dex2IRConverter.Dex2IrFrame[this.insnList.size()];
      this.emitStmts = new ArrayList[this.insnList.size()];
      BitSet access = new BitSet(this.insnList.size());
      this.dfs(exBranch, handlers, access, interpreter);
      StmtList stmts = this.target.stmts;
      stmts.addAll(this.preEmit);

      ArrayList phiLabels;
      for(int i = 0; i < this.insnList.size(); ++i) {
         DexStmtNode p = (DexStmtNode)this.insnList.get(i);
         if (access.get(i)) {
            phiLabels = this.emitStmts[i];
            if (phiLabels != null) {
               stmts.addAll(phiLabels);
            }
         } else if (p instanceof DexLabelStmtNode) {
            stmts.add(this.getLabel(((DexLabelStmtNode)p).label));
         }
      }

      this.emitStmts = null;
      Queue<Dex2IRConverter.DvmValue> queue = new LinkedList();

      int i;
      for(int i1 = 0; i1 < this.frames.length; ++i1) {
         Dex2IRConverter.Dex2IrFrame frame = this.frames[i1];
         if (this.parentCount[i1] > 1 && frame != null && access.get(i1)) {
            for(i = 0; i < frame.getTotalRegisters(); ++i) {
               Dex2IRConverter.DvmValue v = (Dex2IRConverter.DvmValue)frame.getReg(i);
               this.addToQueue(queue, v);
            }
         }
      }

      while(!queue.isEmpty()) {
         Dex2IRConverter.DvmValue v = (Dex2IRConverter.DvmValue)queue.poll();
         this.getLocal(v);
         if (v.parent != null && v.parent.local == null) {
            queue.add(v.parent);
         }

         if (v.otherParent != null) {
            Iterator var28 = v.otherParent.iterator();

            while(var28.hasNext()) {
               Dex2IRConverter.DvmValue v2 = (Dex2IRConverter.DvmValue)var28.next();
               if (v2.local == null) {
                  queue.add(v2);
               }
            }
         }
      }

      Set<Value> phiValues = new HashSet();
      phiLabels = new ArrayList();

      for(i = 0; i < this.frames.length; ++i) {
         Dex2IRConverter.Dex2IrFrame frame = this.frames[i];
         if (this.parentCount[i] > 1 && frame != null && access.get(i)) {
            DexStmtNode p = (DexStmtNode)this.insnList.get(i);
            LabelStmt labelStmt = this.getLabel(((DexLabelStmtNode)p).label);
            List<AssignStmt> phis = new ArrayList();

            for(int j = 0; j < frame.getTotalRegisters(); ++j) {
               Dex2IRConverter.DvmValue v = (Dex2IRConverter.DvmValue)frame.getReg(j);
               this.addPhi(v, phiValues, phis);
            }

            labelStmt.phis = phis;
            phiLabels.add(labelStmt);
         }
      }

      if (phiLabels.size() > 0) {
         this.target.phiLabels = phiLabels;
      }

      return this.target;
   }

   private void fixExceptionHandlers() {
      if (this.dexCodeNode.tryStmts != null) {
         Queue<Integer> q = new LinkedList();
         Set<Integer> handlers = new TreeSet();
         Iterator var3 = this.dexCodeNode.tryStmts.iterator();

         int index;
         while(var3.hasNext()) {
            TryCatchNode tcb = (TryCatchNode)var3.next();
            DexLabel[] var5 = tcb.handler;
            index = var5.length;

            for(int var7 = 0; var7 < index; ++var7) {
               DexLabel h = var5[var7];
               int index = this.indexOf(h);
               q.add(index + 1);
               handlers.add(index);
            }
         }

         q.add(0);
         Map<Integer, DexLabel> needChange = new HashMap();
         BitSet access = new BitSet(this.insnList.size());

         while(true) {
            while(true) {
               int index;
               Integer key;
               do {
                  if (q.isEmpty()) {
                     if (needChange.size() > 0) {
                        Iterator var17 = this.dexCodeNode.tryStmts.iterator();

                        while(var17.hasNext()) {
                           TryCatchNode tcb = (TryCatchNode)var17.next();
                           DexLabel[] handler = tcb.handler;

                           for(int i = 0; i < handler.length; ++i) {
                              DexLabel h = handler[i];
                              index = this.indexOf(h);
                              if (needChange.containsKey(index)) {
                                 DexLabel n = (DexLabel)needChange.get(index);
                                 if (n == null) {
                                    n = new DexLabel();
                                    needChange.put(index, n);
                                    DexLabelStmtNode dexStmtNode = new DexLabelStmtNode(n);
                                    dexStmtNode.__index = this.insnList.size();
                                    this.insnList.add(dexStmtNode);
                                    this.labelMap.put(n, dexStmtNode);
                                    JumpStmtNode jumpStmtNode = new JumpStmtNode(Op.GOTO, 0, 0, h);
                                    jumpStmtNode.__index = this.insnList.size();
                                    this.insnList.add(jumpStmtNode);
                                 }

                                 handler[i] = n;
                              }
                           }
                        }
                     }

                     return;
                  }

                  key = (Integer)q.poll();
                  index = key;
               } while(access.get(index));

               access.set(index);
               if (handlers.contains(key)) {
                  needChange.put(key, (Object)null);
               }

               DexStmtNode node = (DexStmtNode)this.insnList.get(key);
               if (node.op == null) {
                  q.add(index + 1);
               } else {
                  Op op = node.op;
                  if (op.canContinue()) {
                     q.add(index + 1);
                  }

                  if (op.canBranch()) {
                     JumpStmtNode jump = (JumpStmtNode)node;
                     q.add(this.indexOf(jump.label));
                  }

                  if (op.canSwitch()) {
                     DexLabel[] var24 = ((BaseSwitchStmtNode)node).labels;
                     index = var24.length;

                     for(int var11 = 0; var11 < index; ++var11) {
                        DexLabel dexLabel = var24[var11];
                        q.add(this.indexOf(dexLabel));
                     }
                  }
               }
            }
         }
      }
   }

   private void initExceptionHandlers(DexCodeNode dexCodeNode, BitSet[] exBranch, BitSet handlers) {
      if (dexCodeNode.tryStmts != null) {
         Iterator var4 = dexCodeNode.tryStmts.iterator();

         while(var4.hasNext()) {
            TryCatchNode tcb = (TryCatchNode)var4.next();
            this.target.traps.add(new Trap(this.getLabel(tcb.start), this.getLabel(tcb.end), this.getLabels(tcb.handler), tcb.type));
            DexLabel[] var6 = tcb.handler;
            int p = var6.length;

            for(int var8 = 0; var8 < p; ++var8) {
               DexLabel h = var6[var8];
               handlers.set(this.indexOf(h));
            }

            int endIndex = this.indexOf(tcb.end);

            for(p = this.indexOf(tcb.start) + 1; p < endIndex; ++p) {
               DexStmtNode stmt = (DexStmtNode)this.insnList.get(p);
               if (stmt.op != null && stmt.op.canThrow()) {
                  BitSet x = exBranch[p];
                  if (x == null) {
                     x = exBranch[p] = new BitSet(this.insnList.size());
                  }

                  DexLabel[] var10 = tcb.handler;
                  int var11 = var10.length;

                  for(int var12 = 0; var12 < var11; ++var12) {
                     DexLabel h = var10[var12];
                     int hIndex = this.indexOf(h);
                     x.set(hIndex);
                     int var10002 = this.parentCount[hIndex]++;
                  }
               }
            }
         }
      }

   }

   private void addPhi(Dex2IRConverter.DvmValue v, Set<Value> phiValues, List<AssignStmt> phis) {
      if (v != null && v.local != null) {
         if (v.parent != null) {
            phiValues.add(this.getLocal(v.parent));
         }

         if (v.otherParent != null) {
            Iterator var4 = v.otherParent.iterator();

            while(var4.hasNext()) {
               Dex2IRConverter.DvmValue v2 = (Dex2IRConverter.DvmValue)var4.next();
               phiValues.add(this.getLocal(v2));
            }
         }

         if (phiValues.size() > 0) {
            phis.add(Stmts.nAssign(v.local, Exprs.nPhi((Value[])phiValues.toArray(new Value[phiValues.size()]))));
            phiValues.clear();
         }
      }

   }

   Local getLocal(Dex2IRConverter.DvmValue value) {
      Local local = value.local;
      if (local == null) {
         local = value.local = this.newLocal();
      }

      return local;
   }

   private void addToQueue(Queue<Dex2IRConverter.DvmValue> queue, Dex2IRConverter.DvmValue v) {
      if (v != null && v.local != null) {
         if (v.parent != null && v.parent.local == null) {
            queue.add(v.parent);
         }

         if (v.otherParent != null) {
            Iterator var3 = v.otherParent.iterator();

            while(var3.hasNext()) {
               Dex2IRConverter.DvmValue v2 = (Dex2IRConverter.DvmValue)var3.next();
               if (v2.local == null) {
                  queue.add(v2);
               }
            }
         }
      }

   }

   private void setCurrentEmit(int index) {
      this.currentEmit = this.emitStmts[index];
      if (this.currentEmit == null) {
         this.currentEmit = this.emitStmts[index] = new ArrayList(1);
      }

   }

   private void dfs(BitSet[] exBranch, BitSet handlers, BitSet access, DvmInterpreter<Dex2IRConverter.DvmValue> interpreter) {
      this.currentEmit = this.preEmit;
      Dex2IRConverter.Dex2IrFrame first = this.initFirstFrame(this.dexCodeNode, this.target);
      if (this.parentCount[0] > 1) {
         this.merge(first, 0);
      } else {
         this.frames[0] = first;
      }

      Stack<DexStmtNode> stack = new Stack();
      stack.push(this.insnList.get(0));
      Dex2IRConverter.Dex2IrFrame tmp = new Dex2IRConverter.Dex2IrFrame(this.dexCodeNode.totalRegister);

      while(true) {
         DexStmtNode p;
         int index;
         do {
            if (stack.isEmpty()) {
               return;
            }

            p = (DexStmtNode)stack.pop();
            index = p.__index;
         } while(access.get(index));

         access.set(index);
         Dex2IRConverter.Dex2IrFrame frame = this.frames[index];
         this.setCurrentEmit(index);
         if (p instanceof DexLabelStmtNode) {
            this.emit(this.getLabel(((DexLabelStmtNode)p).label));
            if (handlers.get(index)) {
               Local ex = this.newLocal();
               this.emit(Stmts.nIdentity(ex, Exprs.nExceptionRef("Ljava/lang/Throwable;")));
               frame.setTmp(new Dex2IRConverter.DvmValue(ex));
            }
         }

         BitSet ex = exBranch[index];
         if (ex != null) {
            for(int i = ex.nextSetBit(0); i >= 0; i = ex.nextSetBit(i + 1)) {
               this.merge(frame, i);
               stack.push(this.insnList.get(i));
            }
         }

         tmp.init(frame);

         try {
            if (p.op != null) {
               switch(p.op) {
               case RETURN_VOID:
                  this.emit(Stmts.nReturnVoid());
                  break;
               case GOTO:
               case GOTO_16:
               case GOTO_32:
                  this.emit(Stmts.nGoto(this.getLabel(((JumpStmtNode)p).label)));
                  break;
               case NOP:
                  this.emit(Stmts.nNop());
                  break;
               case BAD_OP:
                  this.emit(Stmts.nThrow(Exprs.nInvokeNew(new Value[]{Exprs.nString("bad dex opcode")}, new String[]{"Ljava/lang/String;"}, "Ljava/lang/VerifyError;")));
                  break;
               default:
                  tmp.execute(p, interpreter);
               }
            }
         } catch (Exception var19) {
            throw new RuntimeException("Fail on Op " + p.op + " index " + index, var19);
         }

         if (p.op != null) {
            Op op = p.op;
            if (op.canBranch()) {
               JumpStmtNode jump = (JumpStmtNode)p;
               int targetIndex = this.indexOf(jump.label);
               stack.push(this.insnList.get(targetIndex));
               this.merge(tmp, targetIndex);
            }

            if (op.canSwitch()) {
               BaseSwitchStmtNode switchStmtNode = (BaseSwitchStmtNode)p;
               DexLabel[] var23 = switchStmtNode.labels;
               int var15 = var23.length;

               for(int var16 = 0; var16 < var15; ++var16) {
                  DexLabel label = var23[var16];
                  int targetIndex = this.indexOf(label);
                  stack.push(this.insnList.get(targetIndex));
                  this.merge(tmp, targetIndex);
               }
            }

            if (op.canContinue()) {
               stack.push(this.insnList.get(index + 1));
               this.merge(tmp, index + 1);
            }
         } else {
            stack.push(this.insnList.get(index + 1));
            this.merge(tmp, index + 1);
         }

         if (this.parentCount[index] <= 1) {
            this.frames[index] = null;
         }
      }
   }

   private void relate(Dex2IRConverter.DvmValue parent, Dex2IRConverter.DvmValue child) {
      if (child.parent == null) {
         child.parent = parent;
      } else if (child.parent != parent) {
         if (child.otherParent == null) {
            child.otherParent = new HashSet(5);
         }

         child.otherParent.add(parent);
      }

   }

   void merge(Dex2IRConverter.Dex2IrFrame src, int dst) {
      Dex2IRConverter.Dex2IrFrame distFrame = this.frames[dst];
      if (distFrame == null) {
         distFrame = this.frames[dst] = new Dex2IRConverter.Dex2IrFrame(this.dexCodeNode.totalRegister);
      }

      if (this.parentCount[dst] > 1) {
         for(int i = 0; i < src.getTotalRegisters(); ++i) {
            Dex2IRConverter.DvmValue p = (Dex2IRConverter.DvmValue)src.getReg(i);
            Dex2IRConverter.DvmValue q = (Dex2IRConverter.DvmValue)distFrame.getReg(i);
            if (p != null) {
               if (q == null) {
                  q = new Dex2IRConverter.DvmValue();
                  distFrame.setReg(i, q);
               }

               this.relate(p, q);
            }
         }
      } else {
         distFrame.init(src);
      }

   }

   private Local newLocal() {
      Local thiz = Exprs.nLocal(this.target.locals.size());
      this.target.locals.add(thiz);
      return thiz;
   }

   void emit(Stmt stmt) {
      this.currentEmit.add(stmt);
   }

   private Dex2IRConverter.Dex2IrFrame initFirstFrame(DexCodeNode methodNode, IrMethod target) {
      Dex2IRConverter.Dex2IrFrame first = new Dex2IRConverter.Dex2IrFrame(methodNode.totalRegister);
      int x = methodNode.totalRegister - methodArgCount(target.args);
      if (!target.isStatic) {
         Local thiz = this.newLocal();
         this.emit(Stmts.nIdentity(thiz, Exprs.nThisRef(target.owner)));
         first.setReg(x - 1, new Dex2IRConverter.DvmValue(thiz));
      }

      Local p;
      int i;
      for(i = 0; i < target.args.length; ++i) {
         p = this.newLocal();
         this.emit(Stmts.nIdentity(p, Exprs.nParameterRef(target.args[i], i)));
         first.setReg(x, new Dex2IRConverter.DvmValue(p));
         x += sizeofType(target.args[i]);
      }

      if (this.initAllToZero) {
         for(i = 0; i < first.getTotalRegisters(); ++i) {
            if (first.getReg(i) == null) {
               p = this.newLocal();
               this.emit(Stmts.nAssign(p, Exprs.nInt(0)));
               first.setReg(i, new Dex2IRConverter.DvmValue(p));
            }
         }
      }

      return first;
   }

   private DvmInterpreter<Dex2IRConverter.DvmValue> buildInterpreter() {
      return new DvmInterpreter<Dex2IRConverter.DvmValue>() {
         Dex2IRConverter.DvmValue b(Value value) {
            Local local = Dex2IRConverter.this.newLocal();
            Dex2IRConverter.this.emit(Stmts.nAssign(local, value));
            return new Dex2IRConverter.DvmValue(local);
         }

         public Dex2IRConverter.DvmValue newOperation(DexStmtNode insn) {
            switch(insn.op) {
            case CONST:
            case CONST_16:
            case CONST_4:
            case CONST_HIGH16:
               return this.b(Exprs.nInt((Integer)((ConstStmtNode)insn).value));
            case CONST_WIDE:
            case CONST_WIDE_16:
            case CONST_WIDE_32:
            case CONST_WIDE_HIGH16:
               return this.b(Exprs.nLong((Long)((ConstStmtNode)insn).value));
            case CONST_CLASS:
               return this.b(Exprs.nType((DexType)((ConstStmtNode)insn).value));
            case CONST_STRING:
            case CONST_STRING_JUMBO:
               return this.b(Exprs.nString((String)((ConstStmtNode)insn).value));
            case SGET:
            case SGET_BOOLEAN:
            case SGET_BYTE:
            case SGET_CHAR:
            case SGET_OBJECT:
            case SGET_SHORT:
            case SGET_WIDE:
               Field field = ((FieldStmtNode)insn).field;
               return this.b(Exprs.nStaticField(field.getOwner(), field.getName(), field.getType()));
            case NEW_INSTANCE:
               return this.b(Exprs.nNew(((TypeStmtNode)insn).type));
            default:
               return null;
            }
         }

         public Dex2IRConverter.DvmValue copyOperation(DexStmtNode insn, Dex2IRConverter.DvmValue value) {
            if (value == null) {
               this.emitNotFindOperand(insn);
               return this.b(Exprs.nInt(0));
            } else {
               return this.b(Dex2IRConverter.this.getLocal(value));
            }
         }

         public Dex2IRConverter.DvmValue unaryOperation(DexStmtNode insn, Dex2IRConverter.DvmValue value) {
            if (value == null) {
               this.emitNotFindOperand(insn);
               return this.b(Exprs.nInt(0));
            } else {
               Local local = Dex2IRConverter.this.getLocal(value);
               Field field;
               switch(insn.op) {
               case NOT_INT:
                  return this.b(Exprs.nNot(local, "I"));
               case NOT_LONG:
                  return this.b(Exprs.nNot(local, "J"));
               case NEG_DOUBLE:
                  return this.b(Exprs.nNeg(local, "D"));
               case NEG_FLOAT:
                  return this.b(Exprs.nNeg(local, "F"));
               case NEG_INT:
                  return this.b(Exprs.nNeg(local, "I"));
               case NEG_LONG:
                  return this.b(Exprs.nNeg(local, "J"));
               case INT_TO_BYTE:
                  return this.b(Exprs.nCast(local, "I", "B"));
               case INT_TO_CHAR:
                  return this.b(Exprs.nCast(local, "I", "C"));
               case INT_TO_DOUBLE:
                  return this.b(Exprs.nCast(local, "I", "D"));
               case INT_TO_FLOAT:
                  return this.b(Exprs.nCast(local, "I", "F"));
               case INT_TO_LONG:
                  return this.b(Exprs.nCast(local, "I", "J"));
               case INT_TO_SHORT:
                  return this.b(Exprs.nCast(local, "I", "S"));
               case FLOAT_TO_DOUBLE:
                  return this.b(Exprs.nCast(local, "F", "D"));
               case FLOAT_TO_INT:
                  return this.b(Exprs.nCast(local, "F", "I"));
               case FLOAT_TO_LONG:
                  return this.b(Exprs.nCast(local, "F", "J"));
               case DOUBLE_TO_FLOAT:
                  return this.b(Exprs.nCast(local, "D", "F"));
               case DOUBLE_TO_INT:
                  return this.b(Exprs.nCast(local, "D", "I"));
               case DOUBLE_TO_LONG:
                  return this.b(Exprs.nCast(local, "D", "J"));
               case LONG_TO_DOUBLE:
                  return this.b(Exprs.nCast(local, "J", "D"));
               case LONG_TO_FLOAT:
                  return this.b(Exprs.nCast(local, "J", "F"));
               case LONG_TO_INT:
                  return this.b(Exprs.nCast(local, "J", "I"));
               case ARRAY_LENGTH:
                  return this.b(Exprs.nLength(local));
               case IF_EQZ:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nEq(local, Exprs.nInt(0), TypeClass.ZIL.name), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_GEZ:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nGe(local, Exprs.nInt(0), "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_GTZ:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nGt(local, Exprs.nInt(0), "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_LEZ:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nLe(local, Exprs.nInt(0), "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_LTZ:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nLt(local, Exprs.nInt(0), "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_NEZ:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nNe(local, Exprs.nInt(0), TypeClass.ZIL.name), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case PACKED_SWITCH:
               case SPARSE_SWITCH:
                  DexLabel[] labels = ((BaseSwitchStmtNode)insn).labels;
                  LabelStmt[] lss = new LabelStmt[labels.length];

                  for(int i = 0; i < labels.length; ++i) {
                     lss[i] = Dex2IRConverter.this.getLabel(labels[i]);
                  }

                  LabelStmt d = new LabelStmt();
                  if (insn.op == Op.PACKED_SWITCH) {
                     Dex2IRConverter.this.emit(Stmts.nTableSwitch(local, ((PackedSwitchStmtNode)insn).first_case, lss, d));
                  } else {
                     Dex2IRConverter.this.emit(Stmts.nLookupSwitch(local, ((SparseSwitchStmtNode)insn).cases, lss, d));
                  }

                  Dex2IRConverter.this.emit(d);
                  return null;
               case SPUT:
               case SPUT_BOOLEAN:
               case SPUT_BYTE:
               case SPUT_CHAR:
               case SPUT_OBJECT:
               case SPUT_SHORT:
               case SPUT_WIDE:
                  field = ((FieldStmtNode)insn).field;
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nStaticField(field.getOwner(), field.getName(), field.getType()), local));
                  return null;
               case IGET:
               case IGET_BOOLEAN:
               case IGET_BYTE:
               case IGET_CHAR:
               case IGET_OBJECT:
               case IGET_SHORT:
               case IGET_WIDE:
                  field = ((FieldStmtNode)insn).field;
                  return this.b(Exprs.nField(local, field.getOwner(), field.getName(), field.getType()));
               case INSTANCE_OF:
                  return this.b(Exprs.nInstanceOf(local, ((TypeStmtNode)insn).type));
               case NEW_ARRAY:
                  return this.b(Exprs.nNewArray(((TypeStmtNode)insn).type.substring(1), local));
               case CHECK_CAST:
                  return this.b(Exprs.nCheckCast(local, ((TypeStmtNode)insn).type));
               case MONITOR_ENTER:
                  Dex2IRConverter.this.emit(Stmts.nLock(local));
                  return null;
               case MONITOR_EXIT:
                  Dex2IRConverter.this.emit(Stmts.nUnLock(local));
                  return null;
               case THROW:
                  Dex2IRConverter.this.emit(Stmts.nThrow(local));
                  return null;
               case ADD_INT_LIT16:
               case ADD_INT_LIT8:
                  return this.b(Exprs.nAdd(local, Exprs.nInt(((Stmt2R1NNode)insn).content), "I"));
               case RSUB_INT_LIT8:
               case RSUB_INT:
                  return this.b(Exprs.nSub(Exprs.nInt(((Stmt2R1NNode)insn).content), local, "I"));
               case MUL_INT_LIT8:
               case MUL_INT_LIT16:
                  return this.b(Exprs.nMul(local, Exprs.nInt(((Stmt2R1NNode)insn).content), "I"));
               case DIV_INT_LIT16:
               case DIV_INT_LIT8:
                  return this.b(Exprs.nDiv(local, Exprs.nInt(((Stmt2R1NNode)insn).content), "I"));
               case REM_INT_LIT16:
               case REM_INT_LIT8:
                  return this.b(Exprs.nRem(local, Exprs.nInt(((Stmt2R1NNode)insn).content), "I"));
               case AND_INT_LIT16:
               case AND_INT_LIT8:
                  return this.b(Exprs.nAnd(local, Exprs.nInt(((Stmt2R1NNode)insn).content), ((Stmt2R1NNode)insn).content >= 0 && ((Stmt2R1NNode)insn).content <= 1 ? TypeClass.ZI.name : "I"));
               case OR_INT_LIT16:
               case OR_INT_LIT8:
                  return this.b(Exprs.nOr(local, Exprs.nInt(((Stmt2R1NNode)insn).content), ((Stmt2R1NNode)insn).content >= 0 && ((Stmt2R1NNode)insn).content <= 1 ? TypeClass.ZI.name : "I"));
               case XOR_INT_LIT16:
               case XOR_INT_LIT8:
                  return this.b(Exprs.nXor(local, Exprs.nInt(((Stmt2R1NNode)insn).content), ((Stmt2R1NNode)insn).content >= 0 && ((Stmt2R1NNode)insn).content <= 1 ? TypeClass.ZI.name : "I"));
               case SHL_INT_LIT8:
                  return this.b(Exprs.nShl(local, Exprs.nInt(((Stmt2R1NNode)insn).content), "I"));
               case SHR_INT_LIT8:
                  return this.b(Exprs.nShr(local, Exprs.nInt(((Stmt2R1NNode)insn).content), "I"));
               case USHR_INT_LIT8:
                  return this.b(Exprs.nUshr(local, Exprs.nInt(((Stmt2R1NNode)insn).content), "I"));
               case FILL_ARRAY_DATA:
                  Dex2IRConverter.this.emit(Stmts.nFillArrayData(local, Exprs.nArrayValue(((FillArrayDataStmtNode)insn).array)));
                  return null;
               default:
                  throw new RuntimeException();
               }
            }
         }

         public Dex2IRConverter.DvmValue binaryOperation(DexStmtNode insn, Dex2IRConverter.DvmValue value1, Dex2IRConverter.DvmValue value2) {
            if (value1 != null && value2 != null) {
               Local local1 = Dex2IRConverter.this.getLocal(value1);
               Local local2 = Dex2IRConverter.this.getLocal(value2);
               switch(insn.op) {
               case AGET:
                  return this.b(Exprs.nArray(local1, local2, TypeClass.IF.name));
               case AGET_BOOLEAN:
                  return this.b(Exprs.nArray(local1, local2, "Z"));
               case AGET_BYTE:
                  return this.b(Exprs.nArray(local1, local2, "B"));
               case AGET_CHAR:
                  return this.b(Exprs.nArray(local1, local2, "C"));
               case AGET_OBJECT:
                  return this.b(Exprs.nArray(local1, local2, "L"));
               case AGET_SHORT:
                  return this.b(Exprs.nArray(local1, local2, "S"));
               case AGET_WIDE:
                  return this.b(Exprs.nArray(local1, local2, TypeClass.JD.name));
               case CMP_LONG:
                  return this.b(Exprs.nLCmp(local1, local2));
               case CMPG_DOUBLE:
                  return this.b(Exprs.nDCmpg(local1, local2));
               case CMPG_FLOAT:
                  return this.b(Exprs.nFCmpg(local1, local2));
               case CMPL_DOUBLE:
                  return this.b(Exprs.nDCmpl(local1, local2));
               case CMPL_FLOAT:
                  return this.b(Exprs.nFCmpl(local1, local2));
               case ADD_DOUBLE:
                  return this.b(Exprs.nAdd(local1, local2, "D"));
               case ADD_FLOAT:
                  return this.b(Exprs.nAdd(local1, local2, "F"));
               case ADD_INT:
                  return this.b(Exprs.nAdd(local1, local2, "I"));
               case ADD_LONG:
                  return this.b(Exprs.nAdd(local1, local2, "J"));
               case SUB_DOUBLE:
                  return this.b(Exprs.nSub(local1, local2, "D"));
               case SUB_FLOAT:
                  return this.b(Exprs.nSub(local1, local2, "F"));
               case SUB_INT:
                  return this.b(Exprs.nSub(local1, local2, "I"));
               case SUB_LONG:
                  return this.b(Exprs.nSub(local1, local2, "J"));
               case MUL_DOUBLE:
                  return this.b(Exprs.nMul(local1, local2, "D"));
               case MUL_FLOAT:
                  return this.b(Exprs.nMul(local1, local2, "F"));
               case MUL_INT:
                  return this.b(Exprs.nMul(local1, local2, "I"));
               case MUL_LONG:
                  return this.b(Exprs.nMul(local1, local2, "J"));
               case DIV_DOUBLE:
                  return this.b(Exprs.nDiv(local1, local2, "D"));
               case DIV_FLOAT:
                  return this.b(Exprs.nDiv(local1, local2, "F"));
               case DIV_INT:
                  return this.b(Exprs.nDiv(local1, local2, "I"));
               case DIV_LONG:
                  return this.b(Exprs.nDiv(local1, local2, "J"));
               case REM_DOUBLE:
                  return this.b(Exprs.nRem(local1, local2, "D"));
               case REM_FLOAT:
                  return this.b(Exprs.nRem(local1, local2, "F"));
               case REM_INT:
                  return this.b(Exprs.nRem(local1, local2, "I"));
               case REM_LONG:
                  return this.b(Exprs.nRem(local1, local2, "J"));
               case AND_INT:
                  return this.b(Exprs.nAnd(local1, local2, TypeClass.ZI.name));
               case AND_LONG:
                  return this.b(Exprs.nAnd(local1, local2, "J"));
               case OR_INT:
                  return this.b(Exprs.nOr(local1, local2, TypeClass.ZI.name));
               case OR_LONG:
                  return this.b(Exprs.nOr(local1, local2, "J"));
               case XOR_INT:
                  return this.b(Exprs.nXor(local1, local2, TypeClass.ZI.name));
               case XOR_LONG:
                  return this.b(Exprs.nXor(local1, local2, "J"));
               case SHL_INT:
                  return this.b(Exprs.nShl(local1, local2, "I"));
               case SHL_LONG:
                  return this.b(Exprs.nShl(local1, local2, "J"));
               case SHR_INT:
                  return this.b(Exprs.nShr(local1, local2, "I"));
               case SHR_LONG:
                  return this.b(Exprs.nShr(local1, local2, "J"));
               case USHR_INT:
                  return this.b(Exprs.nUshr(local1, local2, "I"));
               case USHR_LONG:
                  return this.b(Exprs.nUshr(local1, local2, "J"));
               case IF_EQ:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nEq(local1, local2, TypeClass.ZIL.name), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_GE:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nGe(local1, local2, "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_GT:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nGt(local1, local2, "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_LE:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nLe(local1, local2, "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_LT:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nLt(local1, local2, "I"), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IF_NE:
                  Dex2IRConverter.this.emit(Stmts.nIf(Exprs.nNe(local1, local2, TypeClass.ZIL.name), Dex2IRConverter.this.getLabel(((JumpStmtNode)insn).label)));
                  return null;
               case IPUT:
               case IPUT_BOOLEAN:
               case IPUT_BYTE:
               case IPUT_CHAR:
               case IPUT_OBJECT:
               case IPUT_SHORT:
               case IPUT_WIDE:
                  Field field = ((FieldStmtNode)insn).field;
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nField(local1, field.getOwner(), field.getName(), field.getType()), local2));
                  return null;
               case ADD_DOUBLE_2ADDR:
                  return this.b(Exprs.nAdd(local1, local2, "D"));
               case ADD_FLOAT_2ADDR:
                  return this.b(Exprs.nAdd(local1, local2, "F"));
               case ADD_INT_2ADDR:
                  return this.b(Exprs.nAdd(local1, local2, "I"));
               case ADD_LONG_2ADDR:
                  return this.b(Exprs.nAdd(local1, local2, "J"));
               case SUB_DOUBLE_2ADDR:
                  return this.b(Exprs.nSub(local1, local2, "D"));
               case SUB_FLOAT_2ADDR:
                  return this.b(Exprs.nSub(local1, local2, "F"));
               case SUB_INT_2ADDR:
                  return this.b(Exprs.nSub(local1, local2, "I"));
               case SUB_LONG_2ADDR:
                  return this.b(Exprs.nSub(local1, local2, "J"));
               case MUL_DOUBLE_2ADDR:
                  return this.b(Exprs.nMul(local1, local2, "D"));
               case MUL_FLOAT_2ADDR:
                  return this.b(Exprs.nMul(local1, local2, "F"));
               case MUL_INT_2ADDR:
                  return this.b(Exprs.nMul(local1, local2, "I"));
               case MUL_LONG_2ADDR:
                  return this.b(Exprs.nMul(local1, local2, "J"));
               case DIV_DOUBLE_2ADDR:
                  return this.b(Exprs.nDiv(local1, local2, "D"));
               case DIV_FLOAT_2ADDR:
                  return this.b(Exprs.nDiv(local1, local2, "F"));
               case DIV_INT_2ADDR:
                  return this.b(Exprs.nDiv(local1, local2, "I"));
               case DIV_LONG_2ADDR:
                  return this.b(Exprs.nDiv(local1, local2, "J"));
               case REM_DOUBLE_2ADDR:
                  return this.b(Exprs.nRem(local1, local2, "D"));
               case REM_FLOAT_2ADDR:
                  return this.b(Exprs.nRem(local1, local2, "F"));
               case REM_INT_2ADDR:
                  return this.b(Exprs.nRem(local1, local2, "I"));
               case REM_LONG_2ADDR:
                  return this.b(Exprs.nRem(local1, local2, "J"));
               case AND_INT_2ADDR:
                  return this.b(Exprs.nAnd(local1, local2, TypeClass.ZI.name));
               case AND_LONG_2ADDR:
                  return this.b(Exprs.nAnd(local1, local2, "J"));
               case OR_INT_2ADDR:
                  return this.b(Exprs.nOr(local1, local2, TypeClass.ZI.name));
               case OR_LONG_2ADDR:
                  return this.b(Exprs.nOr(local1, local2, "J"));
               case XOR_INT_2ADDR:
                  return this.b(Exprs.nXor(local1, local2, TypeClass.ZI.name));
               case XOR_LONG_2ADDR:
                  return this.b(Exprs.nXor(local1, local2, "J"));
               case SHL_INT_2ADDR:
                  return this.b(Exprs.nShl(local1, local2, "I"));
               case SHL_LONG_2ADDR:
                  return this.b(Exprs.nShl(local1, local2, "J"));
               case SHR_INT_2ADDR:
                  return this.b(Exprs.nShr(local1, local2, "I"));
               case SHR_LONG_2ADDR:
                  return this.b(Exprs.nShr(local1, local2, "J"));
               case USHR_INT_2ADDR:
                  return this.b(Exprs.nUshr(local1, local2, "I"));
               case USHR_LONG_2ADDR:
                  return this.b(Exprs.nUshr(local1, local2, "J"));
               default:
                  throw new RuntimeException();
               }
            } else {
               this.emitNotFindOperand(insn);
               return this.b(Exprs.nInt(0));
            }
         }

         public Dex2IRConverter.DvmValue ternaryOperation(DexStmtNode insn, Dex2IRConverter.DvmValue value1, Dex2IRConverter.DvmValue value2, Dex2IRConverter.DvmValue value3) {
            if (value1 != null && value2 != null && value3 != null) {
               Local localArray = Dex2IRConverter.this.getLocal(value1);
               Local localIndex = Dex2IRConverter.this.getLocal(value2);
               Local localValue = Dex2IRConverter.this.getLocal(value3);
               switch(insn.op) {
               case APUT:
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(localArray, localIndex, TypeClass.IF.name), localValue));
                  break;
               case APUT_BOOLEAN:
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(localArray, localIndex, "Z"), localValue));
                  break;
               case APUT_BYTE:
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(localArray, localIndex, "B"), localValue));
                  break;
               case APUT_CHAR:
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(localArray, localIndex, "C"), localValue));
                  break;
               case APUT_OBJECT:
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(localArray, localIndex, "L"), localValue));
                  break;
               case APUT_SHORT:
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(localArray, localIndex, "S"), localValue));
                  break;
               case APUT_WIDE:
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(localArray, localIndex, TypeClass.JD.name), localValue));
               }

               return null;
            } else {
               this.emitNotFindOperand(insn);
               return this.b(Exprs.nInt(0));
            }
         }

         public Dex2IRConverter.DvmValue naryOperation(DexStmtNode insn, List<? extends Dex2IRConverter.DvmValue> values) {
            Iterator var3 = values.iterator();

            while(var3.hasNext()) {
               Dex2IRConverter.DvmValue v = (Dex2IRConverter.DvmValue)var3.next();
               if (v == null) {
                  this.emitNotFindOperand(insn);
                  return this.b(Exprs.nInt(0));
               }
            }

            Value[] vs;
            int ixx;
            switch(insn.op) {
            case FILLED_NEW_ARRAY:
            case FILLED_NEW_ARRAY_RANGE:
               Dex2IRConverter.DvmValue value = new Dex2IRConverter.DvmValue();
               FilledNewArrayStmtNode filledNewArrayStmtNode = (FilledNewArrayStmtNode)insn;
               String type = filledNewArrayStmtNode.type;
               String elem = type.substring(1);
               Dex2IRConverter.this.emit(Stmts.nAssign(Dex2IRConverter.this.getLocal(value), Exprs.nNewArray(elem, Exprs.nInt(values.size()))));

               for(int i = 0; i < values.size(); ++i) {
                  Dex2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(Dex2IRConverter.this.getLocal(value), Exprs.nInt(i), elem), Dex2IRConverter.this.getLocal((Dex2IRConverter.DvmValue)values.get(i))));
               }

               return value;
            case INVOKE_CUSTOM:
            case INVOKE_CUSTOM_RANGE:
               vs = new Value[values.size()];

               for(ixx = 0; ixx < vs.length; ++ixx) {
                  vs[ixx] = Dex2IRConverter.this.getLocal((Dex2IRConverter.DvmValue)values.get(ixx));
               }

               MethodCustomStmtNode nx = (MethodCustomStmtNode)insn;
               Value invoke = Exprs.nInvokeCustom(vs, nx.name, nx.proto, nx.bsm, nx.bsmArgs);
               if ("V".equals(nx.getProto().getReturnType())) {
                  Dex2IRConverter.this.emit(Stmts.nVoidInvoke(invoke));
                  return null;
               }

               return this.b(invoke);
            case INVOKE_POLYMORPHIC:
            case INVOKE_POLYMORPHIC_RANGE:
               vs = new Value[values.size()];

               for(ixx = 0; ixx < vs.length; ++ixx) {
                  vs[ixx] = Dex2IRConverter.this.getLocal((Dex2IRConverter.DvmValue)values.get(ixx));
               }

               MethodPolymorphicStmtNode n = (MethodPolymorphicStmtNode)insn;
               Value invokex = Exprs.nInvokePolymorphic(vs, n.proto, n.method);
               if ("V".equals(n.getProto().getReturnType())) {
                  Dex2IRConverter.this.emit(Stmts.nVoidInvoke(invokex));
                  return null;
               }

               return this.b(invokex);
            default:
               Op op = insn.op;
               Value[] vsx = new Value[values.size()];

               for(int ix = 0; ix < vsx.length; ++ix) {
                  vsx[ix] = Dex2IRConverter.this.getLocal((Dex2IRConverter.DvmValue)values.get(ix));
               }

               Method method = ((MethodStmtNode)insn).method;
               Value invokexx = null;
               switch(op) {
               case INVOKE_VIRTUAL_RANGE:
               case INVOKE_VIRTUAL:
                  invokexx = Exprs.nInvokeVirtual(vsx, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
                  break;
               case INVOKE_SUPER_RANGE:
               case INVOKE_DIRECT_RANGE:
               case INVOKE_SUPER:
               case INVOKE_DIRECT:
                  invokexx = Exprs.nInvokeSpecial(vsx, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
                  break;
               case INVOKE_STATIC_RANGE:
               case INVOKE_STATIC:
                  invokexx = Exprs.nInvokeStatic(vsx, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
                  break;
               case INVOKE_INTERFACE_RANGE:
               case INVOKE_INTERFACE:
                  invokexx = Exprs.nInvokeInterface(vsx, method.getOwner(), method.getName(), method.getParameterTypes(), method.getReturnType());
                  break;
               default:
                  throw new RuntimeException();
               }

               if ("V".equals(method.getReturnType())) {
                  Dex2IRConverter.this.emit(Stmts.nVoidInvoke(invokexx));
                  return null;
               } else {
                  return this.b(invokexx);
               }
            }
         }

         void emitNotFindOperand(DexStmtNode insn) {
            String msg;
            switch(insn.op) {
            case MOVE_RESULT:
            case MOVE_RESULT_OBJECT:
            case MOVE_RESULT_WIDE:
               msg = "can't get operand(s) for " + insn.op + ", wrong position ?";
               break;
            default:
               msg = "can't get operand(s) for " + insn.op + ", out-of-range or not initialized ?";
            }

            System.err.println("WARN: " + msg);
            Dex2IRConverter.this.emit(Stmts.nThrow(Exprs.nInvokeNew(new Value[]{Exprs.nString("d2j: " + msg)}, new String[]{"Ljava/lang/String;"}, "Ljava/lang/VerifyError;")));
         }

         public void returnOperation(DexStmtNode insn, Dex2IRConverter.DvmValue value) {
            if (value == null) {
               this.emitNotFindOperand(insn);
            } else {
               Dex2IRConverter.this.emit(Stmts.nReturn(Dex2IRConverter.this.getLocal(value)));
            }
         }
      };
   }

   private LabelStmt[] getLabels(DexLabel[] handler) {
      LabelStmt[] ts = new LabelStmt[handler.length];

      for(int i = 0; i < handler.length; ++i) {
         ts[i] = this.getLabel(handler[i]);
      }

      return ts;
   }

   LabelStmt getLabel(DexLabel label) {
      LabelStmt ls = (LabelStmt)this.map.get(label);
      if (ls == null) {
         ls = Stmts.nLabel();
         this.map.put(label, ls);
      }

      return ls;
   }

   private void initParentCount(int[] parentCount) {
      parentCount[0] = 1;
      Iterator var2 = this.insnList.iterator();

      while(true) {
         while(var2.hasNext()) {
            DexStmtNode p = (DexStmtNode)var2.next();
            Op op = p.op;
            if (op == null) {
               if (p.__index < parentCount.length - 1) {
                  ++parentCount[p.__index + 1];
               }
            } else {
               if (op.canBranch()) {
                  ++parentCount[this.indexOf(((JumpStmtNode)p).label)];
               }

               if (op.canSwitch()) {
                  BaseSwitchStmtNode switchStmtNode = (BaseSwitchStmtNode)p;
                  DexLabel[] var6 = switchStmtNode.labels;
                  int var7 = var6.length;

                  for(int var8 = 0; var8 < var7; ++var8) {
                     DexLabel label = var6[var8];
                     ++parentCount[this.indexOf(label)];
                  }
               }

               if (op.canContinue()) {
                  ++parentCount[p.__index + 1];
               }
            }
         }

         return;
      }
   }

   int indexOf(DexLabel label) {
      DexLabelStmtNode dexLabelStmtNode = (DexLabelStmtNode)this.labelMap.get(label);
      return dexLabelStmtNode.__index;
   }

   static class DvmValue {
      public Dex2IRConverter.DvmValue parent;
      public Set<Dex2IRConverter.DvmValue> otherParent;
      Local local;

      public DvmValue(Local thiz) {
         this.local = thiz;
      }

      public DvmValue() {
      }
   }

   static class Dex2IrFrame extends DvmFrame<Dex2IRConverter.DvmValue> {
      public Dex2IrFrame(int totalRegister) {
         super(totalRegister);
      }
   }
}
