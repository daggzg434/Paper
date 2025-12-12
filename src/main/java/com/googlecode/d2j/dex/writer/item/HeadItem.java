package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.io.DataOut;

public class HeadItem extends BaseItem {
   public static final int V035 = 3486512;
   public static final int V036 = 3552048;
   public int version = 3486512;
   public SectionItem<MapListItem> mapSection;
   public SectionItem<StringIdItem> stringIdSection;
   public SectionItem<TypeIdItem> typeIdSection;
   public SectionItem<ProtoIdItem> protoIdSection;
   public SectionItem<FieldIdItem> fieldIdSection;
   public SectionItem<MethodIdItem> methodIdSection;
   public SectionItem<ClassDefItem> classDefSection;
   public int fileSize = -1;

   public void write(DataOut out) {
      out.uint("magic", 175662436);
      out.uint("version", this.version);
      out.skip4("checksum");
      out.skip("signature", 20);
      out.uint("file_size", this.fileSize);
      out.uint("head_size", 112);
      out.uint("endian_tag", 305419896);
      out.skip("link_size,link_off", 8);
      out.uint("map_off", this.mapSection.items.size() == 0 ? 0 : this.mapSection.offset);
      out.uint("string_ids_size", this.stringIdSection.items.size());
      out.uint("string_ids_off", this.stringIdSection.items.size() == 0 ? 0 : this.stringIdSection.offset);
      out.uint("type_ids_size", this.typeIdSection.items.size());
      out.uint("type_ids_off", this.typeIdSection.items.size() == 0 ? 0 : this.typeIdSection.offset);
      out.uint("proto_ids_size", this.protoIdSection.items.size());
      out.uint("proto_ids_off", this.protoIdSection.items.size() == 0 ? 0 : this.protoIdSection.offset);
      out.uint("field_ids_size", this.fieldIdSection.items.size());
      out.uint("field_ids_off", this.fieldIdSection.items.size() == 0 ? 0 : this.fieldIdSection.offset);
      out.uint("method_ids_size", this.methodIdSection.items.size());
      out.uint("method_ids_off", this.methodIdSection.items.size() == 0 ? 0 : this.methodIdSection.offset);
      out.uint("class_defs_size", this.classDefSection.items.size());
      out.uint("class_defs_off", this.classDefSection.items.size() == 0 ? 0 : this.classDefSection.offset);
      out.uint("data_size", this.fileSize - this.mapSection.offset);
      out.uint("data_off", this.mapSection.offset);
   }

   public int place(int offset) {
      return offset + 112;
   }
}
