package com.googlecode.d2j.node.analysis;

import com.googlecode.d2j.node.insn.DexStmtNode;
import java.util.List;

public abstract class DvmInterpreter<V> {
   public abstract V newOperation(DexStmtNode insn);

   public abstract V copyOperation(DexStmtNode insn, V value);

   public abstract V unaryOperation(DexStmtNode insn, V value);

   public abstract V binaryOperation(DexStmtNode insn, V value1, V value2);

   public abstract V ternaryOperation(DexStmtNode insn, V value1, V value2, V value3);

   public abstract V naryOperation(DexStmtNode insn, List<? extends V> values);

   public abstract void returnOperation(DexStmtNode insn, V value);
}
