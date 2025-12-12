package com.verify.service.Utils.axml.EditXml.decode;

import java.io.IOException;

public class ResBlock implements IAXMLSerialize {
   private static final int TAG = 524672;
   private int mChunkSize;
   private int[] mRawResIds;
   private final int INT_SIZE = 4;

   public void setmRawResIds(int[] mRawResIds) {
      this.mRawResIds = mRawResIds;
   }

   public boolean IsId(int id) {
      int[] var2 = this.mRawResIds;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int i = var2[var4];
         if (i == id) {
            return true;
         }
      }

      return false;
   }

   public void print() {
      StringBuilder sb = new StringBuilder();
      int[] var2 = this.getResourceIds();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int id = var2[var4];
         sb.append(id);
         sb.append(" ");
      }

      System.out.println(sb.toString());
   }

   public void read(IntReader reader) throws IOException {
      this.mChunkSize = reader.readInt();
      if (this.mChunkSize >= 8 && this.mChunkSize % 4 == 0) {
         this.mRawResIds = reader.readIntArray(this.mChunkSize / 4 - 2);
      } else {
         throw new IOException("Invalid resource ids size (" + this.mChunkSize + ").");
      }
   }

   public void prepare() {
      int base = 8;
      int resSize = this.mRawResIds == null ? 0 : this.mRawResIds.length * 4;
      this.mChunkSize = base + resSize;
   }

   public void write(IntWriter writer) throws IOException {
      writer.writeInt(524672);
      writer.writeInt(this.mChunkSize);
      if (this.mRawResIds != null) {
         int[] var2 = this.mRawResIds;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            int id = var2[var4];
            writer.writeInt(id);
         }
      }

   }

   public int[] getResourceIds() {
      return this.mRawResIds;
   }

   public int getResourceIdAt(int index) {
      return this.mRawResIds[index];
   }

   public int getSize() {
      return this.mChunkSize;
   }

   public int getType() {
      return 524672;
   }

   public void setSize(int size) {
   }

   public void setType(int type) {
   }
}
