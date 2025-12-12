package com.verify.service.Utils.axml.EditXml.editor;

import com.verify.service.Utils.axml.EditXml.decode.AXMLDoc;
import com.verify.service.Utils.axml.EditXml.decode.BTagNode;
import com.verify.service.Utils.axml.EditXml.decode.BXMLNode;
import com.verify.service.Utils.axml.EditXml.decode.StringBlock;

public class MetaDataEditor extends BaseEditor<MetaDataEditor.EditorInfo> {
   private int MetaData_Index;
   private int MetaName_Index;
   private int MetaValue_Index;

   public MetaDataEditor(AXMLDoc doc) {
      super(doc);
   }

   public String getEditorName() {
      return "meta-data";
   }

   protected void editor() {
      BTagNode meta = new BTagNode(-1, this.MetaData_Index, this.getEditorName());
      BTagNode.Attribute name_attr = new BTagNode.Attribute(this.namespace, this.MetaName_Index, 3);
      name_attr.setString(((MetaDataEditor.EditorInfo)this.editorInfo).Name_Index, ((MetaDataEditor.EditorInfo)this.editorInfo).Name);
      BTagNode.Attribute value_attr = new BTagNode.Attribute(this.namespace, this.MetaValue_Index, 3);
      value_attr.setString(((MetaDataEditor.EditorInfo)this.editorInfo).Valua_Index, ((MetaDataEditor.EditorInfo)this.editorInfo).Value);
      meta.setAttribute(name_attr);
      meta.setAttribute(value_attr);
      this.findNode().addChild(meta);
   }

   protected BXMLNode findNode() {
      return this.doc.getApplicationNode();
   }

   protected void registStringBlock(StringBlock block) {
      this.namespace = block.putString("http://schemas.android.com/apk/res/android");
      this.MetaData_Index = block.putString("meta-data");
      ((MetaDataEditor.EditorInfo)this.editorInfo).Name_Index = block.putString(((MetaDataEditor.EditorInfo)this.editorInfo).Name);
      ((MetaDataEditor.EditorInfo)this.editorInfo).Valua_Index = block.putString(((MetaDataEditor.EditorInfo)this.editorInfo).Value);
      this.MetaName_Index = block.putString("name");
      this.MetaValue_Index = block.putString("value");
   }

   public static class EditorInfo {
      public String Name;
      public String Value;
      public int Name_Index;
      public int Valua_Index;

      public EditorInfo(String name, String value) {
         this.Name = name;
         this.Value = value;
      }
   }
}
