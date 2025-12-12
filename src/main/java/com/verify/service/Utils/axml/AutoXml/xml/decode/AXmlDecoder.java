package com.verify.service.Utils.axml.AutoXml.xml.decode;

import com.verify.service.Utils.axml.AutoXml.io.ZInput;
import com.verify.service.Utils.axml.AutoXml.io.ZOutput;
import com.verify.service.Utils.axml.AutoXml.util.StringDecoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class AXmlDecoder {
   private static final int AXML_CHUNK_TYPE = 524291;
   public StringDecoder mTableStrings;
   private final ZInput mIn;
   byte[] data;

   private void readStrings() throws IOException {
      int type = this.mIn.readInt();
      this.checkChunk(type, 524291);
      this.mIn.readInt();
      this.mTableStrings = StringDecoder.read(this.mIn);
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      byte[] buf = new byte[2048];

      int num;
      while((num = this.mIn.read(buf, 0, 2048)) != -1) {
         byteOut.write(buf, 0, num);
      }

      this.data = byteOut.toByteArray();
      this.mIn.close();
      byteOut.close();
   }

   public byte[] getData() {
      return this.data;
   }

   public void setData(byte[] data) {
      this.data = data;
   }

   private AXmlDecoder(ZInput in) {
      this.mIn = in;
   }

   public static AXmlDecoder decode(InputStream input) throws IOException {
      AXmlDecoder axml = new AXmlDecoder(new ZInput(input));
      axml.readStrings();
      return axml;
   }

   public void write(List<String> list, OutputStream out) throws IOException {
      this.write(list, new ZOutput(out));
   }

   public void write(List<String> list, ZOutput out) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZOutput buf = new ZOutput(baos);
      String[] array = new String[list.size()];
      list.toArray(array);
      this.mTableStrings.write(array, buf);
      buf.writeFully(this.data);
      out.writeInt(524291);
      out.writeInt(baos.size() + 8);
      out.writeFully(baos.toByteArray());
      buf.close();
   }

   public void write(ZOutput out) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZOutput buf = new ZOutput(baos);
      this.mTableStrings.write(buf);
      buf.writeFully(this.data);
      out.writeInt(524291);
      out.writeInt(baos.size() + 8);
      out.writeFully(baos.toByteArray());
      baos.reset();
      buf.close();
   }

   public byte[] encode() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZOutput buf = new ZOutput(baos);
      this.mTableStrings.write(buf);
      buf.writeFully(this.data);
      byte[] bytes = baos.toByteArray();
      baos.reset();
      buf.writeInt(524291);
      buf.writeInt(bytes.length + 8);
      buf.writeFully(bytes);
      return baos.toByteArray();
   }

   private void checkChunk(int type, int expectedType) throws IOException {
      if (type != expectedType) {
         throw new IOException(String.format("Invalid chunk type: expected=0x%08x, got=0x%08x", expectedType, type));
      }
   }
}
