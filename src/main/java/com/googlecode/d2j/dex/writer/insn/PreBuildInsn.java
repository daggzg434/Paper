package com.googlecode.d2j.dex.writer.insn;

import java.nio.ByteBuffer;

public class PreBuildInsn extends Insn {
   public final byte[] data;

   public PreBuildInsn(byte[] data) {
      this.data = data;
   }

   public int getCodeUnitSize() {
      return this.data.length / 2;
   }

   public void write(ByteBuffer out) {
      out.put(this.data);
   }
}
