package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.dex.writer.insn.Insn;
import com.googlecode.d2j.dex.writer.insn.JumpOp;
import com.googlecode.d2j.dex.writer.insn.Label;
import com.googlecode.d2j.dex.writer.insn.OpInsn;
import com.googlecode.d2j.dex.writer.insn.PreBuildInsn;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.CodeItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.dex.writer.item.DebugInfoItem;
import com.googlecode.d2j.dex.writer.item.StringIdItem;
import com.googlecode.d2j.reader.InstructionFormat;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeWriter extends DexCodeVisitor {
   final CodeItem codeItem;
   final ConstPool cp;
   ByteBuffer b;
   int in_reg_size;
   int max_out_reg_size;
   List<Insn> ops;
   List<Insn> tailOps;
   int total_reg;
   List<CodeItem.TryItem> tryItems;
   Method owner;
   Map<DexLabel, Label> labelMap;
   ClassDataItem.EncodedMethod encodedMethod;

   public CodeWriter(ClassDataItem.EncodedMethod encodedMethod, CodeItem codeItem, Method owner, boolean isStatic, ConstPool cp) {
      this.b = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
      this.in_reg_size = 0;
      this.max_out_reg_size = 0;
      this.ops = new ArrayList();
      this.tailOps = new ArrayList();
      this.tryItems = new ArrayList();
      this.labelMap = new HashMap();
      this.encodedMethod = encodedMethod;
      this.codeItem = codeItem;
      this.owner = owner;
      int in_reg_size = 0;
      if (!isStatic) {
         ++in_reg_size;
      }

      String[] var7 = owner.getParameterTypes();
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         String s = var7[var9];
         switch(s.charAt(0)) {
         case 'D':
         case 'J':
            in_reg_size += 2;
            break;
         default:
            ++in_reg_size;
         }
      }

      this.in_reg_size = in_reg_size;
      this.cp = cp;
   }

   public static void checkContentByte(Op op, String cc, int v) {
      if (v > 127 || v < -128) {
         throw new CantNotFixContentException(op, cc, v);
      }
   }

   public static void checkContentS4bit(Op op, String name, int v) {
      if (v > 7 || v < -8) {
         throw new CantNotFixContentException(op, name, v);
      }
   }

   public static void checkContentShort(Op op, String cccc, int v) {
      if (v > 32767 || v < -32768) {
         throw new CantNotFixContentException(op, cccc, v);
      }
   }

   public static void checkContentU4bit(Op op, String name, int v) {
      if (v > 15 || v < 0) {
         throw new CantNotFixContentException(op, name, v);
      }
   }

   public static void checkContentUByte(Op op, String cc, int v) {
      if (v > 255 || v < 0) {
         throw new CantNotFixContentException(op, cc, v);
      }
   }

   public static void checkContentUShort(Op op, String cccc, int v) {
      if (v > 65535 || v < 0) {
         throw new CantNotFixContentException(op, cccc, v);
      }
   }

   public static void checkRegA(Op op, String s, int reg) {
      if (reg > 15 || reg < 0) {
         throw new CantNotFixContentException(op, s, reg);
      }
   }

   public static void checkRegAA(Op op, String s, int reg) {
      if (reg > 255 || reg < 0) {
         throw new CantNotFixContentException(op, s, reg);
      }
   }

   static void checkRegAAAA(Op op, String s, int reg) {
      if (reg > 65535 || reg < 0) {
         throw new CantNotFixContentException(op, s, reg);
      }
   }

   static byte[] copy(ByteBuffer b) {
      int size = b.position();
      byte[] data = new byte[size];
      System.arraycopy(b.array(), 0, data, 0, size);
      return data;
   }

   public void add(Insn insn) {
      this.ops.add(insn);
   }

   private byte[] build10x(Op op) {
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)0);
      return copy(this.b);
   }

   private byte[] build11n(Op op, int vA, int B) {
      checkRegA(op, "vA", vA);
      checkContentS4bit(op, "#+B", B);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)(vA & 15 | B << 4));
      return copy(this.b);
   }

   private byte[] build11x(Op op, int vAA) {
      checkRegAA(op, "vAA", vAA);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA);
      return copy(this.b);
   }

   private byte[] build12x(Op op, int vA, int vB) {
      checkRegA(op, "vA", vA);
      checkRegA(op, "vB", vB);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)(vA & 15 | vB << 4));
      return copy(this.b);
   }

   private byte[] build21h(Op op, int vAA, Number value) {
      checkRegAA(op, "vAA", vAA);
      int realV;
      if (op == Op.CONST_HIGH16) {
         int v = value.intValue();
         if ((v & '\uffff') != 0) {
            throw new CantNotFixContentException(op, "#+BBBB0000", v);
         }

         realV = v >> 16;
      } else {
         long v = value.longValue();
         if ((v & 281474976710655L) != 0L) {
            throw new CantNotFixContentException(op, "#+BBBB000000000000", v);
         }

         realV = (int)(v >> 48);
      }

      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA).putShort((short)realV);
      return copy(this.b);
   }

   private byte[] build21s(Op op, int vAA, Number value) {
      checkRegAA(op, "vAA", vAA);
      int realV;
      if (op == Op.CONST_16) {
         realV = value.intValue();
         checkContentShort(op, "#+BBBB", realV);
      } else {
         long v = value.longValue();
         if (v > 32767L || v < -32768L) {
            throw new CantNotFixContentException(op, "#+BBBB", v);
         }

         realV = (int)v;
      }

      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA).putShort((short)realV);
      return copy(this.b);
   }

   private byte[] build22b(Op op, int vAA, int vBB, int cc) {
      checkRegAA(op, "vAA", vAA);
      checkRegAA(op, "vBB", vBB);
      checkContentByte(op, "#+CC", cc);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA).put((byte)vBB).put((byte)cc);
      return copy(this.b);
   }

   private byte[] build22s(Op op, int A, int B, int CCCC) {
      checkRegA(op, "vA", A);
      checkRegA(op, "vB", B);
      checkContentShort(op, "+CCCC", CCCC);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)(A & 15 | B << 4)).putShort((short)CCCC);
      return copy(this.b);
   }

   private byte[] build22x(Op op, int vAA, int vBBBB) {
      checkRegAA(op, "vAA", vAA);
      checkRegAAAA(op, "vBBBB", vBBBB);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA).putShort((short)vBBBB);
      return copy(this.b);
   }

   private byte[] build23x(Op op, int vAA, int vBB, int vCC) {
      checkRegAA(op, "vAA", vAA);
      checkRegAA(op, "vBB", vBB);
      checkRegAA(op, "vCC", vCC);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA).put((byte)vBB).put((byte)vCC);
      return copy(this.b);
   }

   private byte[] build31i(Op op, int vAA, Number value) {
      checkRegAA(op, "vAA", vAA);
      int realV;
      if (op == Op.CONST) {
         realV = value.intValue();
      } else {
         if (op != Op.CONST_WIDE_32) {
            throw new RuntimeException();
         }

         long v = value.longValue();
         if (v > 2147483647L || v < -2147483648L) {
            throw new CantNotFixContentException(op, "#+BBBBBBBB", v);
         }

         realV = (int)v;
      }

      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA).putInt(realV);
      return copy(this.b);
   }

   private byte[] build32x(Op op, int vAAAA, int vBBBB) {
      checkRegAAAA(op, "vAAAA", vAAAA);
      checkRegAAAA(op, "vBBBB", vBBBB);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)0).putShort((short)vAAAA).putShort((short)vBBBB);
      return copy(this.b);
   }

   private byte[] build51l(Op op, int vAA, Number value) {
      checkRegAA(op, "vAA", vAA);
      this.b.position(0);
      this.b.put((byte)op.opcode).put((byte)vAA).putLong(value.longValue());
      return copy(this.b);
   }

   Label getLabel(DexLabel label) {
      Label mapped = (Label)this.labelMap.get(label);
      if (mapped == null) {
         mapped = new Label();
         this.labelMap.put(label, mapped);
      }

      return mapped;
   }

   public void visitFillArrayDataStmt(Op op, int ra, Object value) {
      ByteBuffer b;
      int size;
      byte element_width;
      if (value instanceof byte[]) {
         byte[] data = (byte[])((byte[])value);
         size = data.length;
         element_width = 1;
         b = ByteBuffer.allocate(((size * element_width + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
         b.putShort((short)768);
         b.putShort((short)element_width);
         b.putInt(size);
         b.put(data);
      } else {
         int var9;
         int var10;
         if (value instanceof short[]) {
            short[] data = (short[])((short[])value);
            size = data.length;
            element_width = 2;
            b = ByteBuffer.allocate(((size * element_width + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short)768);
            b.putShort((short)element_width);
            b.putInt(size);
            short[] var8 = data;
            var9 = data.length;

            for(var10 = 0; var10 < var9; ++var10) {
               short s = var8[var10];
               b.putShort(s);
            }
         } else if (value instanceof int[]) {
            int[] data = (int[])((int[])value);
            size = data.length;
            element_width = 4;
            b = ByteBuffer.allocate(((size * element_width + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short)768);
            b.putShort((short)element_width);
            b.putInt(size);
            int[] var19 = data;
            var9 = data.length;

            for(var10 = 0; var10 < var9; ++var10) {
               int s = var19[var10];
               b.putInt(s);
            }
         } else if (value instanceof float[]) {
            float[] data = (float[])((float[])value);
            size = data.length;
            element_width = 4;
            b = ByteBuffer.allocate(((size * element_width + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short)768);
            b.putShort((short)element_width);
            b.putInt(size);
            float[] var20 = data;
            var9 = data.length;

            for(var10 = 0; var10 < var9; ++var10) {
               float s = var20[var10];
               b.putInt(Float.floatToIntBits(s));
            }
         } else if (value instanceof long[]) {
            long[] data = (long[])((long[])value);
            size = data.length;
            element_width = 8;
            b = ByteBuffer.allocate(((size * element_width + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short)768);
            b.putShort((short)element_width);
            b.putInt(size);
            long[] var21 = data;
            var9 = data.length;

            for(var10 = 0; var10 < var9; ++var10) {
               long s = var21[var10];
               b.putLong(s);
            }
         } else {
            if (!(value instanceof double[])) {
               throw new RuntimeException();
            }

            double[] data = (double[])((double[])value);
            size = data.length;
            element_width = 8;
            b = ByteBuffer.allocate(((size * element_width + 1) / 2 + 4) * 2).order(ByteOrder.LITTLE_ENDIAN);
            b.putShort((short)768);
            b.putShort((short)element_width);
            b.putInt(size);
            double[] var22 = data;
            var9 = data.length;

            for(var10 = 0; var10 < var9; ++var10) {
               double s = var22[var10];
               b.putLong(Double.doubleToLongBits(s));
            }
         }
      }

      Label d = new Label();
      this.ops.add(new JumpOp(op, ra, 0, d));
      this.tailOps.add(d);
      this.tailOps.add(new PreBuildInsn(b.array()));
   }

   public void visitConstStmt(Op op, int ra, Object value) {
      switch(op.format) {
      case kFmt21c:
      case kFmt31c:
         value = this.cp.wrapEncodedItem(value);
         this.ops.add(new CodeWriter.IndexedInsn(op, ra, 0, (BaseItem)value));
         break;
      case kFmt11n:
         this.ops.add(new PreBuildInsn(this.build11n(op, ra, ((Number)value).intValue())));
         break;
      case kFmt21h:
         this.ops.add(new PreBuildInsn(this.build21h(op, ra, (Number)value)));
         break;
      case kFmt21s:
         this.ops.add(new PreBuildInsn(this.build21s(op, ra, (Number)value)));
         break;
      case kFmt31i:
         this.ops.add(new PreBuildInsn(this.build31i(op, ra, (Number)value)));
         break;
      case kFmt51l:
         this.ops.add(new PreBuildInsn(this.build51l(op, ra, (Number)value)));
      }

   }

   public void visitEnd() {
      if (this.ops.size() == 0 && this.tailOps.size() == 0) {
         this.encodedMethod.code = null;
      } else {
         this.cp.addCodeItem(this.codeItem);
         this.codeItem.registersSize = this.total_reg;
         this.codeItem.outsSize = this.max_out_reg_size;
         this.codeItem.insSize = this.in_reg_size;
         this.codeItem.init(this.ops, this.tailOps, this.tryItems);
         if (this.codeItem.debugInfo != null) {
            this.cp.addDebugInfoItem(this.codeItem.debugInfo);
            List<DebugInfoItem.DNode> debugNodes = this.codeItem.debugInfo.debugNodes;
            Collections.sort(debugNodes, new Comparator<DebugInfoItem.DNode>() {
               public int compare(DebugInfoItem.DNode o1, DebugInfoItem.DNode o2) {
                  int x = o1.label.offset - o2.label.offset;
                  return x;
               }
            });
         }

         this.ops = null;
         this.tailOps = null;
         this.tryItems = null;
      }
   }

   public void visitFieldStmt(Op op, int a, int b, Field field) {
      this.ops.add(new CodeWriter.IndexedInsn(op, a, b, this.cp.uniqField(field)));
   }

   public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
      if (op.format == InstructionFormat.kFmt35c) {
         this.ops.add(new CodeWriter.OP35c(op, args, this.cp.uniqType(type)));
      } else {
         this.ops.add(new CodeWriter.OP3rc(op, args, this.cp.uniqType(type)));
      }

   }

   public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
      this.ops.add(new JumpOp(op, a, b, this.getLabel(label)));
   }

   public void visitLabel(DexLabel label) {
      this.ops.add(this.getLabel(label));
   }

   public void visitMethodStmt(Op op, int[] args, Method method) {
      if (op.format == InstructionFormat.kFmt3rc) {
         this.ops.add(new CodeWriter.OP3rc(op, args, this.cp.uniqMethod(method)));
      } else if (op.format == InstructionFormat.kFmt35c) {
         this.ops.add(new CodeWriter.OP35c(op, args, this.cp.uniqMethod(method)));
      }

      if (args.length > this.max_out_reg_size) {
         this.max_out_reg_size = args.length;
      }

   }

   public void visitPackedSwitchStmt(Op op, int aA, final int first_case, final DexLabel[] labels) {
      Label switch_data_location = new Label();
      final JumpOp jumpOp = new JumpOp(op, aA, 0, switch_data_location);
      this.ops.add(jumpOp);
      this.tailOps.add(switch_data_location);
      this.tailOps.add(new Insn() {
         public int getCodeUnitSize() {
            return labels.length * 2 + 4;
         }

         public void write(ByteBuffer out) {
            out.putShort((short)256).putShort((short)labels.length).putInt(first_case);

            for(int i = 0; i < labels.length; ++i) {
               out.putInt(CodeWriter.this.getLabel(labels[i]).offset - jumpOp.offset);
            }

         }
      });
   }

   public void visitRegister(int total) {
      this.total_reg = total;
   }

   public void visitSparseSwitchStmt(Op op, int ra, final int[] cases, final DexLabel[] labels) {
      Label switch_data_location = new Label();
      final JumpOp jumpOp = new JumpOp(op, ra, 0, switch_data_location);
      this.ops.add(jumpOp);
      this.tailOps.add(switch_data_location);
      this.tailOps.add(new Insn() {
         public int getCodeUnitSize() {
            return cases.length * 4 + 2;
         }

         public void write(ByteBuffer out) {
            out.putShort((short)512).putShort((short)cases.length);

            int i;
            for(i = 0; i < cases.length; ++i) {
               out.putInt(cases[i]);
            }

            for(i = 0; i < cases.length; ++i) {
               out.putInt(CodeWriter.this.getLabel(labels[i]).offset - jumpOp.offset);
            }

         }
      });
   }

   public void visitStmt0R(Op op) {
      if (op != Op.BAD_OP && op.format == InstructionFormat.kFmt10x) {
         this.ops.add(new PreBuildInsn(this.build10x(op)));
      }

   }

   public void visitStmt1R(Op op, int reg) {
      if (op.format == InstructionFormat.kFmt11x) {
         this.ops.add(new PreBuildInsn(this.build11x(op, reg)));
      }

   }

   public void visitStmt2R(Op op, int a, int b) {
      switch(op.format) {
      case kFmt12x:
         this.ops.add(new PreBuildInsn(this.build12x(op, a, b)));
         break;
      case kFmt22x:
         this.ops.add(new PreBuildInsn(this.build22x(op, a, b)));
         break;
      case kFmt32x:
         this.ops.add(new PreBuildInsn(this.build32x(op, a, b)));
      }

   }

   public void visitStmt2R1N(Op op, int distReg, int srcReg, int content) {
      if (op.format == InstructionFormat.kFmt22s) {
         this.ops.add(new PreBuildInsn(this.build22s(op, distReg, srcReg, content)));
      } else if (op.format == InstructionFormat.kFmt22b) {
         this.ops.add(new PreBuildInsn(this.build22b(op, distReg, srcReg, content)));
      }

   }

   public void visitStmt3R(Op op, int a, int b, int c) {
      if (op.format == InstructionFormat.kFmt23x) {
         this.ops.add(new PreBuildInsn(this.build23x(op, a, b, c)));
      }

   }

   public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handlers, String[] types) {
      CodeItem.TryItem tryItem = new CodeItem.TryItem();
      tryItem.start = this.getLabel(start);
      tryItem.end = this.getLabel(end);
      CodeItem.EncodedCatchHandler ech = new CodeItem.EncodedCatchHandler();
      tryItem.handler = ech;
      this.tryItems.add(tryItem);
      ech.addPairs = new ArrayList(types.length);

      for(int i = 0; i < types.length; ++i) {
         String type = types[i];
         Label label = this.getLabel(handlers[i]);
         if (type == null) {
            ech.catchAll = label;
         } else {
            ech.addPairs.add(new CodeItem.EncodedCatchHandler.AddrPair(this.cp.uniqType(type), label));
         }
      }

   }

   public void visitTypeStmt(Op op, int a, int b, String type) {
      this.ops.add(new CodeWriter.IndexedInsn(op, a, b, this.cp.uniqType(type)));
   }

   public DexDebugVisitor visitDebug() {
      if (this.codeItem.debugInfo == null) {
         this.codeItem.debugInfo = new DebugInfoItem();
         this.codeItem.debugInfo.parameterNames = new StringIdItem[this.owner.getParameterTypes().length];
      }

      final DebugInfoItem debugInfoItem = this.codeItem.debugInfo;
      return new DexDebugVisitor() {
         int miniLine = 0;

         public void visitParameterName(int parameterIndex, String name) {
            if (name != null) {
               if (parameterIndex < debugInfoItem.parameterNames.length) {
                  debugInfoItem.parameterNames[parameterIndex] = CodeWriter.this.cp.uniqString(name);
               }
            }
         }

         public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
            if (signature == null) {
               debugInfoItem.debugNodes.add(DebugInfoItem.DNode.startLocal(reg, CodeWriter.this.getLabel(label), CodeWriter.this.cp.uniqString(name), CodeWriter.this.cp.uniqType(type)));
            } else {
               debugInfoItem.debugNodes.add(DebugInfoItem.DNode.startLocalEx(reg, CodeWriter.this.getLabel(label), CodeWriter.this.cp.uniqString(name), CodeWriter.this.cp.uniqType(type), CodeWriter.this.cp.uniqString(signature)));
            }

         }

         public void visitLineNumber(int line, DexLabel label) {
            if ((4294967295L & (long)line) < (long)this.miniLine) {
               this.miniLine = line;
            }

            debugInfoItem.debugNodes.add(DebugInfoItem.DNode.line(line, CodeWriter.this.getLabel(label)));
         }

         public void visitPrologue(DexLabel dexLabel) {
            debugInfoItem.debugNodes.add(DebugInfoItem.DNode.prologue(CodeWriter.this.getLabel(dexLabel)));
         }

         public void visitEpiogue(DexLabel dexLabel) {
            debugInfoItem.debugNodes.add(DebugInfoItem.DNode.epiogue(CodeWriter.this.getLabel(dexLabel)));
         }

         public void visitEndLocal(int reg, DexLabel label) {
            debugInfoItem.debugNodes.add(DebugInfoItem.DNode.endLocal(reg, CodeWriter.this.getLabel(label)));
         }

         public void visitSetFile(String file) {
            debugInfoItem.fileName = CodeWriter.this.cp.uniqString(file);
         }

         public void visitRestartLocal(int reg, DexLabel label) {
            debugInfoItem.debugNodes.add(DebugInfoItem.DNode.restartLocal(reg, CodeWriter.this.getLabel(label)));
         }

         public void visitEnd() {
            debugInfoItem.firstLine = this.miniLine;
         }
      };
   }

   public static class OP3rc extends OpInsn {
      final BaseItem item;
      final int length;
      final int start;

      public OP3rc(Op op, int[] args, BaseItem item) {
         super(op);
         this.item = item;
         this.length = args.length;
         CodeWriter.checkContentUByte(op, "AA", this.length);
         if (this.length > 0) {
            this.start = args[0];
            CodeWriter.checkContentUShort(op, "CCCC", this.start);

            for(int i = 1; i < args.length; ++i) {
               if (this.start + i != args[i]) {
                  throw new CantNotFixContentException(op, "a", args[i]);
               }
            }
         } else {
            this.start = 0;
         }

      }

      public void write(ByteBuffer out) {
         CodeWriter.checkContentUShort(this.op, "@BBBB", this.item.index);
         out.put((byte)this.op.opcode).put((byte)this.length).putShort((short)this.item.index).putShort((short)this.start);
      }
   }

   public static class OP35c extends OpInsn {
      final BaseItem item;
      int A;
      int C;
      int D;
      int E;
      int F;
      int G;

      public OP35c(Op op, int[] args, BaseItem item) {
         super(op);
         int A = args.length;
         if (A > 5) {
            throw new CantNotFixContentException(op, "A", A);
         } else {
            this.A = A;
            switch(A) {
            case 5:
               this.G = args[4];
               CodeWriter.checkContentU4bit(op, "vG", this.G);
            case 4:
               this.F = args[3];
               CodeWriter.checkContentU4bit(op, "vF", this.F);
            case 3:
               this.E = args[2];
               CodeWriter.checkContentU4bit(op, "vE", this.E);
            case 2:
               this.D = args[1];
               CodeWriter.checkContentU4bit(op, "vD", this.D);
            case 1:
               this.C = args[0];
               CodeWriter.checkContentU4bit(op, "vC", this.C);
            default:
               this.item = item;
            }
         }
      }

      public void write(ByteBuffer out) {
         CodeWriter.checkContentUShort(this.op, "@BBBB", this.item.index);
         out.put((byte)this.op.opcode).put((byte)(this.A << 4 | this.G & 15)).putShort((short)this.item.index).put((byte)(this.D << 4 | this.C & 15)).put((byte)(this.F << 4 | this.E & 15));
      }
   }

   public static class IndexedInsn extends OpInsn {
      final int a;
      final int b;
      final BaseItem idxItem;

      public IndexedInsn(Op op, int a, int b, BaseItem idxItem) {
         super(op);
         switch(op.format) {
         case kFmt21c:
         case kFmt31c:
            CodeWriter.checkRegAA(op, "vAA", a);
            break;
         case kFmt22c:
            CodeWriter.checkContentU4bit(op, "A", a);
            CodeWriter.checkContentU4bit(op, "B", b);
         }

         this.a = a;
         this.b = b;
         this.idxItem = idxItem;
      }

      public void write(ByteBuffer out) {
         out.put((byte)this.op.opcode);
         switch(this.op.format) {
         case kFmt21c:
            CodeWriter.checkContentUShort(this.op, "?@BBBB", this.idxItem.index);
            out.put((byte)this.a).putShort((short)this.idxItem.index);
            break;
         case kFmt31c:
            out.put((byte)this.a).putInt(this.idxItem.index);
            break;
         case kFmt22c:
            CodeWriter.checkContentUShort(this.op, "?@CCCC", this.idxItem.index);
            out.put((byte)(this.a & 15 | this.b << 4)).putShort((short)this.idxItem.index);
         }

      }

      public void fit() {
         if (this.op == Op.CONST_STRING && (this.idxItem.index > 65535 || this.idxItem.index < 0)) {
            this.op = Op.CONST_STRING_JUMBO;
         }

      }
   }
}
