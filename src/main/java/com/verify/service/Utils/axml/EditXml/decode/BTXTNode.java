package com.verify.service.Utils.axml.EditXml.decode;

import java.io.IOException;
import java.util.List;

public class BTXTNode extends BXMLNode implements IVisitable {
   private final int TAG = 1048836;
   private int mRawName;

   public void checkTag(int value) throws IOException {
      super.checkTag(1048836, value);
   }

   public void readStart(IntReader reader) throws IOException {
      super.readStart(reader);
      this.mRawName = reader.readInt();
      int skip0 = reader.readInt();
      int skip1 = reader.readInt();
   }

   public void readEnd(IntReader reader) throws IOException {
   }

   public void prepare() {
   }

   public void writeStart(IntWriter writer) throws IOException {
      writer.writeInt(1048836);
      super.writeStart(writer);
      writer.writeInt(this.mRawName);
      writer.writeInt(0);
      writer.writeInt(0);
   }

   public void writeEnd(IntWriter writer) {
   }

   public int getName() {
      return this.mRawName;
   }

   public boolean hasChild() {
      return false;
   }

   public List<BXMLNode> getChildren() {
      throw new RuntimeException("Text node has no child");
   }

   public void addChild(BXMLNode node) {
      throw new RuntimeException("Can't add child to Text node");
   }

   public void accept(IVisitor v) {
      v.visit(this);
   }
}
