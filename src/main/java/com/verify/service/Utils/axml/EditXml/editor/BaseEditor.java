package com.verify.service.Utils.axml.EditXml.editor;

import com.verify.service.Utils.axml.EditXml.decode.AXMLDoc;
import com.verify.service.Utils.axml.EditXml.decode.BXMLNode;
import com.verify.service.Utils.axml.EditXml.decode.StringBlock;

public abstract class BaseEditor<T> implements XEditor {
   protected AXMLDoc doc;
   protected String attrName;
   protected String arrrValue;
   protected int namespace;
   protected int attr_name;
   protected int attr_value;
   protected T editorInfo;

   public BaseEditor(AXMLDoc doc) {
      this.doc = doc;
   }

   public void setEditorInfo(T editorInfo) {
      this.editorInfo = editorInfo;
   }

   public void setEditor(String attrName, String attrValue) {
      this.attrName = attrName;
      this.arrrValue = attrValue;
   }

   public void commit() {
      if (this.editorInfo != null) {
         AXMLDoc var10001 = this.doc;
         this.registStringBlock(AXMLDoc.getStringBlock());
         this.editor();
      }

   }

   public abstract String getEditorName();

   protected abstract void editor();

   protected abstract BXMLNode findNode();

   protected abstract void registStringBlock(StringBlock block);
}
