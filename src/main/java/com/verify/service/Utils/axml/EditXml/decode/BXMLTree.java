package com.verify.service.Utils.axml.EditXml.decode;

import com.verify.service.Utils.axml.EditXml.utils.Pair;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;

public class BXMLTree implements IAXMLSerialize {
   private final int NS_START = 1048832;
   private final int NS_END = 1048833;
   private final int NODE_START = 1048834;
   private final int NODE_END = 1048835;
   private final int TEXT = 1048836;
   private Stack<BXMLNode> mVisitor = new Stack();
   private BNSNode mRoot = new BNSNode();
   private int mSize;

   public void print(IVisitor visitor) {
      this.mRoot.accept(visitor);
   }

   public void write(IntWriter writer) throws IOException {
      this.write(this.mRoot, writer);
   }

   public void prepare() {
      this.mSize = 0;
      this.prepare(this.mRoot);
   }

   private void write(BXMLNode node, IntWriter writer) throws IOException {
      node.writeStart(writer);
      if (node.hasChild()) {
         Iterator var3 = node.getChildren().iterator();

         while(var3.hasNext()) {
            BXMLNode child = (BXMLNode)var3.next();
            this.write(child, writer);
         }
      }

      node.writeEnd(writer);
   }

   private void prepare(BXMLNode node) {
      node.prepare();
      Pair<Integer, Integer> p = node.getSize();
      this.mSize += (Integer)p.first + (Integer)p.second;
      if (node.hasChild()) {
         Iterator var3 = node.getChildren().iterator();

         while(var3.hasNext()) {
            BXMLNode child = (BXMLNode)var3.next();
            this.prepare(child);
         }
      }

   }

   public int getSize() {
      return this.mSize;
   }

   public BXMLNode getRoot() {
      return this.mRoot;
   }

   public void read(IntReader reader) throws IOException {
      this.mRoot.checkStartTag(1048832);
      this.mVisitor.push(this.mRoot);
      this.mRoot.readStart(reader);

      while(true) {
         int chunkType = reader.readInt();
         BTagNode node;
         switch(chunkType) {
         case 1048833:
            if (!this.mRoot.equals(this.mVisitor.pop())) {
               throw new IOException("doc has invalid end");
            } else {
               this.mRoot.checkEndTag(chunkType);
               this.mRoot.readEnd(reader);
               return;
            }
         case 1048834:
            node = new BTagNode();
            node.checkStartTag(1048834);
            BXMLNode parent = (BXMLNode)this.mVisitor.peek();
            parent.addChild(node);
            this.mVisitor.push(node);
            node.readStart(reader);
            break;
         case 1048835:
            node = (BTagNode)this.mVisitor.pop();
            node.checkEndTag(1048835);
            node.readEnd(reader);
         }
      }
   }

   public int getType() {
      return 0;
   }

   public void setSize(int size) {
   }

   public void setType(int type) {
   }
}
