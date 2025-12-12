package com.verify.service.Utils.axml.EditXml.decode;

import com.verify.service.Utils.axml.Manifest_ids;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BTagNode extends BXMLNode {
   private final int TAG_START = 1048834;
   private final int TAG_END = 1048835;
   private int mRawNSUri;
   private int mRawName;
   private String mRawNSUri_Data;
   private String mRawName_Data;
   private short mRawAttrCount;
   private short mRawClassAttr;
   private short mRawIdAttr;
   private short mRawStyleAttr;
   private List<BTagNode.Attribute> mRawAttrs;
   private static final int INT_SIZE = 4;

   public String getmRawName_Data() {
      return this.mRawName_Data;
   }

   public BTagNode() {
   }

   public BTagNode(int ns, int name, String data) {
      this.mRawName = name;
      this.mRawNSUri = ns;
      this.mRawName_Data = data;
   }

   public void checkStartTag(int tag) throws IOException {
      this.checkTag(1048834, tag);
   }

   public void checkEndTag(int tag) throws IOException {
      this.checkTag(1048835, tag);
   }

   public void readStart(IntReader reader) throws IOException {
      super.readStart(reader);
      int xffff_ffff = reader.readInt();
      this.mRawNSUri = reader.readInt();
      this.mRawName = reader.readInt();
      this.mRawNSUri_Data = AXMLDoc.getStringBlock().getStringFor(this.mRawNSUri);
      this.mRawName_Data = AXMLDoc.getStringBlock().getStringFor(this.mRawName);
      int x0014_0014 = reader.readInt();
      this.mRawAttrCount = (short)reader.readShort();
      this.mRawIdAttr = (short)reader.readShort();
      this.mRawClassAttr = (short)reader.readShort();
      this.mRawStyleAttr = (short)reader.readShort();
      if (this.mRawAttrCount > 0) {
         if (this.mRawName == 62) {
            System.out.println();
         }

         this.mRawAttrs = new ArrayList();
         int[] attrs = reader.readIntArray(this.mRawAttrCount * 5);

         for(int i = 0; i < this.mRawAttrCount; ++i) {
            this.mRawAttrs.add(new BTagNode.Attribute(this.subArray(attrs, i * 5, 5)));
            BTagNode.Attribute var6 = (BTagNode.Attribute)this.mRawAttrs.get(i);
         }
      }

   }

   public void readEnd(IntReader reader) throws IOException {
      super.readEnd(reader);
      int xffff_ffff = reader.readInt();
      int ns_uri = reader.readInt();
      int name = reader.readInt();
      if (ns_uri != this.mRawNSUri || name != this.mRawName) {
         throw new IOException("Invalid end element");
      }
   }

   public void prepare() {
      int base_first = 36;
      this.mRawAttrCount = (short)(this.mRawAttrs == null ? 0 : this.mRawAttrs.size());
      int attrSize = this.mRawAttrs == null ? 0 : this.mRawAttrs.size() * 5 * 4;
      this.mChunkSize.first = base_first + attrSize;
      this.mChunkSize.second = 24;
   }

   public void writeStart(IntWriter writer) throws IOException {
      writer.writeInt(1048834);
      super.writeStart(writer);
      writer.writeInt(-1);
      writer.writeInt(this.mRawNSUri_Data == null ? this.mRawNSUri : AXMLDoc.getStringBlock().getStringMapping(this.mRawNSUri_Data));
      writer.writeInt(this.mRawName_Data == null ? this.mRawName : AXMLDoc.getStringBlock().getStringMapping(this.mRawName_Data));
      writer.writeInt(1310740);
      writer.writeShort(this.mRawAttrCount);
      writer.writeShort(this.mRawIdAttr);
      writer.writeShort(this.mRawClassAttr);
      writer.writeShort(this.mRawStyleAttr);
      if (this.mRawAttrCount > 0) {
         Collections.sort(this.mRawAttrs);
         Iterator var2 = this.mRawAttrs.iterator();

         while(var2.hasNext()) {
            BTagNode.Attribute attr = (BTagNode.Attribute)var2.next();
            writer.writeInt(attr.mNameSpace_Data == null ? attr.mNameSpace : AXMLDoc.getStringBlock().getStringMapping(attr.mNameSpace_Data));
            writer.writeInt(attr.mName_Data == null ? attr.mName : AXMLDoc.getStringBlock().getStringMapping(attr.mName_Data));
            writer.writeInt(attr.mString_Data == null ? attr.mString : AXMLDoc.getStringBlock().getStringMapping(attr.mString_Data));
            writer.writeInt(attr.mType);
            writer.writeInt(attr.mValue_Data == null ? attr.mValue : AXMLDoc.getStringBlock().getStringMapping(attr.mValue_Data));
         }
      }

   }

   public void writeEnd(IntWriter writer) throws IOException {
      writer.writeInt(1048835);
      super.writeEnd(writer);
      writer.writeInt(-1);
      writer.writeInt(this.mRawNSUri_Data == null ? this.mRawNSUri : AXMLDoc.getStringBlock().getStringMapping(this.mRawNSUri_Data));
      writer.writeInt(this.mRawName_Data == null ? this.mRawName : AXMLDoc.getStringBlock().getStringMapping(this.mRawName_Data));
   }

   public int getIdAttr() {
      return this.getAttrStringForKey(this.mRawIdAttr);
   }

   public int getClassAttr() {
      return this.getAttrStringForKey(this.mRawClassAttr);
   }

   public int getStyleAttr() {
      return this.getAttrStringForKey(this.mRawStyleAttr);
   }

   public BTagNode.Attribute[] getAttribute() {
      return this.mRawAttrs == null ? new BTagNode.Attribute[0] : (BTagNode.Attribute[])this.mRawAttrs.toArray(new BTagNode.Attribute[this.mRawAttrs.size()]);
   }

   public void setAttribute(BTagNode.Attribute attr) {
      if (this.mRawAttrs == null) {
         this.mRawAttrs = new ArrayList();
      }

      this.mRawAttrs.add(attr);
   }

   public void setAttribute2(BTagNode.Attribute attr) {
      if (this.mRawAttrs == null) {
         this.mRawAttrs = new ArrayList();
      }

      if (this.mRawAttrs.size() >= 3) {
         this.mRawAttrs.add(3, attr);
      } else {
         this.mRawAttrs.add(attr);
      }

   }

   public void setAttribute(BTagNode.Attribute[] attr) {
      if (this.mRawAttrs == null) {
         this.mRawAttrs = new ArrayList();
      }

      this.mRawAttrs.clear();
      BTagNode.Attribute[] var2 = attr;
      int var3 = attr.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         BTagNode.Attribute a = var2[var4];
         this.mRawAttrs.add(a);
      }

   }

   public int getAttrStringForKey(int key) {
      BTagNode.Attribute[] attrs = this.getAttribute();
      BTagNode.Attribute[] var3 = attrs;
      int var4 = attrs.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         BTagNode.Attribute attr = var3[var5];
         if (attr.mName == key) {
            return attr.mString;
         }
      }

      return -1;
   }

   public boolean setAttrStringForKey(int key, int string_value, String data) {
      BTagNode.Attribute[] attrs = this.getAttribute();
      BTagNode.Attribute[] var5 = attrs;
      int var6 = attrs.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         BTagNode.Attribute attr = var5[var7];
         if (attr.mName == key) {
            attr.setValue(3, string_value, data);
            return true;
         }
      }

      return false;
   }

   public boolean setAttrIntForKey(int key, int string_value, String data) {
      BTagNode.Attribute[] attrs = this.getAttribute();
      BTagNode.Attribute[] var5 = attrs;
      int var6 = attrs.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         BTagNode.Attribute attr = var5[var7];
         if (attr.mName == key) {
            attr.setValue(16, string_value, data);
            return true;
         }
      }

      return false;
   }

   public int[] getAttrValueForKey(int key) {
      BTagNode.Attribute[] attrs = this.getAttribute();
      BTagNode.Attribute[] var3 = attrs;
      int var4 = attrs.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         BTagNode.Attribute attr = var3[var5];
         if (attr.mName == key) {
            int[] type_value = new int[]{attr.mType, attr.mValue};
            return type_value;
         }
      }

      return null;
   }

   public boolean setAttrValueForKey(int key, int type, int value) {
      return false;
   }

   public int getName() {
      return this.mRawName;
   }

   public void setName(int name) {
      this.mRawName = name;
   }

   public int getNamesapce() {
      return this.mRawNSUri;
   }

   public void setNamespace(int ns) {
      this.mRawNSUri = ns;
   }

   private int[] subArray(int[] src, int start, int len) {
      if (start + len > src.length) {
         throw new RuntimeException("OutOfArrayBound");
      } else {
         int[] des = new int[len];
         System.arraycopy(src, start, des, 0, len);
         return des;
      }
   }

   public void accept(IVisitor v) {
      v.visit(this);
   }

   public static class Attribute implements Comparable<BTagNode.Attribute> {
      public static final int SIZE = 5;
      public int mNameSpace;
      public int mName;
      public int mString;
      public int mType;
      public int mValue;
      public int mId;
      private String mNameSpace_Data;
      public String mName_Data;
      public String mString_Data;
      public String mValue_Data;

      public Attribute(int ns, int name, int type) {
         this.mNameSpace = ns;
         this.mNameSpace_Data = "http://schemas.android.com/apk/res/android";
         this.mName = name;
         this.mName_Data = AXMLDoc.getStringBlock().getStringFor(this.mName);
         this.mId = Manifest_ids.getInstance().parseids(this.mName_Data);
         this.mType = type << 24;
      }

      public void setString(int str, String data) {
         if (this.mType >> 24 != 3) {
            throw new RuntimeException("Can't set string for none string type");
         } else {
            this.mString = str;
            this.mValue = str;
            this.mValue_Data = data;
            this.mString_Data = data;
         }
      }

      public void setValue(int type, int value, String data) {
         this.mType = type << 24;
         if (type == 3) {
            this.mValue = value;
            this.mString = value;
            this.mValue_Data = data;
            this.mString_Data = data;
         } else {
            this.mValue = value;
            this.mString = -1;
            this.mValue_Data = null;
            this.mString_Data = null;
         }

      }

      public Attribute(int[] raw) {
         this.mNameSpace = raw[0];
         this.mName = raw[1];
         this.mString = raw[2];
         this.mType = raw[3];
         this.mValue = raw[4];
         this.mNameSpace_Data = AXMLDoc.getStringBlock().getStringFor(this.mNameSpace);
         this.mName_Data = AXMLDoc.getStringBlock().getStringFor(this.mName);
         this.mString_Data = AXMLDoc.getStringBlock().getStringFor(this.mString);
         this.mValue_Data = AXMLDoc.getStringBlock().getStringFor(this.mValue);
         this.mId = Manifest_ids.getInstance().parseids(this.mName_Data);
      }

      public boolean hasNamespace() {
         return this.mNameSpace != -1;
      }

      public String toString() {
         return "Attribute{mNameSpace=" + this.mNameSpace + ", mName=" + this.mName + ", mString=" + this.mString + ", mType=" + this.mType + ", mValue=" + this.mValue + '}';
      }

      public int compareTo(BTagNode.Attribute attribute) {
         return this.mId = attribute.mId;
      }
   }
}
