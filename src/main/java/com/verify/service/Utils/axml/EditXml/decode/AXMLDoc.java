package com.verify.service.Utils.axml.EditXml.decode;

import com.verify.service.Utils.axml.Manifest_ids;
import com.verify.service.Utils.axml.EditXml.utils.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AXMLDoc {
   private final int MAGIC_NUMBER = 524291;
   private final int CHUNK_STRING_BLOCK = 1835009;
   private final int CHUNK_RESOURCE_ID = 524672;
   private final int CHUNK_XML_TREE = 1048832;
   private final String MANIFEST = "manifest";
   private final String APPLICATION = "application";
   private int mDocSize;
   private static StringBlock mStringBlock;
   private static ResBlock mResBlock;
   private BXMLTree mXMLTree;
   private InputStream is;

   public static StringBlock getStringBlock() {
      return mStringBlock;
   }

   public static ResBlock getResBlock() {
      return mResBlock;
   }

   public BXMLTree getBXMLTree() {
      return this.mXMLTree;
   }

   public BXMLNode getManifestNode() {
      List<BXMLNode> children = this.mXMLTree.getRoot().getChildren();
      Iterator var2 = children.iterator();

      BXMLNode node;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         node = (BXMLNode)var2.next();
      } while(!"manifest".equals(((BTagNode)node).getmRawName_Data()));

      return node;
   }

   public BXMLNode getApplicationNode() {
      BXMLNode manifest = this.getManifestNode();
      if (manifest == null) {
         return null;
      } else {
         Iterator var2 = manifest.getChildren().iterator();

         BXMLNode node;
         do {
            if (!var2.hasNext()) {
               return null;
            }

            node = (BXMLNode)var2.next();
         } while(!"application".equals(((BTagNode)node).getmRawName_Data()));

         return node;
      }
   }

   public void build(OutputStream os) throws IOException {
      IntWriter writer = null;

      try {
         writer = new IntWriter(os, false);
         mStringBlock.prepare();
         List<Integer> attr_id = new ArrayList();
         Iterator var4 = mStringBlock.getmStrings().iterator();

         while(true) {
            if (var4.hasNext()) {
               String string = (String)var4.next();
               int id = Manifest_ids.getInstance().parseids(string);
               if (!string.equals("android")) {
                  attr_id.add(id);
                  continue;
               }
            }

            int[] id_attr = new int[attr_id.size()];

            for(int i = 0; i < attr_id.size(); ++i) {
               id_attr[i] = (Integer)attr_id.get(i);
            }

            mResBlock.setmRawResIds(id_attr);
            mResBlock.prepare();
            this.mXMLTree.prepare();
            int base = 8;
            this.mDocSize = base + mStringBlock.getSize() + mResBlock.getSize() + this.mXMLTree.getSize();
            writer.writeInt(524291);
            writer.writeInt(this.mDocSize);
            mStringBlock.write(writer);
            mResBlock.write(writer);
            this.mXMLTree.write(writer);
            os.flush();
            return;
         }
      } catch (IOException var10) {
         var10.printStackTrace();
         throw new IOException(var10);
      } finally {
         IOUtils.closeQuietly(writer, os);
      }
   }

   public void testSize() throws IOException {
      System.out.println("string block size:" + mStringBlock.getSize());
      mStringBlock.prepare();
      System.out.println("string block size:" + mStringBlock.getSize());
      System.out.println("res block size:" + mResBlock.getSize());
      mResBlock.prepare();
      System.out.println("res size:" + mResBlock.getSize());
      System.out.println("xml size:" + this.mXMLTree.getSize());
      this.mXMLTree.prepare();
      System.out.println("xml size:" + this.mXMLTree.getSize());
      System.out.println("doc size:" + this.mDocSize);
      int base = 8;
      int size = base + mStringBlock.getSize() + mResBlock.getSize() + this.mXMLTree.getSize();
      System.out.println("doc size:" + size);
   }

   public void print() {
      System.out.println("size:" + this.mDocSize);
      this.mXMLTree.print(new XMLVisitor(mStringBlock));
   }

   public void parse(InputStream is) throws Exception {
      this.is = is;
      IntReader reader = new IntReader(is, false);
      int magicNum = reader.readInt();
      if (magicNum != 524291) {
         throw new RuntimeException("Not valid AXML format");
      } else {
         int size = reader.readInt();
         this.mDocSize = size;
         int chunkType = reader.readInt();
         if (chunkType == 1835009) {
            this.parseStringBlock(reader);
         }

         chunkType = reader.readInt();
         if (chunkType == 524672) {
            this.parseResourceBlock(reader);
         }

         chunkType = reader.readInt();
         if (chunkType == 1048832) {
            this.parseXMLTree(reader);
         }

      }
   }

   public void release() {
      try {
         if (this.is != null) {
            this.is.close();
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   private void parseStringBlock(IntReader reader) throws Exception {
      StringBlock block = new StringBlock();
      block.read(reader);
      mStringBlock = block;
   }

   private void parseResourceBlock(IntReader reader) throws IOException {
      ResBlock block = new ResBlock();
      block.read(reader);
      mResBlock = block;
   }

   private void parseXMLTree(IntReader reader) throws Exception {
      BXMLTree tree = new BXMLTree();
      tree.read(reader);
      this.mXMLTree = tree;
   }
}
