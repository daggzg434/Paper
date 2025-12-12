package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.DexConstants;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.ClassDefItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

class ClassWriter extends DexClassVisitor implements DexConstants {
   public final ConstPool cp;
   public ClassDefItem defItem;
   ClassDataItem dataItem = new ClassDataItem();

   public ClassWriter(ClassDefItem defItem, ConstPool cp) {
      this.defItem = defItem;
      this.cp = cp;
   }

   public AnnotationWriter visitAnnotation(String type, Visibility visibility) {
      AnnotationItem annItem = new AnnotationItem(this.cp.uniqType(type), visibility);
      AnnotationSetItem asi = this.defItem.classAnnotations;
      if (asi == null) {
         asi = new AnnotationSetItem();
         this.defItem.classAnnotations = asi;
      }

      asi.annotations.add(annItem);
      return new AnnotationWriter(annItem.annotation.elements, this.cp);
   }

   public void visitEnd() {
      if (this.dataItem != null && this.dataItem.getMemberSize() > 0) {
         this.cp.addClassDataItem(this.dataItem);
         this.defItem.classData = this.dataItem;
      }

      this.defItem.prepare(this.cp);
   }

   public DexFieldVisitor visitField(int accessFlags, Field field, Object value) {
      ClassDataItem.EncodedField encodedField = new ClassDataItem.EncodedField();
      encodedField.accessFlags = accessFlags;
      encodedField.field = this.cp.uniqField(field);
      if (value != null) {
         encodedField.staticValue = EncodedValue.wrap(this.cp.wrapEncodedItem(value));
      }

      if (0 != (8 & accessFlags)) {
         this.dataItem.staticFields.add(encodedField);
      } else {
         this.dataItem.instanceFields.add(encodedField);
      }

      return new FieldWriter(encodedField, this.cp);
   }

   public DexMethodVisitor visitMethod(int accessFlags, Method method) {
      ClassDataItem.EncodedMethod encodedMethod = new ClassDataItem.EncodedMethod();
      encodedMethod.accessFlags = accessFlags;
      encodedMethod.method = this.cp.uniqMethod(method);
      if (0 != (accessFlags & 65546)) {
         this.dataItem.directMethods.add(encodedMethod);
      } else {
         this.dataItem.virtualMethods.add(encodedMethod);
      }

      return new MethodWriter(encodedMethod, method, 0 != (accessFlags & 8), this.cp);
   }

   public void visitSource(String file) {
      this.defItem.sourceFile = this.cp.uniqString(file);
   }
}
