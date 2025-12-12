package com.verify.service.Utils;

import com.verify.service.ServiceApplication;
import com.verify.service.Utils.axml.ManifestAttributes;
import com.verify.service.Utils.axml.AutoXml.ManifestParse;
import com.verify.service.Utils.axml.EditXml.decode.AXMLDoc;
import com.verify.service.Utils.axml.EditXml.decode.BTagNode;
import com.verify.service.Utils.axml.EditXml.decode.BXMLNode;
import com.verify.service.Utils.axml.EditXml.editor.SpActivityEditor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class AutoXml {
   public static byte[] Auto(byte[] Xml, String xml_path, Properties conf) throws Exception {
      ManifestAttributes attr = ManifestAttributes.getInstance();
      AXMLDoc axmlDoc = new AXMLDoc();
      axmlDoc.parse(new ByteArrayInputStream(Xml));
      XmlPullParserFactory f = XmlPullParserFactory.newInstance();
      f.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
      XmlPullParser p = f.newPullParser();
      p.setInput(new FileInputStream(ServiceApplication.temp_static + File.separator + xml_path), "utf-8");
      int ns = AXMLDoc.getStringBlock().putString("http://schemas.android.com/apk/res/android");
      Map<Integer, BTagNode> root_node = null;
      String root_name = null;

      label183:
      for(int i = p.getEventType(); i != 1; i = p.next()) {
         int k;
         switch(i) {
         case 2:
            if (p.getName().equals("manifest")) {
               break;
            }

            int uses_name_index = AXMLDoc.getStringBlock().putString(p.getName());
            List<BTagNode.Attribute> attributeList = new ArrayList();

            for(int in = 0; in < p.getAttributeCount(); ++in) {
               String name = p.getAttributeName(in);
               String value = p.getAttributeValue(in);
               if ("replace".equals(name) && "true".equals(value)) {
                  k = 0;

                  while(true) {
                     if (k >= p.getAttributeCount()) {
                        continue label183;
                     }

                     if ("name".equals(p.getAttributeName(k))) {
                        String main_class = ManifestParse.parseMainActivity(new ByteArrayInputStream(Xml));
                        if (main_class == null) {
                           throw new Exception("Main Class Not Find");
                        }

                        SpActivityEditor spActivityEditor = new SpActivityEditor(axmlDoc);
                        spActivityEditor.SetMainClass(main_class);
                        spActivityEditor.setEditorInfo(new SpActivityEditor.EditorInfo(p.getAttributeValue(k)));
                        spActivityEditor.commit();
                        conf.setProperty("MainClass", main_class);
                     }

                     ++k;
                  }
               }

               k = attr.decodeType(name);
               BTagNode.Attribute attribute = new BTagNode.Attribute(ns, AXMLDoc.getStringBlock().putString(name), k);
               switch(k) {
               case 0:
                  String v = attr.decodeValue(value);
                  if (v.startsWith("0x")) {
                     attribute.setValue(17, Integer.parseInt(v.substring(2), 16), (String)null);
                  } else {
                     attribute.setValue(16, Integer.parseInt(v), (String)null);
                  }
                  break;
               case 1:
                  if (value.startsWith("@")) {
                     attribute.setValue(1, Integer.parseInt(value.substring(1), 16), (String)null);
                  }
                  break;
               case 3:
                  if (p.getName().equals("provider") && name.equals("authorities")) {
                     value = ManifestParse.parseManifestPackageName(new ByteArrayInputStream(Xml)) + value;
                  }

                  if (value.startsWith("@")) {
                     attribute.setValue(1, Integer.parseInt(value.substring(1), 16), (String)null);
                  } else {
                     attribute.setString(AXMLDoc.getStringBlock().putString(value), value);
                  }
                  break;
               case 16:
                  attribute.setValue(16, Integer.parseInt(attr.decodeValue(value)), (String)null);
                  break;
               case 18:
                  attribute.setValue(18, value.equals("true") ? 1 : 0, (String)null);
               }

               attributeList.add(attribute);
            }

            BTagNode node;
            BTagNode.Attribute a;
            Iterator var29;
            if (root_node == null) {
               root_name = p.getName();
               root_node = new HashMap();
               node = new BTagNode(-1, uses_name_index, p.getName());
               var29 = attributeList.iterator();

               while(var29.hasNext()) {
                  a = (BTagNode.Attribute)var29.next();
                  node.setAttribute(a);
               }

               root_node.put(p.getDepth(), node);
            } else {
               node = new BTagNode(-1, uses_name_index, p.getName());
               var29 = attributeList.iterator();

               while(var29.hasNext()) {
                  a = (BTagNode.Attribute)var29.next();
                  node.setAttribute(a);
               }

               ((BTagNode)root_node.get(p.getDepth() - 1)).addChild(node);
               root_node.put(p.getDepth(), node);
            }
            break;
         case 3:
            if (p.getName().equals(root_name)) {
               String var13 = p.getName();
               byte var14 = -1;
               switch(var13.hashCode()) {
               case -1773650763:
                  if (var13.equals("uses-configuration")) {
                     var14 = 9;
                  }
                  break;
               case -1667688228:
                  if (var13.equals("permission-tree")) {
                     var14 = 5;
                  }
                  break;
               case -1655966961:
                  if (var13.equals("activity")) {
                     var14 = 11;
                  }
                  break;
               case -1356765254:
                  if (var13.equals("uses-library")) {
                     var14 = 17;
                  }
                  break;
               case -1115949454:
                  if (var13.equals("meta-data")) {
                     var14 = 13;
                  }
                  break;
               case -987494927:
                  if (var13.equals("provider")) {
                     var14 = 15;
                  }
                  break;
               case -808719889:
                  if (var13.equals("receiver")) {
                     var14 = 14;
                  }
                  break;
               case -517618225:
                  if (var13.equals("permission")) {
                     var14 = 2;
                  }
                  break;
               case -170723071:
                  if (var13.equals("permission-group")) {
                     var14 = 3;
                  }
                  break;
               case 544550766:
                  if (var13.equals("instrumentation")) {
                     var14 = 10;
                  }
                  break;
               case 599862896:
                  if (var13.equals("uses-permission")) {
                     var14 = 0;
                  }
                  break;
               case 790287890:
                  if (var13.equals("activity-alias")) {
                     var14 = 16;
                  }
                  break;
               case 941426460:
                  if (var13.equals("supports-gl-texture")) {
                     var14 = 4;
                  }
                  break;
               case 1343942321:
                  if (var13.equals("uses-permission-sdk-23")) {
                     var14 = 7;
                  }
                  break;
               case 1390744025:
                  if (var13.equals("uses-split")) {
                     var14 = 6;
                  }
                  break;
               case 1554253136:
                  if (var13.equals("application")) {
                     var14 = 18;
                  }
                  break;
               case 1792785909:
                  if (var13.equals("uses-feature")) {
                     var14 = 8;
                  }
                  break;
               case 1818228622:
                  if (var13.equals("compatible-screens")) {
                     var14 = 1;
                  }
                  break;
               case 1984153269:
                  if (var13.equals("service")) {
                     var14 = 12;
                  }
               }

               switch(var14) {
               case 0:
               case 1:
               case 2:
               case 3:
               case 4:
               case 5:
               case 6:
               case 7:
               case 8:
               case 9:
               case 10:
                  axmlDoc.getManifestNode().getChildren().add(root_node.get(p.getDepth()));
                  root_name = null;
                  root_node.clear();
                  root_node = null;
                  break;
               case 11:
               case 12:
               case 13:
               case 14:
               case 15:
               case 16:
               case 17:
                  axmlDoc.getApplicationNode().addChild((BXMLNode)root_node.get(p.getDepth()));
                  root_name = null;
                  root_node.clear();
                  root_node = null;
                  break;
               case 18:
                  BTagNode.Attribute[] var15 = ((BTagNode)root_node.get(p.getDepth())).getAttribute();
                  k = var15.length;

                  for(int var17 = 0; var17 < k; ++var17) {
                     BTagNode.Attribute attribute = var15[var17];
                     ((BTagNode)axmlDoc.getApplicationNode()).setAttribute(attribute);
                  }

                  root_name = null;
                  root_node.clear();
                  root_node = null;
               }
            }
         }
      }

      ByteArrayOutputStream Xml_Out = new ByteArrayOutputStream();
      axmlDoc.build(Xml_Out);
      axmlDoc.release();
      return Xml_Out.toByteArray();
   }
}
