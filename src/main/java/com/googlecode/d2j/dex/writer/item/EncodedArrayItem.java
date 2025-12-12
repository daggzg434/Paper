package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.io.DataOut;

public class EncodedArrayItem extends BaseItem {
   public EncodedArray value = new EncodedArray();

   public int place(int offset) {
      return this.value.place(offset);
   }

   public void write(DataOut out) {
      this.value.write(out);
   }
}
