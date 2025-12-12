package com.googlecode.d2j.dex.writer.insn;

import java.nio.ByteBuffer;

public abstract class Insn {
   protected static final boolean DEBUG = false;
   public int offset;

   public abstract int getCodeUnitSize();

   public void write(ByteBuffer out) {
   }

   boolean isLabel() {
      return false;
   }
}
