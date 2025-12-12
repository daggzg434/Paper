package com.verify.service.Utils.axml.EditXml.editor;

import com.verify.service.Utils.axml.EditXml.decode.AXMLDoc;
import com.verify.service.Utils.axml.EditXml.decode.BTagNode;
import com.verify.service.Utils.axml.EditXml.decode.BXMLNode;
import com.verify.service.Utils.axml.EditXml.decode.StringBlock;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PermissionEditor extends BaseEditor<PermissionEditor.EditorInfo> {
   private int user_permission;

   public PermissionEditor(AXMLDoc doc) {
      super(doc);
   }

   public String getEditorName() {
      return "uses-permission";
   }

   protected void editor() {
      List<BXMLNode> children = this.findNode().getChildren();
      Iterator var2 = ((PermissionEditor.EditorInfo)this.editorInfo).editors.iterator();

      while(true) {
         while(true) {
            while(var2.hasNext()) {
               PermissionEditor.PermissionOpera opera = (PermissionEditor.PermissionOpera)var2.next();
               BTagNode n;
               if (opera.isRemove()) {
                  Iterator iterator = children.iterator();

                  while(iterator.hasNext()) {
                     n = (BTagNode)iterator.next();
                     if (this.user_permission == n.getName() && n.getAttrStringForKey(this.attr_name) == opera.permissionValue_Index) {
                        iterator.remove();
                        break;
                     }
                  }
               } else if (opera.isAdd()) {
                  BTagNode.Attribute permission_attr = new BTagNode.Attribute(this.namespace, this.attr_name, 3);
                  permission_attr.setString(opera.permissionValue_Index, opera.permission);
                  n = new BTagNode(-1, this.user_permission, this.getEditorName());
                  n.setAttribute(permission_attr);
                  children.add(n);
                  AXMLDoc var10000 = this.doc;
                  AXMLDoc.getStringBlock().setString(opera.permissionValue_Index, opera.permission);
               }
            }

            return;
         }
      }
   }

   protected BXMLNode findNode() {
      return this.doc.getManifestNode();
   }

   protected void registStringBlock(StringBlock sb) {
      this.namespace = sb.putString("http://schemas.android.com/apk/res/android");
      this.user_permission = sb.putString("uses-permission");
      this.attr_name = sb.putString("name");
      Iterator iterator = ((PermissionEditor.EditorInfo)this.editorInfo).editors.iterator();

      while(iterator.hasNext()) {
         PermissionEditor.PermissionOpera opera = (PermissionEditor.PermissionOpera)iterator.next();
         if (opera.isAdd()) {
            if (sb.containsString(opera.permission)) {
               iterator.remove();
            } else {
               opera.permissionValue_Index = sb.addString(opera.permission);
            }
         } else if (opera.isRemove()) {
            if (!sb.containsString(opera.permission)) {
               iterator.remove();
            } else {
               opera.permissionValue_Index = sb.getStringMapping(opera.permission);
            }
         }
      }

   }

   public static class PermissionOpera {
      private static final int ADD = 1;
      private static final int REMOVE = 2;
      private int opera = 0;
      private String permission;
      private int permissionValue_Index;

      public PermissionOpera(String permission) {
         this.permission = permission;
      }

      public final PermissionEditor.PermissionOpera add() {
         this.opera &= -3;
         this.opera |= 1;
         return this;
      }

      public final PermissionEditor.PermissionOpera remove() {
         this.opera &= -2;
         this.opera |= 2;
         return this;
      }

      final boolean isAdd() {
         return (this.opera & 1) == 1;
      }

      final boolean isRemove() {
         return (this.opera & 2) == 2;
      }
   }

   public static class EditorInfo {
      private List<PermissionEditor.PermissionOpera> editors = new ArrayList();

      public final PermissionEditor.EditorInfo with(PermissionEditor.PermissionOpera opera) {
         this.editors.add(opera);
         return this;
      }
   }
}
