package com.verify.service.Utils.axml.EditXml.editor;

import com.verify.service.Utils.axml.EditXml.decode.AXMLDoc;
import com.verify.service.Utils.axml.EditXml.decode.BTagNode;
import com.verify.service.Utils.axml.EditXml.decode.BXMLNode;
import com.verify.service.Utils.axml.EditXml.decode.StringBlock;

public class ApplicationEditor extends BaseEditor<ApplicationEditor.EditorInfo> {
   public boolean flag = false;
   public String AppName;

   public ApplicationEditor(AXMLDoc doc) {
      super(doc);
   }

   public String getEditorName() {
      return "application";
   }

   protected void editor() {
      BTagNode bTagNode = (BTagNode)this.findNode();
      BTagNode.Attribute[] var2 = bTagNode.getAttribute();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         BTagNode.Attribute attr = var2[var4];
         if (attr.mName == this.attr_name) {
            this.flag = true;
            this.AppName = AXMLDoc.getStringBlock().getStringFor(attr.mValue);
            if (this.AppName.startsWith(".")) {
               BTagNode.Attribute[] var6 = ((BTagNode)this.doc.getManifestNode()).getAttribute();
               int var7 = var6.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  BTagNode.Attribute a = var6[var8];
                  if (a.mName == AXMLDoc.getStringBlock().getStringMapping("package")) {
                     this.AppName = AXMLDoc.getStringBlock().getStringFor(a.mValue) + this.AppName;
                  }
               }
            }

            attr.setString(((ApplicationEditor.EditorInfo)this.editorInfo).Name_Index, ((ApplicationEditor.EditorInfo)this.editorInfo).Name);
         }
      }

      if (!this.flag) {
         BTagNode.Attribute attribute = new BTagNode.Attribute(this.namespace, this.attr_name, 3);
         attribute.setValue(3, ((ApplicationEditor.EditorInfo)this.editorInfo).Name_Index, ((ApplicationEditor.EditorInfo)this.editorInfo).Name);
         ((BTagNode)this.findNode()).setAttribute2(attribute);
      }

   }

   protected BXMLNode findNode() {
      return this.doc.getApplicationNode();
   }

   protected void registStringBlock(StringBlock block) {
      this.namespace = block.putString("http://schemas.android.com/apk/res/android");
      ((ApplicationEditor.EditorInfo)this.editorInfo).Name_Index = block.putString(((ApplicationEditor.EditorInfo)this.editorInfo).Name);
      this.attr_name = block.putString("name");
   }

   public static class EditorInfo {
      public int Name_Index;
      public String Name;

      public EditorInfo(String name) {
         this.Name = name;
      }
   }
}
