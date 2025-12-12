package com.googlecode.dex2jar.ir.expr;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;

public class Local extends Value.E0Expr {
   public int _ls_index;
   public String signature;
   public String debugName;

   public Local(String debugName) {
      super(Value.VT.LOCAL);
      this.debugName = debugName;
   }

   public Local(int index, String debugName) {
      super(Value.VT.LOCAL);
      this.debugName = debugName;
      this._ls_index = index;
   }

   public Local() {
      super(Value.VT.LOCAL);
   }

   public Local(int index) {
      super(Value.VT.LOCAL);
      this._ls_index = index;
   }

   public Value clone() {
      Local clone = new Local(this._ls_index);
      clone.debugName = this.debugName;
      clone.signature = this.signature;
      clone.valueType = this.valueType;
      return clone;
   }

   public Value clone(LabelAndLocalMapper mapper) {
      return mapper.map(this);
   }

   public String toString0() {
      return this.debugName == null ? "a" + this._ls_index : this.debugName + "_" + this._ls_index;
   }
}
