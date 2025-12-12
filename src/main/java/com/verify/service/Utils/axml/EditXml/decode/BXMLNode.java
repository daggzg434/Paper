package com.verify.service.Utils.axml.EditXml.decode;

import com.verify.service.Utils.axml.EditXml.utils.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BXMLNode implements IVisitable {
   public Pair<Integer, Integer> mChunkSize = new Pair();
   public Pair<Integer, Integer> mLineNumber = new Pair();
   private List<BXMLNode> mChild;

   public BXMLNode() {
      this.mLineNumber.first = 0;
      this.mLineNumber.second = 0;
   }

   public void checkTag(int expect, int value) throws IOException {
      if (value != expect) {
         throw new IOException("Can't read current node");
      }
   }

   public void readStart(IntReader reader) throws IOException {
      this.mChunkSize.first = reader.readInt();
      this.mLineNumber.first = reader.readInt();
   }

   public void readEnd(IntReader reader) throws IOException {
      this.mChunkSize.second = reader.readInt();
      this.mLineNumber.second = reader.readInt();
   }

   public void writeStart(IntWriter writer) throws IOException {
      writer.writeInt((Integer)this.mChunkSize.first);
      writer.writeInt((Integer)this.mLineNumber.first);
   }

   public void writeEnd(IntWriter writer) throws IOException {
      writer.writeInt((Integer)this.mChunkSize.second);
      writer.writeInt((Integer)this.mLineNumber.second);
   }

   public boolean hasChild() {
      return this.mChild != null && !this.mChild.isEmpty();
   }

   public List<BXMLNode> getChildren() {
      return this.mChild;
   }

   public void addChild(BXMLNode node) {
      if (this.mChild == null) {
         this.mChild = new ArrayList();
      }

      if (node != null) {
         this.mChild.add(node);
      }

   }

   public abstract void prepare();

   public Pair<Integer, Integer> getSize() {
      return this.mChunkSize;
   }

   public Pair<Integer, Integer> getLineNumber() {
      return this.mLineNumber;
   }
}
