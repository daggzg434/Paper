package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class StringIdItem extends BaseItem implements Comparable<StringIdItem> {
   public final StringDataItem stringData;

   public StringIdItem(StringDataItem stringDataItem) {
      this.stringData = stringDataItem;
   }

   public String toString() {
      return "StringIdItem [stringData=" + this.stringData + "]";
   }

   public int place(int offset) {
      return offset + 4;
   }

   public int compareTo(StringIdItem o) {
      return this.stringData.compareTo(o.stringData);
   }

   public void write(DataOut out) {
      out.uint("string_data_off", this.stringData.offset);
   }
}
