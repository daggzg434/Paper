package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexAnnotationVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;

class FieldWriter extends DexFieldVisitor {
   public final ConstPool cp;
   private final ClassDataItem.EncodedField encodedField;

   public FieldWriter(ClassDataItem.EncodedField encodedField, ConstPool cp) {
      this.encodedField = encodedField;
      this.cp = cp;
   }

   public DexAnnotationVisitor visitAnnotation(String name, Visibility visibility) {
      AnnotationItem annItem = new AnnotationItem(this.cp.uniqType(name), visibility);
      AnnotationSetItem asi = this.encodedField.annotationSetItem;
      if (asi == null) {
         asi = new AnnotationSetItem();
         this.encodedField.annotationSetItem = asi;
      }

      asi.annotations.add(annItem);
      return new AnnotationWriter(annItem.annotation.elements, this.cp);
   }
}
