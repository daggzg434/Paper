package com.verify.service.Utils.axml;

import com.verify.service.ServiceApplication;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ManifestAttributes {
   private static final String MANIFEST_ATTR_XML = "attrs_manifest.xml";
   private static ManifestAttributes instance;
   public static HashMap<String, Integer> TypeMap = new HashMap();
   public static HashMap<String, String> ValueMap = new HashMap();

   public static ManifestAttributes getInstance() {
      if (instance == null) {
         try {
            instance = new ManifestAttributes();
         } catch (Exception var1) {
            var1.printStackTrace();
         }
      }

      return instance;
   }

   private ManifestAttributes() {
      this.parseAll();
   }

   private void parseAll() {
      this.parse(this.loadXML("attrs_manifest.xml"));
   }

   private Document loadXML(String xml) {
      Document doc = null;

      try {
         InputStream xmlStream = new FileInputStream(ServiceApplication.temp_static + File.separator + xml);
         Throwable var4 = null;

         try {
            if (xmlStream == null) {
               throw new Exception(xml + " not found in classpath");
            }

            DocumentBuilder dBuilder = XmlSecurity.getSecureDbf().newDocumentBuilder();
            doc = dBuilder.parse(xmlStream);
         } catch (Throwable var14) {
            var4 = var14;
            throw var14;
         } finally {
            if (xmlStream != null) {
               if (var4 != null) {
                  try {
                     xmlStream.close();
                  } catch (Throwable var13) {
                     var4.addSuppressed(var13);
                  }
               } else {
                  xmlStream.close();
               }
            }

         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return doc;
   }

   private void parse(Document doc) {
      NodeList nodeList = doc.getChildNodes();

      for(int count = 0; count < nodeList.getLength(); ++count) {
         Node node = nodeList.item(count);
         if (node.getNodeType() == 1 && node.hasChildNodes()) {
            this.parseAttrList(node.getChildNodes());
         }
      }

   }

   private void parseAttrList(NodeList nodeList) {
      for(int count = 0; count < nodeList.getLength(); ++count) {
         Node tempNode = nodeList.item(count);
         if (tempNode.getNodeName().equals("attr")) {
            if (tempNode.getAttributes().getNamedItem("format") != null) {
               String var5 = tempNode.getAttributes().getNamedItem("format").getNodeValue();
               byte var6 = -1;
               switch(var5.hashCode()) {
               case -1926150010:
                  if (var5.equals("string|integer|color|float|boolean")) {
                     var6 = 5;
                  }
                  break;
               case -925155509:
                  if (var5.equals("reference")) {
                     var6 = 1;
                  }
                  break;
               case -891985903:
                  if (var5.equals("string")) {
                     var6 = 0;
                  }
                  break;
               case -568190494:
                  if (var5.equals("reference|string")) {
                     var6 = 4;
                  }
                  break;
               case 64711720:
                  if (var5.equals("boolean")) {
                     var6 = 3;
                  }
                  break;
               case 1958052158:
                  if (var5.equals("integer")) {
                     var6 = 2;
                  }
               }

               byte Type;
               switch(var6) {
               case 0:
                  Type = 3;
                  break;
               case 1:
                  Type = 1;
                  break;
               case 2:
                  Type = 16;
                  break;
               case 3:
                  Type = 18;
                  break;
               case 4:
                  Type = 3;
                  break;
               case 5:
                  Type = 3;
                  break;
               default:
                  Type = 0;
               }

               TypeMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), Integer.valueOf(Type));
            }
         } else if (tempNode.getNodeName().equals("enum")) {
            if (tempNode.getAttributes().getNamedItem("value") != null) {
               ValueMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), tempNode.getAttributes().getNamedItem("value").getNodeValue());
            }
         } else if (tempNode.getNodeName().equals("flag") && tempNode.getAttributes().getNamedItem("value") != null) {
            ValueMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), tempNode.getAttributes().getNamedItem("value").getNodeValue());
         }

         if (tempNode.getNodeType() == 1 && tempNode.hasAttributes() && tempNode.hasChildNodes()) {
            this.parseAttrList(tempNode.getChildNodes());
         }
      }

   }

   public int decodeType(String key) {
      Iterator var2 = TypeMap.entrySet().iterator();

      Entry entry;
      do {
         if (!var2.hasNext()) {
            return 0;
         }

         entry = (Entry)var2.next();
      } while(!((String)entry.getKey()).equals(key));

      return (Integer)entry.getValue();
   }

   public String decodeValue(String key) {
      Iterator var2 = ValueMap.entrySet().iterator();

      Entry entry;
      do {
         if (!var2.hasNext()) {
            return key;
         }

         entry = (Entry)var2.next();
      } while(!((String)entry.getKey()).equals(key));

      return (String)entry.getValue();
   }
}
