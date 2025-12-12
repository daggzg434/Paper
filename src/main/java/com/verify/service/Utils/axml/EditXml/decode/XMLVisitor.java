package com.verify.service.Utils.axml.EditXml.decode;

import java.util.Iterator;

public class XMLVisitor implements IVisitor {
   private StringBlock mStrings;
   private ResBlock mRes;
   private int depth;
   final String intent = "                                ";
   final int step = 4;
   private static final float[] RADIX_MULTS = new float[]{0.00390625F, 3.051758E-5F, 1.192093E-7F, 4.656613E-10F};
   private static final String[] DIMENSION_UNITS = new String[]{"px", "dip", "sp", "pt", "in", "mm", "", ""};
   private static final String[] FRACTION_UNITS = new String[]{"%", "%p", "", "", "", "", "", ""};

   public XMLVisitor(StringBlock sb) {
      this.mStrings = sb;
   }

   public void visit(BNSNode node) {
      int prefix = node.getPrefix();
      int uri = node.getUri();
      String line1 = String.format("xmlns:%s=%s", this.getStringAt(prefix), this.getStringAt(uri));
      System.out.println(line1);
      if (node.hasChild()) {
         Iterator var5 = node.getChildren().iterator();

         while(var5.hasNext()) {
            BXMLNode child = (BXMLNode)var5.next();
            child.accept(this);
         }
      }

   }

   public void visit(BTagNode node) {
      if (!node.hasChild()) {
         this.print("<" + this.getStringAt(node.getName()));
         this.printAttribute(node.getAttribute());
         this.print("/>");
      } else {
         this.print("<" + this.getStringAt(node.getName()));
         ++this.depth;
         this.printAttribute(node.getAttribute());
         this.print(">");
         Iterator var2 = node.getChildren().iterator();

         while(var2.hasNext()) {
            BXMLNode child = (BXMLNode)var2.next();
            child.accept(this);
         }

         --this.depth;
         this.print("</" + this.getStringAt(node.getName()) + ">");
      }

   }

   public void visit(BTXTNode node) {
      this.print("Text node");
   }

   private void printAttribute(BTagNode.Attribute[] attrs) {
      BTagNode.Attribute[] var2 = attrs;
      int var3 = attrs.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         BTagNode.Attribute attr = var2[var4];
         StringBuilder sb = new StringBuilder();
         if (attr.hasNamespace()) {
            sb.append("android").append(':');
         }

         String name = this.getStringAt(attr.mName);
         if ("id".equals(name)) {
            System.out.println("hehe");
         }

         sb.append(name).append('=');
         sb.append('"').append(this.getAttributeValue(attr)).append('"');
         this.print(sb.toString());
      }

   }

   private void print(String str) {
      System.out.println("                                ".substring(0, this.depth * 4) + str);
   }

   private String getStringAt(int index) {
      return this.mStrings.getStringFor(index);
   }

   private int getResIdAt(int index) {
      return this.mRes.getResourceIdAt(index);
   }

   private String getAttributeValue(BTagNode.Attribute attr) {
      int type = attr.mType >> 24;
      int data = attr.mValue;
      if (type == 3) {
         return this.mStrings.getStringFor(attr.mString);
      } else if (type == 2) {
         return String.format("?%s%08X", this.getPackage(data), data);
      } else if (type == 1) {
         return String.format("@%s%08X", this.getPackage(data), data);
      } else if (type == 4) {
         return String.valueOf(Float.intBitsToFloat(data));
      } else if (type == 17) {
         return String.format("0x%08X", data);
      } else if (type == 18) {
         return data != 0 ? "true" : "false";
      } else if (type == 5) {
         return Float.toString(complexToFloat(data)) + DIMENSION_UNITS[data & 15];
      } else if (type == 6) {
         return Float.toString(complexToFloat(data)) + FRACTION_UNITS[data & 15];
      } else if (type >= 28 && type <= 31) {
         return String.format("#%08X", data);
      } else {
         return type >= 16 && type <= 31 ? String.valueOf(data) : String.format("<0x%X, type 0x%02X>", data, type);
      }
   }

   private String getPackage(int id) {
      return id >>> 24 == 1 ? "android:" : "";
   }

   public static float complexToFloat(int complex) {
      return (float)(complex & -256) * RADIX_MULTS[complex >> 4 & 3];
   }
}
