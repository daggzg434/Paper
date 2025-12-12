package com.verify.service.Utils.axml.AutoXml;

import com.verify.service.Utils.axml.AutoXml.xml.decode.AXmlDecoder;
import com.verify.service.Utils.axml.AutoXml.xml.decode.AXmlResourceParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ManifestAppName {
   public static boolean customApplication = false;
   public static String customApplicationName;
   public static String packageName;

   public static byte[] parseManifest(InputStream is, String Name) throws IOException {
      customApplication = false;
      customApplicationName = null;
      packageName = null;
      AXmlDecoder axml = AXmlDecoder.decode(is);
      AXmlResourceParser parser = new AXmlResourceParser();
      parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);
      boolean success = false;

      int type;
      while((type = parser.next()) != 1) {
         if (type == 2) {
            int size;
            int off;
            if (parser.getName().equals("manifest")) {
               size = parser.getAttributeCount();

               for(off = 0; off < size; ++off) {
                  if (parser.getAttributeName(off).equals("package")) {
                     packageName = parser.getAttributeValue(off);
                  }
               }
            } else if (parser.getName().equals("application")) {
               size = parser.getAttributeCount();

               byte[] newData;
               int chunkSize;
               for(off = 0; off < size; ++off) {
                  if (parser.getAttributeNameResource(off) == 16842755) {
                     customApplication = true;
                     customApplicationName = parser.getAttributeValue(off);
                     int index = axml.mTableStrings.getSize();
                     newData = axml.getData();
                     chunkSize = parser.currentAttributeStart + 20 * off;
                     chunkSize += 8;
                     ManifestParse.writeInt(newData, chunkSize, index);
                     chunkSize += 8;
                     ManifestParse.writeInt(newData, chunkSize, index);
                  }
               }

               if (!customApplication) {
                  off = parser.currentAttributeStart;
                  byte[] data = axml.getData();
                  newData = new byte[data.length + 20];
                  System.arraycopy(data, 0, newData, 0, off);
                  System.arraycopy(data, off, newData, off + 20, data.length - off);
                  chunkSize = ManifestParse.readInt(newData, off - 32);
                  ManifestParse.writeInt(newData, off - 32, chunkSize + 20);
                  ManifestParse.writeInt(newData, off - 8, size + 1);
                  int idIndex = parser.findResourceID(16842755);
                  if (idIndex == -1) {
                     throw new IOException("idIndex == -1");
                  }

                  boolean isMax = true;

                  for(int i = 0; i < size; ++i) {
                     int id = parser.getAttributeNameResource(i);
                     if (id > 16842755) {
                        isMax = false;
                        if (i != 0) {
                           System.arraycopy(newData, off + 20, newData, off, 20 * i);
                           off += 20 * i;
                        }
                        break;
                     }
                  }

                  if (isMax) {
                     System.arraycopy(newData, off + 20, newData, off, 20 * size);
                     off += 20 * size;
                  }

                  ManifestParse.writeInt(newData, off, axml.mTableStrings.find("http://schemas.android.com/apk/res/android"));
                  ManifestParse.writeInt(newData, off + 4, idIndex);
                  ManifestParse.writeInt(newData, off + 8, axml.mTableStrings.getSize());
                  ManifestParse.writeInt(newData, off + 12, 50331656);
                  ManifestParse.writeInt(newData, off + 16, axml.mTableStrings.getSize());
                  axml.setData(newData);
               }

               success = true;
               break;
            }
         }
      }

      if (!success) {
         throw new IOException();
      } else {
         ArrayList<String> list = new ArrayList(axml.mTableStrings.getSize());
         axml.mTableStrings.getStrings(list);
         list.add(Name);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         axml.write(list, (OutputStream)baos);
         return baos.toByteArray();
      }
   }
}
