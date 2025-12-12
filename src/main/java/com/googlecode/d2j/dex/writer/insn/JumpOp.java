package com.googlecode.d2j.dex.writer.insn;

import com.googlecode.d2j.dex.writer.CodeWriter;
import com.googlecode.d2j.reader.Op;
import java.nio.ByteBuffer;

public class JumpOp extends OpInsn {
   final int a;
   final int b;
   final Label label;

   public JumpOp(Op op, int a, int b, Label label) {
      super(op);
      switch(op.format) {
      case kFmt31t:
      case kFmt21t:
         CodeWriter.checkRegAA(op, "vAA", a);
         break;
      case kFmt22t:
         CodeWriter.checkRegA(op, "vA", a);
         CodeWriter.checkRegA(op, "vB", b);
      }

      this.label = label;
      this.a = a;
      this.b = b;
   }

   public void write(ByteBuffer out) {
      out.put((byte)this.op.opcode);
      int offset = this.label.offset - this.offset;
      switch(this.op.format) {
      case kFmt31t:
         out.put((byte)this.a).putInt(offset);
         break;
      case kFmt21t:
         CodeWriter.checkContentShort(this.op, "+BBBB", offset);
         out.put((byte)this.a).putShort((short)offset);
         break;
      case kFmt22t:
         CodeWriter.checkContentShort(this.op, "+CCCC", offset);
         out.put((byte)(this.a & 15 | this.b << 4)).putShort((short)offset);
         break;
      case kFmt10t:
         CodeWriter.checkContentByte(this.op, "+AA", offset);
         out.put((byte)offset);
         break;
      case kFmt20t:
         CodeWriter.checkContentShort(this.op, "+AAAA", offset);
         out.put((byte)0).putShort((short)offset);
         break;
      case kFmt30t:
         out.put((byte)0).putInt(offset);
         break;
      default:
         throw new RuntimeException("not support");
      }

   }

   public boolean fit() {
      int offset = this.label.offset - this.offset;
      if ((this.op != Op.GOTO || offset <= 127 && offset >= -128) && (this.op != Op.GOTO_16 || offset <= 32767 && offset >= -32768)) {
         return true;
      } else {
         if (offset <= 32767 && offset >= -32768) {
            this.op = Op.GOTO_16;
         } else {
            this.op = Op.GOTO_32;
         }

         return false;
      }
   }
}
