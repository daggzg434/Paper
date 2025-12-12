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

public class Manifest_ids {
   private static final String MANIFEST_ATTR_XML = "manifest_ids.xml";
   private static Manifest_ids instance;
   public static HashMap<String, Integer> IdsMap = new HashMap();

   public static Manifest_ids getInstance() {
      if (instance == null) {
         try {
            instance = new Manifest_ids();
         } catch (Exception var1) {
            var1.printStackTrace();
         }
      }

      return instance;
   }

   private Manifest_ids() {
      this.parseAll();
   }

   private void parseAll() {
      this.parse(this.loadXML("manifest_ids.xml"));
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
         if (tempNode.getNodeName().equals("public") && tempNode.getAttributes().getNamedItem("type") != null && tempNode.getAttributes().getNamedItem("type").getNodeValue().equals("attr")) {
            String ids = tempNode.getAttributes().getNamedItem("id").getNodeValue();
            IdsMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), Integer.parseInt(ids.substring(2, ids.length()), 16));
         }

         if (tempNode.getNodeType() == 1 && tempNode.hasAttributes() && tempNode.hasChildNodes()) {
            this.parseAttrList(tempNode.getChildNodes());
         }
      }

   }

   public int parseids(String key) {
      Iterator var2 = IdsMap.entrySet().iterator();

      Entry entry;
      do {
         if (!var2.hasNext()) {
            return 0;
         }

         entry = (Entry)var2.next();
      } while(!((String)entry.getKey()).equals(key));

      return (Integer)entry.getValue();
   }
}
