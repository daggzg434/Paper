package com.verify.service.Utils.axml.EditXml.editor;

import com.verify.service.Utils.axml.EditXml.decode.AXMLDoc;
import com.verify.service.Utils.axml.EditXml.decode.BTagNode;
import com.verify.service.Utils.axml.EditXml.decode.BXMLNode;
import com.verify.service.Utils.axml.EditXml.decode.StringBlock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SpActivityEditor extends BaseEditor<SpActivityEditor.EditorInfo> {
   private int activity;
   public String MainName;

   public SpActivityEditor(AXMLDoc doc) {
      super(doc);
   }

   public String getEditorName() {
      return "activity";
   }

   private String ParseMain(AXMLDoc doc) {
      String Name = null;
      List<BXMLNode> children = doc.getApplicationNode().getChildren();
      Iterator iterator = children.iterator();

      label73:
      while(true) {
         BTagNode n;
         do {
            do {
               if (!iterator.hasNext()) {
                  return null;
               }

               n = (BTagNode)iterator.next();
            } while(!n.getmRawName_Data().equals("activity"));

            BTagNode.Attribute[] var6 = n.getAttribute();
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               BTagNode.Attribute attr = var6[var8];
               if (attr.mName_Data.equals("name")) {
                  Name = attr.mValue_Data;
               }
            }
         } while(n.getChildren() == null);

         Iterator var16 = n.getChildren().iterator();

         while(true) {
            BTagNode i;
            do {
               do {
                  if (!var16.hasNext()) {
                     continue label73;
                  }

                  BXMLNode tag = (BXMLNode)var16.next();
                  i = (BTagNode)tag;
               } while(!i.getmRawName_Data().equals("intent-filter"));
            } while(i.getChildren() == null);

            Iterator var19 = i.getChildren().iterator();

            while(var19.hasNext()) {
               BXMLNode k = (BXMLNode)var19.next();
               BTagNode action = (BTagNode)k;
               BTagNode.Attribute[] var12 = action.getAttribute();
               int var13 = var12.length;

               for(int var14 = 0; var14 < var13; ++var14) {
                  BTagNode.Attribute attr = var12[var14];
                  if (attr.mName_Data.equals("name") && "android.intent.category.LAUNCHER".equals(attr.mValue_Data)) {
                     return Name;
                  }
               }
            }
         }
      }
   }

   public void SetMainClass(String cla) {
      this.MainName = cla;
   }

   protected void editor() {
      List<BXMLNode> children = this.findNode().getChildren();
      Iterator iterator = children.iterator();

      while(true) {
         BTagNode n;
         do {
            if (!iterator.hasNext()) {
               return;
            }

            n = (BTagNode)iterator.next();
         } while(!n.getmRawName_Data().equals(this.getEditorName()));

         BTagNode.Attribute[] var4 = n.getAttribute();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            BTagNode.Attribute a_attr = var4[var6];
            if (a_attr.mName_Data.equals("name") && a_attr.mString_Data.equals(this.MainName)) {
               BTagNode a = new BTagNode(-1, this.activity, this.getEditorName());
               BTagNode.Attribute[] var9 = n.getAttribute();
               int var10 = var9.length;

               BTagNode.Attribute attr;
               for(int var11 = 0; var11 < var10; ++var11) {
                  attr = var9[var11];
                  BTagNode.Attribute k = new BTagNode.Attribute(attr.mNameSpace, attr.mName, attr.mType);
                  if (attr.mValue_Data != null) {
                     k.setValue(attr.mType >> 24, attr.mValue, attr.mValue_Data);
                  } else {
                     k.setValue(attr.mType >> 24, attr.mValue, (String)null);
                  }

                  a.setAttribute(k);
               }

               children.add(a);
               n.setAttrStringForKey(this.attr_name, ((SpActivityEditor.EditorInfo)this.editorInfo).Name_Index, ((SpActivityEditor.EditorInfo)this.editorInfo).Name);
               List<BTagNode.Attribute> curr_attr = Arrays.asList(n.getAttribute());
               List<BTagNode.Attribute> newattr = new ArrayList();
               Iterator var16 = curr_attr.iterator();

               while(var16.hasNext()) {
                  attr = (BTagNode.Attribute)var16.next();
                  if (attr.mName_Data.equals("name")) {
                     newattr.add(attr);
                  }
               }

               n.setAttribute((BTagNode.Attribute[])newattr.toArray(new BTagNode.Attribute[newattr.size()]));
               return;
            }
         }
      }
   }

   protected BXMLNode findNode() {
      return this.doc.getApplicationNode();
   }

   protected void registStringBlock(StringBlock block) {
      this.namespace = block.putString("http://schemas.android.com/apk/res/android");
      this.activity = block.putString("activity");
      this.attr_name = block.putString("name");
      ((SpActivityEditor.EditorInfo)this.editorInfo).Name_Index = block.putString(((SpActivityEditor.EditorInfo)this.editorInfo).Name);
   }

   public static class EditorInfo {
      public String Name;
      public int Name_Index;

      public EditorInfo(String name) {
         this.Name = name;
      }
   }
}
