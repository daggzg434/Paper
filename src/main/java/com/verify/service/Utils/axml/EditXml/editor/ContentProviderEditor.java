package com.verify.service.Utils.axml.EditXml.editor;

import com.verify.service.Utils.axml.EditXml.decode.AXMLDoc;
import com.verify.service.Utils.axml.EditXml.decode.BTagNode;
import com.verify.service.Utils.axml.EditXml.decode.BXMLNode;
import com.verify.service.Utils.axml.EditXml.decode.StringBlock;

public class ContentProviderEditor extends BaseEditor<ContentProviderEditor.EditorInfo> {
   private int provider_Index;
   private int provider_authorities_Index;
   private int provider_name_Index;
   private int provider_exported_Index;

   public ContentProviderEditor(AXMLDoc doc) {
      super(doc);
   }

   public String getEditorName() {
      return "provider";
   }

   protected void editor() {
      BTagNode provider = new BTagNode(-1, this.provider_Index, this.getEditorName());
      BTagNode.Attribute authorities_attr = new BTagNode.Attribute(this.namespace, this.provider_authorities_Index, 3);
      authorities_attr.setString(((ContentProviderEditor.EditorInfo)this.editorInfo).authorities_Index, ((ContentProviderEditor.EditorInfo)this.editorInfo).authorities);
      BTagNode.Attribute name_attr = new BTagNode.Attribute(this.namespace, this.provider_name_Index, 3);
      name_attr.setString(((ContentProviderEditor.EditorInfo)this.editorInfo).name_Index, ((ContentProviderEditor.EditorInfo)this.editorInfo).name);
      BTagNode.Attribute exported_attr = new BTagNode.Attribute(this.namespace, this.provider_exported_Index, 18);
      exported_attr.setValue(18, ((ContentProviderEditor.EditorInfo)this.editorInfo).exported ? 1 : 0, (String)null);
      provider.setAttribute(name_attr);
      provider.setAttribute(exported_attr);
      provider.setAttribute(authorities_attr);
      this.findNode().addChild(provider);
   }

   protected BXMLNode findNode() {
      return this.doc.getApplicationNode();
   }

   protected void registStringBlock(StringBlock block) {
      this.namespace = block.putString("http://schemas.android.com/apk/res/android");
      this.provider_Index = block.putString("provider");
      ((ContentProviderEditor.EditorInfo)this.editorInfo).name_Index = block.putString(((ContentProviderEditor.EditorInfo)this.editorInfo).name);
      ((ContentProviderEditor.EditorInfo)this.editorInfo).authorities_Index = block.putString(((ContentProviderEditor.EditorInfo)this.editorInfo).authorities);
      this.provider_authorities_Index = block.putString("authorities");
      this.provider_name_Index = block.putString("name");
      this.provider_exported_Index = block.putString("exported");
   }

   public static class EditorInfo {
      public String authorities;
      public String name;
      public boolean exported;
      public int authorities_Index;
      public int name_Index;

      public EditorInfo(String authorities, String name, boolean exported) {
         this.authorities = authorities;
         this.name = name;
         this.exported = exported;
      }
   }
}
