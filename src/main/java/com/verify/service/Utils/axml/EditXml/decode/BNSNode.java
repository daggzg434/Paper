package com.verify.service.Utils.axml.EditXml.decode;

import java.io.IOException;

public class BNSNode extends BXMLNode {
   private final int TAG_START = 1048832;
   private final int TAG_END = 1048833;
   private int mPrefix;
   private int mUri;
   private String mPrefix_Data;
   private String mUri_Data;

   public void checkStartTag(int tag) throws IOException {
      this.checkTag(1048832, tag);
   }

   public void checkEndTag(int tag) throws IOException {
      this.checkTag(1048833, tag);
   }

   public void readStart(IntReader reader) throws IOException {
      super.readStart(reader);
      int ffffx0 = reader.readInt();
      this.mPrefix = reader.readInt();
      this.mUri = reader.readInt();
      this.mPrefix_Data = AXMLDoc.getStringBlock().getStringFor(this.mPrefix);
      this.mUri_Data = AXMLDoc.getStringBlock().getStringFor(this.mUri);
   }

   public void readEnd(IntReader reader) throws IOException {
      super.readEnd(reader);
      int ffffx0 = reader.readInt();
      int prefix = reader.readInt();
      int uri = reader.readInt();
      if (prefix != this.mPrefix || uri != this.mUri) {
         throw new IOException("Invalid end element");
      }
   }

   public void prepare() {
   }

   public void writeStart(IntWriter writer) throws IOException {
      writer.writeInt(1048832);
      super.writeStart(writer);
      writer.writeInt(-1);
      writer.writeInt(this.mPrefix_Data == null ? this.mPrefix : AXMLDoc.getStringBlock().getStringMapping(this.mPrefix_Data));
      writer.writeInt(this.mUri_Data == null ? this.mUri : AXMLDoc.getStringBlock().getStringMapping(this.mUri_Data));
   }

   public void writeEnd(IntWriter writer) throws IOException {
      writer.writeInt(1048833);
      super.writeEnd(writer);
      writer.writeInt(-1);
      writer.writeInt(this.mPrefix_Data == null ? this.mPrefix : AXMLDoc.getStringBlock().getStringMapping(this.mPrefix_Data));
      writer.writeInt(this.mUri_Data == null ? this.mUri : AXMLDoc.getStringBlock().getStringMapping(this.mUri_Data));
   }

   public int getPrefix() {
      return this.mPrefix;
   }

   public int getUri() {
      return this.mUri;
   }

   public void accept(IVisitor v) {
      v.visit(this);
   }
}
