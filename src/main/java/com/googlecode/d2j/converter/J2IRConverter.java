package com.googlecode.d2j.converter;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.Trap;
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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class J2IRConverter {
   Map<Label, LabelStmt> map = new HashMap();
   InsnList insnList;
   int[] parentCount;
   J2IRConverter.JvmFrame[] frames;
   MethodNode methodNode;
   IrMethod target;
   List<Stmt>[] emitStmts;
   List<Stmt> preEmit = new ArrayList();
   List<Stmt> currentEmit;

   private J2IRConverter() {
   }

   public static IrMethod convert(String owner, MethodNode methodNode) throws AnalyzerException {
      return (new J2IRConverter()).convert0(owner, methodNode);
   }

   LabelStmt getLabel(LabelNode labelNode) {
      Label label = labelNode.getLabel();
      LabelStmt ls = (LabelStmt)this.map.get(label);
      if (ls == null) {
         ls = Stmts.nLabel();
         this.map.put(label, ls);
      }

      return ls;
   }

   void emit(Stmt stmt) {
      this.currentEmit.add(stmt);
   }

   IrMethod populate(String owner, MethodNode source) {
      IrMethod target = new IrMethod();
      target.name = source.name;
      target.owner = "L" + owner + ";";
      target.ret = Type.getReturnType(source.desc).getDescriptor();
      Type[] args = Type.getArgumentTypes(source.desc);
      String[] sArgs = new String[args.length];
      target.args = sArgs;

      for(int i = 0; i < args.length; ++i) {
         sArgs[i] = args[i].getDescriptor();
      }

      target.isStatic = 0 != (source.access & 8);
      return target;
   }

   IrMethod convert0(String owner, MethodNode methodNode) throws AnalyzerException {
      this.methodNode = methodNode;
      this.target = this.populate(owner, methodNode);
      if (methodNode.instructions.size() == 0) {
         return this.target;
      } else {
         this.insnList = methodNode.instructions;
         BitSet[] exBranch = new BitSet[this.insnList.size()];
         this.parentCount = new int[this.insnList.size()];
         this.initParentCount(this.parentCount);
         BitSet handlers = new BitSet(this.insnList.size());
         if (methodNode.tryCatchBlocks != null) {
            Iterator var5 = methodNode.tryCatchBlocks.iterator();

            while(var5.hasNext()) {
               TryCatchBlockNode tcb = (TryCatchBlockNode)var5.next();
               this.target.traps.add(new Trap(this.getLabel(tcb.start), this.getLabel(tcb.end), new LabelStmt[]{this.getLabel(tcb.handler)}, new String[]{tcb.type == null ? null : Type.getObjectType(tcb.type).getDescriptor()}));
               int handlerIdx = this.insnList.indexOf(tcb.handler);
               handlers.set(handlerIdx);

               for(AbstractInsnNode p = tcb.start.getNext(); p != tcb.end; p = p.getNext()) {
                  BitSet x = exBranch[this.insnList.indexOf(p)];
                  if (x == null) {
                     x = exBranch[this.insnList.indexOf(p)] = new BitSet(this.insnList.size());
                  }

                  x.set(handlerIdx);
                  int var10002 = this.parentCount[handlerIdx]++;
               }
            }
         }

         Interpreter<J2IRConverter.JvmValue> interpreter = this.buildInterpreter();
         this.frames = new J2IRConverter.JvmFrame[this.insnList.size()];
         this.emitStmts = new ArrayList[this.insnList.size()];
         BitSet access = new BitSet(this.insnList.size());
         this.dfs(exBranch, handlers, access, interpreter);
         StmtList stmts = this.target.stmts;
         stmts.addAll(this.preEmit);

         for(int i = 0; i < this.insnList.size(); ++i) {
            AbstractInsnNode p = this.insnList.get(i);
            if (access.get(i)) {
               List<Stmt> es = this.emitStmts[i];
               if (es != null) {
                  stmts.addAll(es);
               }
            } else if (p.getType() == 8) {
               stmts.add(this.getLabel((LabelNode)p));
            }
         }

         this.emitStmts = null;
         Queue<J2IRConverter.JvmValue> queue = new LinkedList();

         int i;
         for(int i1 = 0; i1 < this.frames.length; ++i1) {
            J2IRConverter.JvmFrame frame = this.frames[i1];
            if (this.parentCount[i1] > 1 && frame != null && access.get(i1)) {
               for(i = 0; i < frame.getLocals(); ++i) {
                  J2IRConverter.JvmValue v = (J2IRConverter.JvmValue)frame.getLocal(i);
                  this.addToQueue(queue, v);
               }

               for(i = 0; i < frame.getStackSize(); ++i) {
                  this.addToQueue(queue, (J2IRConverter.JvmValue)frame.getStack(i));
               }
            }
         }

         while(!queue.isEmpty()) {
            J2IRConverter.JvmValue v = (J2IRConverter.JvmValue)queue.poll();
            this.getLocal(v);
            if (v.parent != null && v.parent.local == null) {
               queue.add(v.parent);
            }

            if (v.otherParent != null) {
               Iterator var26 = v.otherParent.iterator();

               while(var26.hasNext()) {
                  J2IRConverter.JvmValue v2 = (J2IRConverter.JvmValue)var26.next();
                  if (v2.local == null) {
                     queue.add(v2);
                  }
               }
            }
         }

         Set<Value> phiValues = new HashSet();
         List<LabelStmt> phiLabels = new ArrayList();

         for(i = 0; i < this.frames.length; ++i) {
            J2IRConverter.JvmFrame frame = this.frames[i];
            if (this.parentCount[i] > 1 && frame != null && access.get(i)) {
               AbstractInsnNode p = this.insnList.get(i);
               LabelStmt labelStmt = this.getLabel((LabelNode)p);
               List<AssignStmt> phis = new ArrayList();

               int j;
               for(j = 0; j < frame.getLocals(); ++j) {
                  J2IRConverter.JvmValue v = (J2IRConverter.JvmValue)frame.getLocal(j);
                  this.addPhi(v, phiValues, phis);
               }

               for(j = 0; j < frame.getStackSize(); ++j) {
                  this.addPhi((J2IRConverter.JvmValue)frame.getStack(j), phiValues, phis);
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
   }

   private void addPhi(J2IRConverter.JvmValue v, Set<Value> phiValues, List<AssignStmt> phis) {
      if (v != null && v.local != null) {
         if (v.parent != null) {
            phiValues.add(this.getLocal(v.parent));
         }

         if (v.otherParent != null) {
            Iterator var4 = v.otherParent.iterator();

            while(var4.hasNext()) {
               J2IRConverter.JvmValue v2 = (J2IRConverter.JvmValue)var4.next();
               phiValues.add(this.getLocal(v2));
            }
         }

         if (phiValues.size() > 0) {
            phis.add(Stmts.nAssign(v.local, Exprs.nPhi((Value[])phiValues.toArray(new Value[phiValues.size()]))));
            phiValues.clear();
         }
      }

   }

   private void addToQueue(Queue<J2IRConverter.JvmValue> queue, J2IRConverter.JvmValue v) {
      if (v != null && v.local != null) {
         if (v.parent != null && v.parent.local == null) {
            queue.add(v.parent);
         }

         if (v.otherParent != null) {
            Iterator var3 = v.otherParent.iterator();

            while(var3.hasNext()) {
               J2IRConverter.JvmValue v2 = (J2IRConverter.JvmValue)var3.next();
               if (v2.local == null) {
                  queue.add(v2);
               }
            }
         }
      }

   }

   private void dfs(BitSet[] exBranch, BitSet handlers, BitSet access, Interpreter<J2IRConverter.JvmValue> interpreter) throws AnalyzerException {
      this.currentEmit = this.preEmit;
      J2IRConverter.JvmFrame first = this.initFirstFrame(this.methodNode, this.target);
      if (this.parentCount[0] > 1) {
         this.merge(first, 0);
      } else {
         this.frames[0] = first;
      }

      Stack<AbstractInsnNode> stack = new Stack();
      stack.push(this.insnList.getFirst());
      J2IRConverter.JvmFrame tmp = new J2IRConverter.JvmFrame(this.methodNode.maxLocals, this.methodNode.maxStack);

      while(true) {
         AbstractInsnNode p;
         int index;
         do {
            if (stack.isEmpty()) {
               return;
            }

            p = (AbstractInsnNode)stack.pop();
            index = this.insnList.indexOf(p);
         } while(access.get(index));

         access.set(index);
         J2IRConverter.JvmFrame frame = this.frames[index];
         this.setCurrentEmit(index);
         if (p.getType() == 8) {
            this.emit(this.getLabel((LabelNode)p));
            if (handlers.get(index)) {
               Local ex = this.newLocal();
               this.emit(Stmts.nIdentity(ex, Exprs.nExceptionRef("Ljava/lang/Throwable;")));
               frame.clearStack();
               frame.push(new J2IRConverter.JvmValue(1, ex));
            }
         }

         BitSet ex = exBranch[index];
         int op;
         if (ex != null) {
            for(op = ex.nextSetBit(0); op >= 0; op = ex.nextSetBit(op + 1)) {
               this.mergeEx(frame, op);
               stack.push(this.insnList.get(op));
            }
         }

         tmp.init(frame);
         tmp.execute(p, interpreter);
         op = p.getOpcode();
         if (p.getType() == 7) {
            JumpInsnNode jump = (JumpInsnNode)p;
            stack.push(jump.label);
            this.merge(tmp, this.insnList.indexOf(jump.label));
         }

         if (op == 170 || op == 171) {
            Iterator var14;
            LabelNode label;
            if (op == 170) {
               TableSwitchInsnNode tsin = (TableSwitchInsnNode)p;
               var14 = tsin.labels.iterator();

               while(var14.hasNext()) {
                  label = (LabelNode)var14.next();
                  stack.push(label);
                  this.merge(tmp, this.insnList.indexOf(label));
               }

               stack.push(tsin.dflt);
               this.merge(tmp, this.insnList.indexOf(tsin.dflt));
            } else {
               LookupSwitchInsnNode lsin = (LookupSwitchInsnNode)p;
               var14 = lsin.labels.iterator();

               while(var14.hasNext()) {
                  label = (LabelNode)var14.next();
                  stack.push(label);
                  this.merge(tmp, this.insnList.indexOf(label));
               }

               stack.push(lsin.dflt);
               this.merge(tmp, this.insnList.indexOf(lsin.dflt));
            }
         }

         if ((op < 167 || op > 177) && op != 191) {
            stack.push(p.getNext());
            this.merge(tmp, index + 1);
         }

         if (this.parentCount[index] <= 1) {
            this.frames[index] = null;
         }
      }
   }

   private void setCurrentEmit(int index) {
      this.currentEmit = this.emitStmts[index];
      if (this.currentEmit == null) {
         this.currentEmit = this.emitStmts[index] = new ArrayList(1);
      }

   }

   private Interpreter<J2IRConverter.JvmValue> buildInterpreter() {
      return new Interpreter<J2IRConverter.JvmValue>(262144) {
         public J2IRConverter.JvmValue newValue(Type type) {
            return null;
         }

         public J2IRConverter.JvmValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
            switch(insn.getOpcode()) {
            case 1:
               return this.b(1, Exprs.nNull());
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
               return this.b(1, Exprs.nInt(insn.getOpcode() - 3));
            case 9:
            case 10:
               return this.b(2, Exprs.nLong((long)(insn.getOpcode() - 9)));
            case 11:
            case 12:
            case 13:
               return this.b(1, Exprs.nFloat((float)(insn.getOpcode() - 11)));
            case 14:
            case 15:
               return this.b(2, Exprs.nDouble((double)(insn.getOpcode() - 14)));
            case 16:
            case 17:
               return this.b(1, Exprs.nInt(((IntInsnNode)insn).operand));
            case 18:
               Object cst = ((LdcInsnNode)insn).cst;
               if (cst instanceof Integer) {
                  return this.b(1, Exprs.nInt((Integer)cst));
               } else if (cst instanceof Float) {
                  return this.b(1, Exprs.nFloat((Float)cst));
               } else if (cst instanceof Long) {
                  return this.b(2, Exprs.nLong((Long)cst));
               } else if (cst instanceof Double) {
                  return this.b(2, Exprs.nDouble((Double)cst));
               } else if (cst instanceof String) {
                  return this.b(1, Exprs.nString((String)cst));
               } else if (cst instanceof Type) {
                  Type type = (Type)cst;
                  int sort = type.getSort();
                  if (sort != 10 && sort != 9) {
                     if (sort == 11) {
                        throw new UnsupportedOperationException("Not supported yet.");
                     }

                     throw new IllegalArgumentException("Illegal LDC constant " + cst);
                  }

                  return this.b(1, Exprs.nType(type.getDescriptor()));
               } else {
                  if (cst instanceof Handle) {
                     throw new UnsupportedOperationException("Not supported yet.");
                  }

                  throw new IllegalArgumentException("Illegal LDC constant " + cst);
               }
            case 168:
               throw new UnsupportedOperationException("Not supported yet.");
            case 178:
               FieldInsnNode fin = (FieldInsnNode)insn;
               return this.b(Type.getType(fin.desc).getSize(), Exprs.nStaticField("L" + fin.owner + ";", fin.name, fin.desc));
            case 187:
               return this.b(1, Exprs.nNew("L" + ((TypeInsnNode)insn).desc + ";"));
            default:
               throw new Error("Internal error.");
            }
         }

         public J2IRConverter.JvmValue copyOperation(AbstractInsnNode insn, J2IRConverter.JvmValue value) throws AnalyzerException {
            return this.b(value.getSize(), J2IRConverter.this.getLocal(value));
         }

         public J2IRConverter.JvmValue unaryOperation(AbstractInsnNode insn, J2IRConverter.JvmValue value0) throws AnalyzerException {
            Local local = value0 == null ? null : J2IRConverter.this.getLocal(value0);
            String desc;
            FieldInsnNode fin;
            LabelStmt[] targets;
            switch(insn.getOpcode()) {
            case 116:
               return this.b(1, Exprs.nNeg(local, "I"));
            case 117:
               return this.b(2, Exprs.nNeg(local, "J"));
            case 118:
               return this.b(1, Exprs.nNeg(local, "F"));
            case 119:
               return this.b(2, Exprs.nNeg(local, "D"));
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 129:
            case 130:
            case 131:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 168:
            case 169:
            case 177:
            case 178:
            case 181:
            case 182:
            case 183:
            case 184:
            case 185:
            case 186:
            case 187:
            case 196:
            case 197:
            default:
               throw new Error("Internal error.");
            case 132:
               return this.b(1, Exprs.nAdd(local, Exprs.nInt(((IincInsnNode)insn).incr), "I"));
            case 133:
               return this.b(2, Exprs.nCast(local, "I", "J"));
            case 134:
               return this.b(1, Exprs.nCast(local, "I", "F"));
            case 135:
               return this.b(2, Exprs.nCast(local, "I", "D"));
            case 136:
               return this.b(1, Exprs.nCast(local, "J", "I"));
            case 137:
               return this.b(1, Exprs.nCast(local, "J", "F"));
            case 138:
               return this.b(2, Exprs.nCast(local, "J", "D"));
            case 139:
               return this.b(1, Exprs.nCast(local, "F", "I"));
            case 140:
               return this.b(2, Exprs.nCast(local, "F", "J"));
            case 141:
               return this.b(2, Exprs.nCast(local, "F", "D"));
            case 142:
               return this.b(1, Exprs.nCast(local, "D", "I"));
            case 143:
               return this.b(2, Exprs.nCast(local, "D", "J"));
            case 144:
               return this.b(1, Exprs.nCast(local, "D", "F"));
            case 145:
               return this.b(1, Exprs.nCast(local, "I", "B"));
            case 146:
               return this.b(1, Exprs.nCast(local, "I", "C"));
            case 147:
               return this.b(1, Exprs.nCast(local, "I", "S"));
            case 153:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nEq(local, Exprs.nInt(0), "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 154:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nNe(local, Exprs.nInt(0), "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 155:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nLt(local, Exprs.nInt(0), "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 156:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nGe(local, Exprs.nInt(0), "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 157:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nGt(local, Exprs.nInt(0), "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 158:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nLe(local, Exprs.nInt(0), "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 167:
               J2IRConverter.this.emit(Stmts.nGoto(J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 170:
               TableSwitchInsnNode ts = (TableSwitchInsnNode)insn;
               targets = new LabelStmt[ts.labels.size()];

               for(int i = 0; i < ts.labels.size(); ++i) {
                  targets[i] = J2IRConverter.this.getLabel((LabelNode)ts.labels.get(i));
               }

               J2IRConverter.this.emit(Stmts.nTableSwitch(local, ts.min, targets, J2IRConverter.this.getLabel(ts.dflt)));
               return null;
            case 171:
               LookupSwitchInsnNode ls = (LookupSwitchInsnNode)insn;
               targets = new LabelStmt[ls.labels.size()];
               int[] lookupValues = new int[ls.labels.size()];

               for(int ix = 0; ix < ls.labels.size(); ++ix) {
                  targets[ix] = J2IRConverter.this.getLabel((LabelNode)ls.labels.get(ix));
                  lookupValues[ix] = (Integer)ls.keys.get(ix);
               }

               J2IRConverter.this.emit(Stmts.nLookupSwitch(local, lookupValues, targets, J2IRConverter.this.getLabel(ls.dflt)));
               return null;
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
               return null;
            case 179:
               fin = (FieldInsnNode)insn;
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nStaticField("L" + fin.owner + ";", fin.name, fin.desc), local));
               return null;
            case 180:
               fin = (FieldInsnNode)insn;
               Type fieldType = Type.getType(fin.desc);
               return this.b(fieldType.getSize(), Exprs.nField(local, "L" + fin.owner + ";", fin.name, fin.desc));
            case 188:
               switch(((IntInsnNode)insn).operand) {
               case 4:
                  return this.b(1, Exprs.nNewArray("Z", local));
               case 5:
                  return this.b(1, Exprs.nNewArray("C", local));
               case 6:
                  return this.b(1, Exprs.nNewArray("F", local));
               case 7:
                  return this.b(1, Exprs.nNewArray("D", local));
               case 8:
                  return this.b(1, Exprs.nNewArray("B", local));
               case 9:
                  return this.b(1, Exprs.nNewArray("S", local));
               case 10:
                  return this.b(1, Exprs.nNewArray("I", local));
               case 11:
                  return this.b(1, Exprs.nNewArray("D", local));
               default:
                  throw new AnalyzerException(insn, "Invalid array type");
               }
            case 189:
               desc = "L" + ((TypeInsnNode)insn).desc + ";";
               return this.b(1, Exprs.nNewArray(desc, local));
            case 190:
               return this.b(1, Exprs.nLength(local));
            case 191:
               J2IRConverter.this.emit(Stmts.nThrow(local));
               return null;
            case 192:
               String orgDesc = ((TypeInsnNode)insn).desc;
               desc = orgDesc.startsWith("[") ? orgDesc : "L" + orgDesc + ";";
               return this.b(1, Exprs.nCheckCast(local, desc));
            case 193:
               return this.b(1, Exprs.nInstanceOf(local, "L" + ((TypeInsnNode)insn).desc + ";"));
            case 194:
               J2IRConverter.this.emit(Stmts.nLock(local));
               return null;
            case 195:
               J2IRConverter.this.emit(Stmts.nUnLock(local));
               return null;
            case 198:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nEq(local, Exprs.nNull(), "L"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 199:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nNe(local, Exprs.nNull(), "L"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            }
         }

         J2IRConverter.JvmValue b(int size, Value value) {
            Local local = J2IRConverter.this.newLocal();
            J2IRConverter.this.emit(Stmts.nAssign(local, value));
            return new J2IRConverter.JvmValue(size, local);
         }

         public J2IRConverter.JvmValue binaryOperation(AbstractInsnNode insn, J2IRConverter.JvmValue value10, J2IRConverter.JvmValue value20) throws AnalyzerException {
            Local local1 = J2IRConverter.this.getLocal(value10);
            Local local2 = J2IRConverter.this.getLocal(value20);
            switch(insn.getOpcode()) {
            case 46:
               return this.b(1, Exprs.nArray(local1, local2, "I"));
            case 47:
               return this.b(1, Exprs.nArray(local1, local2, "J"));
            case 48:
               return this.b(1, Exprs.nArray(local1, local2, "F"));
            case 49:
               return this.b(1, Exprs.nArray(local1, local2, "D"));
            case 50:
               return this.b(1, Exprs.nArray(local1, local2, "L"));
            case 51:
               return this.b(1, Exprs.nArray(local1, local2, "B"));
            case 52:
               return this.b(1, Exprs.nArray(local1, local2, "C"));
            case 53:
               return this.b(1, Exprs.nArray(local1, local2, "S"));
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
            case 69:
            case 70:
            case 71:
            case 72:
            case 73:
            case 74:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 83:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 116:
            case 117:
            case 118:
            case 119:
            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 167:
            case 168:
            case 169:
            case 170:
            case 171:
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 178:
            case 179:
            case 180:
            default:
               throw new Error("Internal error.");
            case 96:
               return this.b(1, Exprs.nAdd(local1, local2, "I"));
            case 97:
               return this.b(2, Exprs.nAdd(local1, local2, "J"));
            case 98:
               return this.b(1, Exprs.nAdd(local1, local2, "F"));
            case 99:
               return this.b(2, Exprs.nAdd(local1, local2, "D"));
            case 100:
               return this.b(1, Exprs.nSub(local1, local2, "I"));
            case 101:
               return this.b(2, Exprs.nSub(local1, local2, "J"));
            case 102:
               return this.b(1, Exprs.nSub(local1, local2, "F"));
            case 103:
               return this.b(2, Exprs.nSub(local1, local2, "D"));
            case 104:
               return this.b(1, Exprs.nMul(local1, local2, "I"));
            case 105:
               return this.b(2, Exprs.nMul(local1, local2, "J"));
            case 106:
               return this.b(1, Exprs.nMul(local1, local2, "F"));
            case 107:
               return this.b(2, Exprs.nMul(local1, local2, "D"));
            case 108:
               return this.b(1, Exprs.nDiv(local1, local2, "I"));
            case 109:
               return this.b(2, Exprs.nDiv(local1, local2, "J"));
            case 110:
               return this.b(1, Exprs.nDiv(local1, local2, "F"));
            case 111:
               return this.b(2, Exprs.nDiv(local1, local2, "D"));
            case 112:
               return this.b(1, Exprs.nRem(local1, local2, "I"));
            case 113:
               return this.b(2, Exprs.nRem(local1, local2, "J"));
            case 114:
               return this.b(1, Exprs.nRem(local1, local2, "F"));
            case 115:
               return this.b(2, Exprs.nRem(local1, local2, "D"));
            case 120:
               return this.b(1, Exprs.nShl(local1, local2, "I"));
            case 121:
               return this.b(2, Exprs.nShl(local1, local2, "J"));
            case 122:
               return this.b(1, Exprs.nShr(local1, local2, "I"));
            case 123:
               return this.b(2, Exprs.nShr(local1, local2, "J"));
            case 124:
               return this.b(1, Exprs.nUshr(local1, local2, "I"));
            case 125:
               return this.b(2, Exprs.nUshr(local1, local2, "J"));
            case 126:
               return this.b(1, Exprs.nAnd(local1, local2, "I"));
            case 127:
               return this.b(2, Exprs.nAnd(local1, local2, "J"));
            case 128:
               return this.b(1, Exprs.nOr(local1, local2, "I"));
            case 129:
               return this.b(2, Exprs.nOr(local1, local2, "J"));
            case 130:
               return this.b(1, Exprs.nXor(local1, local2, "I"));
            case 131:
               return this.b(2, Exprs.nXor(local1, local2, "J"));
            case 148:
               return this.b(2, Exprs.nLCmp(local1, local2));
            case 149:
               return this.b(1, Exprs.nFCmpl(local1, local2));
            case 150:
               return this.b(1, Exprs.nFCmpg(local1, local2));
            case 151:
               return this.b(2, Exprs.nDCmpl(local1, local2));
            case 152:
               return this.b(2, Exprs.nDCmpg(local1, local2));
            case 159:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nEq(local1, local2, "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 160:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nNe(local1, local2, "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 161:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nLt(local1, local2, "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 162:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nGe(local1, local2, "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 163:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nGt(local1, local2, "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 164:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nLe(local1, local2, "I"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 165:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nEq(local1, local2, "L"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 166:
               J2IRConverter.this.emit(Stmts.nIf(Exprs.nNe(local1, local2, "L"), J2IRConverter.this.getLabel(((JumpInsnNode)insn).label)));
               return null;
            case 181:
               FieldInsnNode fin = (FieldInsnNode)insn;
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nField(local1, "L" + fin.owner + ";", fin.name, fin.desc), local2));
               return null;
            }
         }

         public J2IRConverter.JvmValue ternaryOperation(AbstractInsnNode insn, J2IRConverter.JvmValue value1, J2IRConverter.JvmValue value2, J2IRConverter.JvmValue value3) throws AnalyzerException {
            Local local1 = J2IRConverter.this.getLocal(value1);
            Local local2 = J2IRConverter.this.getLocal(value2);
            Local local3 = J2IRConverter.this.getLocal(value3);
            switch(insn.getOpcode()) {
            case 79:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "I"), local3));
               break;
            case 80:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "J"), local3));
               break;
            case 81:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "F"), local3));
               break;
            case 82:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "D"), local3));
               break;
            case 83:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "L"), local3));
               break;
            case 84:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "B"), local3));
               break;
            case 85:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "C"), local3));
               break;
            case 86:
               J2IRConverter.this.emit(Stmts.nAssign(Exprs.nArray(local1, local2, "S"), local3));
            }

            return null;
         }

         public String[] toDescArray(Type[] ts) {
            String[] ds = new String[ts.length];

            for(int i = 0; i < ts.length; ++i) {
               ds[i] = ts[i].getDescriptor();
            }

            return ds;
         }

         public J2IRConverter.JvmValue naryOperation(AbstractInsnNode insn, List<? extends J2IRConverter.JvmValue> xvalues) throws AnalyzerException {
            Value[] values = new Value[xvalues.size()];

            for(int i = 0; i < xvalues.size(); ++i) {
               values[i] = J2IRConverter.this.getLocal((J2IRConverter.JvmValue)xvalues.get(i));
            }

            if (insn.getOpcode() == 197) {
               throw new UnsupportedOperationException("Not supported yet.");
            } else {
               MethodInsnNode mi = (MethodInsnNode)insn;
               Value v = null;
               String ret = Type.getReturnType(mi.desc).getDescriptor();
               String owner = "L" + mi.owner + ";";
               String[] ps = this.toDescArray(Type.getArgumentTypes(mi.desc));
               switch(insn.getOpcode()) {
               case 182:
                  v = Exprs.nInvokeVirtual(values, owner, mi.name, ps, ret);
                  break;
               case 183:
                  v = Exprs.nInvokeSpecial(values, owner, mi.name, ps, ret);
                  break;
               case 184:
                  v = Exprs.nInvokeStatic(values, owner, mi.name, ps, ret);
                  break;
               case 185:
                  v = Exprs.nInvokeInterface(values, owner, mi.name, ps, ret);
                  break;
               case 186:
                  throw new UnsupportedOperationException("Not supported yet.");
               }

               if ("V".equals(ret)) {
                  J2IRConverter.this.emit(Stmts.nVoidInvoke(v));
                  return null;
               } else {
                  return this.b(Type.getReturnType(mi.desc).getSize(), v);
               }
            }
         }

         public J2IRConverter.JvmValue merge(J2IRConverter.JvmValue v, J2IRConverter.JvmValue w) {
            throw new UnsupportedOperationException("Not supported yet.");
         }

         public void returnOperation(AbstractInsnNode insn, J2IRConverter.JvmValue value, J2IRConverter.JvmValue expected) throws AnalyzerException {
            switch(insn.getOpcode()) {
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
               J2IRConverter.this.emit(Stmts.nReturn(J2IRConverter.this.getLocal(value)));
               break;
            case 177:
               J2IRConverter.this.emit(Stmts.nReturnVoid());
            }

         }
      };
   }

   Local getLocal(J2IRConverter.JvmValue value) {
      Local local = value.local;
      if (local == null) {
         local = value.local = this.newLocal();
      }

      return local;
   }

   private void initParentCount(int[] parentCount) {
      parentCount[0] = 1;

      for(AbstractInsnNode p = this.insnList.getFirst(); p != null; p = p.getNext()) {
         if (p.getType() == 7) {
            JumpInsnNode jump = (JumpInsnNode)p;
            ++parentCount[this.insnList.indexOf(jump.label)];
         }

         int op = p.getOpcode();
         if (op == 170 || op == 171) {
            Iterator var5;
            LabelNode label;
            if (op == 170) {
               TableSwitchInsnNode tsin = (TableSwitchInsnNode)p;

               for(var5 = tsin.labels.iterator(); var5.hasNext(); ++parentCount[this.insnList.indexOf(label)]) {
                  label = (LabelNode)var5.next();
               }

               ++parentCount[this.insnList.indexOf(tsin.dflt)];
            } else {
               LookupSwitchInsnNode lsin = (LookupSwitchInsnNode)p;

               for(var5 = lsin.labels.iterator(); var5.hasNext(); ++parentCount[this.insnList.indexOf(label)]) {
                  label = (LabelNode)var5.next();
               }

               ++parentCount[this.insnList.indexOf(lsin.dflt)];
            }
         }

         if ((op < 167 || op > 177) && op != 191) {
            AbstractInsnNode next = p.getNext();
            if (next != null) {
               ++parentCount[this.insnList.indexOf(p.getNext())];
            }
         }
      }

   }

   private void mergeEx(J2IRConverter.JvmFrame src, int dst) {
      J2IRConverter.JvmFrame distFrame = this.frames[dst];
      if (distFrame == null) {
         distFrame = this.frames[dst] = new J2IRConverter.JvmFrame(this.methodNode.maxLocals, this.methodNode.maxStack);
      }

      for(int i = 0; i < src.getLocals(); ++i) {
         J2IRConverter.JvmValue p = (J2IRConverter.JvmValue)src.getLocal(i);
         J2IRConverter.JvmValue q = (J2IRConverter.JvmValue)distFrame.getLocal(i);
         if (p != null) {
            if (q == null) {
               q = new J2IRConverter.JvmValue(p.getSize());
               distFrame.setLocal(i, q);
            }

            this.relate(p, q);
         }
      }

   }

   private void merge(J2IRConverter.JvmFrame src, int dst) {
      J2IRConverter.JvmFrame distFrame = this.frames[dst];
      if (distFrame == null) {
         distFrame = this.frames[dst] = new J2IRConverter.JvmFrame(this.methodNode.maxLocals, this.methodNode.maxStack);
      }

      if (this.parentCount[dst] > 1) {
         int i;
         J2IRConverter.JvmValue p;
         J2IRConverter.JvmValue q;
         for(i = 0; i < src.getLocals(); ++i) {
            p = (J2IRConverter.JvmValue)src.getLocal(i);
            q = (J2IRConverter.JvmValue)distFrame.getLocal(i);
            if (p != null) {
               if (q == null) {
                  q = new J2IRConverter.JvmValue(p.getSize());
                  distFrame.setLocal(i, q);
               }

               this.relate(p, q);
            }
         }

         if (src.getStackSize() > 0) {
            if (distFrame.getStackSize() == 0) {
               for(i = 0; i < src.getStackSize(); ++i) {
                  distFrame.push(new J2IRConverter.JvmValue(((J2IRConverter.JvmValue)src.getStack(i)).getSize()));
               }
            } else if (distFrame.getStackSize() != src.getStackSize()) {
               throw new RuntimeException("stack not balanced");
            }

            for(i = 0; i < src.getStackSize(); ++i) {
               p = (J2IRConverter.JvmValue)src.getStack(i);
               q = (J2IRConverter.JvmValue)distFrame.getStack(i);
               this.relate(p, q);
            }
         }
      } else {
         distFrame.init(src);
      }

   }

   private void relate(J2IRConverter.JvmValue parent, J2IRConverter.JvmValue child) {
      if (child.parent == null) {
         child.parent = parent;
      } else if (child.parent != parent) {
         if (child.otherParent == null) {
            child.otherParent = new HashSet(5);
         }

         child.otherParent.add(parent);
      }

   }

   private J2IRConverter.JvmFrame initFirstFrame(MethodNode methodNode, IrMethod target) {
      J2IRConverter.JvmFrame first = new J2IRConverter.JvmFrame(methodNode.maxLocals, methodNode.maxStack);
      int x = 0;
      if (!target.isStatic) {
         Local thiz = this.newLocal();
         this.emit(Stmts.nIdentity(thiz, Exprs.nThisRef(target.owner)));
         first.setLocal(x++, new J2IRConverter.JvmValue(1, thiz));
      }

      for(int i = 0; i < target.args.length; ++i) {
         Local p = this.newLocal();
         this.emit(Stmts.nIdentity(p, Exprs.nParameterRef(target.args[i], i)));
         int sizeOfType = this.sizeOfType(target.args[i]);
         first.setLocal(x, new J2IRConverter.JvmValue(sizeOfType, p));
         x += sizeOfType;
      }

      return first;
   }

   private int sizeOfType(String arg) {
      switch(arg.charAt(0)) {
      case 'D':
      case 'J':
         return 2;
      default:
         return 1;
      }
   }

   private Local newLocal() {
      Local thiz = Exprs.nLocal(this.target.locals.size());
      this.target.locals.add(thiz);
      return thiz;
   }

   public static class JvmValue implements org.objectweb.asm.tree.analysis.Value {
      private final int size;
      public J2IRConverter.JvmValue parent;
      public Set<J2IRConverter.JvmValue> otherParent;
      Local local;

      public JvmValue(int size, Local local) {
         this.size = size;
         this.local = local;
      }

      public JvmValue(int size) {
         this.size = size;
      }

      public int getSize() {
         return this.size;
      }
   }

   static class JvmFrame extends Frame<J2IRConverter.JvmValue> {
      public JvmFrame(int nLocals, int nStack) {
         super(nLocals, nStack);
      }

      public void execute(AbstractInsnNode insn, Interpreter<J2IRConverter.JvmValue> interpreter) throws AnalyzerException {
         if (insn.getType() != 14 && insn.getType() != 15 && insn.getType() != 8) {
            if (insn.getOpcode() == 177) {
               interpreter.returnOperation(insn, (org.objectweb.asm.tree.analysis.Value)null, (org.objectweb.asm.tree.analysis.Value)null);
            } else if (insn.getOpcode() == 167) {
               interpreter.unaryOperation(insn, (org.objectweb.asm.tree.analysis.Value)null);
            } else {
               if (insn.getOpcode() == 169) {
                  throw new RuntimeException("not support yet!");
               }

               super.execute(insn, interpreter);
            }

         }
      }
   }
}
