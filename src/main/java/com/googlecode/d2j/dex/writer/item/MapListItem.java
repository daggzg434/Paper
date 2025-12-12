package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapListItem extends BaseItem {
   public final List<SectionItem<?>> items = new ArrayList();

   public int getSize() {
      return 4 + this.items.size() * 12;
   }

   public void writeMapItem(DataOut out, int type, int size, int offset) {
      out.begin("map_item");
      out.ushort("type", type);
      out.ushort("unused", 0);
      out.uint("size", size);
      out.uint("offset", offset);
      out.end();
   }

   public void cleanZeroSizeEntry() {
      Iterator it = this.items.iterator();

      while(true) {
         SectionItem i;
         do {
            if (!it.hasNext()) {
               return;
            }

            i = (SectionItem)it.next();
         } while(i != null && i.items.size() >= 1);

         it.remove();
      }
   }

   public void write(DataOut out) {
      out.begin("map_list");
      out.uint("size", this.items.size());
      Iterator var2 = this.items.iterator();

      while(var2.hasNext()) {
         SectionItem<?> t = (SectionItem)var2.next();
         this.writeMapItem(out, t.sectionType.code, t.items.size(), t.offset);
      }

      out.end();
      this.items.clear();
   }

   public int place(int offset) {
      return offset + 4 + this.items.size() * 12;
   }
}
