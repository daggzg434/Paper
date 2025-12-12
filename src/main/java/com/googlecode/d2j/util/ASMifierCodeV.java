package com.googlecode.d2j.util;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.reader.Op;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import java.util.HashMap;
import java.util.Map;

public class ASMifierCodeV extends DexCodeVisitor implements DexConstants {
   Out m;
   Map<DexLabel, String> labelMap = new HashMap();
   int i = 0;

   public ASMifierCodeV(Out m) {
      this.m = m;
   }

   public void visitStmt2R1N(Op op, int distReg, int srcReg, int content) {
      this.m.s("code.visitStmt2R1N(%s,%s,%s,%s);", this.op(op), distReg, srcReg, content);
   }

   public void visitRegister(int total) {
      this.m.s("code.visitRegister(%s);", total);
   }

   public void visitStmt3R(Op op, int a, int b, int c) {
      this.m.s("code.visitStmt3R(%s,%s,%s,%s);", this.op(op), a, b, c);
   }

   public void visitStmt2R(Op op, int a, int b) {
      this.m.s("code.visitStmt2R(%s,%s,%s);", this.op(op), a, b);
   }

   public void visitStmt0R(Op op) {
      this.m.s("code.visitStmt0R(%s);", this.op(op));
   }

   public void visitStmt1R(Op op, int reg) {
      this.m.s("code.visitStmt1R(%s,%s);", this.op(op), reg);
   }

   public void visitTypeStmt(Op op, int a, int b, String type) {
      this.m.s("code.visitTypeStmt(%s,%s,%s,%s);", this.op(op), a, b, Escape.v(type));
   }

   public void visitConstStmt(Op op, int toReg, Object value) {
      if (value instanceof Integer) {
         this.m.s("code.visitConstStmt(%s,%s,%s); // int: 0x%08x  float:%f", this.op(op), toReg, Escape.v(value), value, Float.intBitsToFloat((Integer)value));
      } else if (value instanceof Long) {
         this.m.s("code.visitConstStmt(%s,%s,%s); // long: 0x%016x  double:%f", this.op(op), toReg, Escape.v(value), value, Double.longBitsToDouble((Long)value));
      } else {
         this.m.s("code.visitConstStmt(%s,%s,%s);", this.op(op), toReg, Escape.v(value));
      }

   }

   public void visitFieldStmt(Op op, int fromOrToReg, int objReg, Field field) {
      this.m.s("code.visitFieldStmt(%s,%s,%s,%s);", this.op(op), fromOrToReg, objReg, Escape.v(field));
   }

   public void visitFilledNewArrayStmt(Op op, int[] args, String type) {
      this.m.s("code.visitFilledNewArrayStmt(%s,%s,%s);", this.op(op), Escape.v(args), Escape.v(type));
   }

   public String v(DexLabel[] labels) {
      StringBuilder sb = new StringBuilder("new DexLabel[]{");
      boolean first = true;
      DexLabel[] var4 = labels;
      int var5 = labels.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         DexLabel dexLabel = var4[var6];
         if (first) {
            first = false;
         } else {
            sb.append(",");
         }

         sb.append(this.v(dexLabel));
      }

      return sb.append("}").toString();
   }

   private Object v(DexLabel l) {
      String name = (String)this.labelMap.get(l);
      if (name == null) {
         name = "L" + this.i++;
         this.m.s("DexLabel %s=new DexLabel();", name);
         this.labelMap.put(l, name);
      }

      return name;
   }

   String op(Op op) {
      return op.name();
   }

   public void visitJumpStmt(Op op, int a, int b, DexLabel label) {
      this.m.s("code.visitJumpStmt(%s,%s,%s,%s);", this.op(op), a, b, this.v(label));
   }

   public void visitMethodStmt(Op op, int[] args, String name, Proto proto, MethodHandle bsm, Object... bsmArgs) {
      this.m.s("code.visitMethodStmt(%s,%s,%s,%s,%s,%s);", this.op(op), Escape.v(args), Escape.v(name), Escape.v(proto), Escape.v(bsm), Escape.v(bsmArgs));
   }

   public void visitMethodStmt(Op op, int[] args, Method bsm, Proto proto) {
      this.m.s("code.visitMethodStmt(%s,%s,%s,%s);", this.op(op), Escape.v(args), Escape.v(bsm), Escape.v(proto));
   }

   public void visitMethodStmt(Op op, int[] args, Method method) {
      this.m.s("code.visitMethodStmt(%s,%s,%s);", this.op(op), Escape.v(args), Escape.v(method));
   }

   public void visitSparseSwitchStmt(Op op, int ra, int[] cases, DexLabel[] labels) {
      this.m.s("code.visitSparseSwitchStmt(%s,%s,%s,%s);", this.op(op), ra, Escape.v(cases), this.v(labels));
   }

   public void visitPackedSwitchStmt(Op op, int ra, int first_case, DexLabel[] labels) {
      this.m.s("code.visitSparseSwitchStmt(%s,%s,%s,%s);", this.op(op), ra, first_case, this.v(labels));
   }

   public void visitTryCatch(DexLabel start, DexLabel end, DexLabel[] handlers, String[] types) {
      this.m.s("code.visitTryCatch(%s,%s,%s,%s);", this.v(start), this.v(end), this.v(handlers), Escape.v(types));
   }

   public void visitEnd() {
      this.m.s("code.visitEnd();");
   }

   public void visitLabel(DexLabel label) {
      this.m.s("code.visitLabel(%s);", this.v(label));
   }

   public void visitFillArrayDataStmt(Op op, int ra, Object array) {
      super.visitFillArrayDataStmt(op, ra, array);
   }

   public DexDebugVisitor visitDebug() {
      this.m.s("DexDebugVisitor ddv=new DexDebugVisitor(code.visitDebug());");
      return new DexDebugVisitor() {
         public void visitParameterName(int reg, String name) {
            ASMifierCodeV.this.m.s("ddv.visitParameterName(%d,%s);", reg, Escape.v(name));
         }

         public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
            ASMifierCodeV.this.m.s("ddv.visitStartLocal(%d,%s,%s,%s,%s);", reg, ASMifierCodeV.this.v(label), Escape.v(name), Escape.v(type), Escape.v(signature));
         }

         public void visitLineNumber(int line, DexLabel label) {
            ASMifierCodeV.this.m.s("ddv.visitLineNumber(%d,%s);", line, ASMifierCodeV.this.v(label));
         }

         public void visitPrologue(DexLabel dexLabel) {
            ASMifierCodeV.this.m.s("ddv.visitPrologue(%s);", ASMifierCodeV.this.v(dexLabel));
         }

         public void visitEpiogue(DexLabel dexLabel) {
            ASMifierCodeV.this.m.s("ddv.visitEpiogue(%s);", ASMifierCodeV.this.v(dexLabel));
         }

         public void visitEndLocal(int reg, DexLabel label) {
            ASMifierCodeV.this.m.s("ddv.visitEndLocal(%d,%s);", reg, ASMifierCodeV.this.v(label));
         }

         public void visitSetFile(String file) {
            ASMifierCodeV.this.m.s("ddv.visitSetFile(%s);", Escape.v(file));
         }

         public void visitRestartLocal(int reg, DexLabel label) {
            ASMifierCodeV.this.m.s("ddv.visitRestartLocal(%d,%s);", reg, ASMifierCodeV.this.v(label));
         }
      };
   }
}
