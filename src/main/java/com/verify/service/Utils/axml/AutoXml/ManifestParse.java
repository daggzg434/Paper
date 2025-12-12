package com.verify.service.Utils.axml.AutoXml;

import com.verify.service.Utils.axml.AutoXml.xml.decode.AXmlDecoder;
import com.verify.service.Utils.axml.AutoXml.xml.decode.AXmlResourceParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ManifestParse {
   public static String PackageName = null;

   public static void writeInt(byte[] data, int off, int value) {
      data[off++] = (byte)(value & 255);
      data[off++] = (byte)(value >>> 8 & 255);
      data[off++] = (byte)(value >>> 16 & 255);
      data[off] = (byte)(value >>> 24 & 255);
   }

   public static int readInt(byte[] data, int off) {
      return data[off + 3] << 24 | (data[off + 2] & 255) << 16 | (data[off + 1] & 255) << 8 | data[off] & 255;
   }

   public static List<String> parseManifestActivity(InputStream is) throws IOException {
      List<String> list = new ArrayList();
      AXmlDecoder axml = AXmlDecoder.decode(is);
      AXmlResourceParser parser = new AXmlResourceParser();
      parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);

      while(true) {
         while(true) {
            int type;
            do {
               if ((type = parser.next()) == 1) {
                  return list;
               }
            } while(type != 2);

            int size;
            int i;
            if (parser.getName().equals("manifest")) {
               size = parser.getAttributeCount();

               for(i = 0; i < size; ++i) {
                  if (parser.getAttributeName(i).equals("package")) {
                     PackageName = parser.getAttributeValue(i);
                  }
               }
            } else if (parser.getName().equals("activity")) {
               size = parser.getAttributeCount();

               for(i = 0; i < size; ++i) {
                  if (parser.getAttributeNameResource(i) == 16842755) {
                     String name = parser.getAttributeValue(i);
                     if (name.startsWith(".")) {
                        name = PackageName + name;
                     }

                     list.add(name);
                  }
               }
            }
         }
      }
   }

   public static String parseManifestPackageName(InputStream is) throws IOException {
      AXmlDecoder axml = AXmlDecoder.decode(is);
      AXmlResourceParser parser = new AXmlResourceParser();
      parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);

      while(true) {
         int type;
         do {
            do {
               if ((type = parser.next()) == 1) {
                  return null;
               }
            } while(type != 2);
         } while(!parser.getName().equals("manifest"));

         int size = parser.getAttributeCount();

         for(int i = 0; i < size; ++i) {
            if (parser.getAttributeName(i).equals("package")) {
               return parser.getAttributeValue(i);
            }
         }
      }
   }

   public static int parseManifestVer(InputStream is) throws IOException {
      AXmlDecoder axml = AXmlDecoder.decode(is);
      AXmlResourceParser parser = new AXmlResourceParser();
      parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);

      while(true) {
         int type;
         do {
            do {
               if ((type = parser.next()) == 1) {
                  return 0;
               }
            } while(type != 2);
         } while(!parser.getName().equals("manifest"));

         int size = parser.getAttributeCount();

         for(int i = 0; i < size; ++i) {
            if (parser.getAttributeName(i).equals("versionCode")) {
               return parser.getAttributeIntValue(i, 0);
            }
         }
      }
   }

   public static String parseMainActivity(InputStream is) throws IOException {
      String class_name = null;
      AXmlDecoder axml = AXmlDecoder.decode(is);
      AXmlResourceParser parser = new AXmlResourceParser();
      parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);

      while(true) {
         while(true) {
            int type;
            do {
               if ((type = parser.next()) == 1) {
                  return null;
               }
            } while(type != 2);

            int size;
            int i;
            if (parser.getName().equals("activity")) {
               size = parser.getAttributeCount();

               for(i = 0; i < size; ++i) {
                  if (parser.getAttributeNameResource(i) == 16842755) {
                     class_name = parser.getAttributeValue(i);
                  }
               }
            } else if (parser.getName().equals("category")) {
               size = parser.getAttributeCount();

               for(i = 0; i < size; ++i) {
                  if (parser.getAttributeNameResource(i) == 16842755 && parser.getAttributeValue(i).equals("android.intent.category.LAUNCHER")) {
                     return class_name;
                  }
               }
            }
         }
      }
   }
}
