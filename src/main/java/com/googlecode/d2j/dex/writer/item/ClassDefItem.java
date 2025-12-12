package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.ev.EncodedArray;
import com.googlecode.d2j.dex.writer.ev.EncodedValue;
import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ClassDefItem extends BaseItem {
   public TypeIdItem clazz;
   public int accessFlags;
   public TypeIdItem superclazz;
   public TypeListItem interfaces;
   public StringIdItem sourceFile;
   public ClassDataItem classData;
   public AnnotationSetItem classAnnotations;
   private AnnotationsDirectoryItem annotations;
   private EncodedArrayItem staticValues;

   public int place(int offset) {
      return offset + 32;
   }

   public void prepare(ConstPool cp) {
      if (this.classData != null) {
         this.classData.prepare(cp);
      }

      this.preparteAnnotationsDirectoryItem(cp);
      this.prepareEncodedArrayItem(cp);
   }

   private void prepareEncodedArrayItem(ConstPool cp) {
      if (this.classData != null) {
         List<ClassDataItem.EncodedField> fs = this.classData.staticFields;
         int count = -1;

         for(int i = 0; i < fs.size(); ++i) {
            ClassDataItem.EncodedField f = (ClassDataItem.EncodedField)fs.get(i);
            EncodedValue ev = f.staticValue;
            if (ev != null && !ev.isDefaultValueForType()) {
               count = i;
            }
         }

         if (count >= 0) {
            EncodedArrayItem encodedArrayItem = cp.putEnCodedArrayItem();
            EncodedArray array = encodedArrayItem.value;

            for(int i = 0; i <= count; ++i) {
               ClassDataItem.EncodedField f = (ClassDataItem.EncodedField)fs.get(i);
               EncodedValue ev = f.staticValue;
               if (ev == null) {
                  array.values.add(EncodedValue.defaultValueForType(f.field.getTypeString()));
               } else {
                  array.values.add(ev);
               }
            }

            this.staticValues = encodedArrayItem;
         }

      }
   }

   private void preparteAnnotationsDirectoryItem(ConstPool cp) {
      Map<FieldIdItem, AnnotationSetItem> fieldAnnotations = new TreeMap();
      Map<MethodIdItem, AnnotationSetItem> methodAnnotations = new TreeMap();
      Map<MethodIdItem, AnnotationSetRefListItem> parameterAnnotations = new TreeMap();
      if (this.classData != null) {
         this.collectField(fieldAnnotations, this.classData.staticFields, cp);
         this.collectField(fieldAnnotations, this.classData.instanceFields, cp);
         this.collectMethod(methodAnnotations, parameterAnnotations, this.classData.directMethods, cp);
         this.collectMethod(methodAnnotations, parameterAnnotations, this.classData.virtualMethods, cp);
      }

      if (this.classAnnotations != null || fieldAnnotations.size() > 0 || methodAnnotations.size() > 0 || parameterAnnotations.size() > 0) {
         AnnotationsDirectoryItem annotationsDirectoryItem = cp.putAnnotationDirectoryItem();
         this.annotations = annotationsDirectoryItem;
         if (this.classAnnotations != null) {
            annotationsDirectoryItem.classAnnotations = cp.uniqAnnotationSetItem(this.classAnnotations);
         }

         if (fieldAnnotations.size() > 0) {
            annotationsDirectoryItem.fieldAnnotations = fieldAnnotations;
         }

         if (methodAnnotations.size() > 0) {
            annotationsDirectoryItem.methodAnnotations = methodAnnotations;
         }

         if (parameterAnnotations.size() > 0) {
            annotationsDirectoryItem.parameterAnnotations = parameterAnnotations;
         }
      }

   }

   private void collectMethod(Map<MethodIdItem, AnnotationSetItem> methodAnnotations, Map<MethodIdItem, AnnotationSetRefListItem> parameterAnnotations, List<ClassDataItem.EncodedMethod> ms, ConstPool cp) {
      Iterator var5 = ms.iterator();

      while(var5.hasNext()) {
         ClassDataItem.EncodedMethod m = (ClassDataItem.EncodedMethod)var5.next();
         if (m.annotationSetItem != null) {
            methodAnnotations.put(m.method, cp.uniqAnnotationSetItem(m.annotationSetItem));
         }

         if (m.parameterAnnotation != null) {
            parameterAnnotations.put(m.method, cp.uniqAnnotationSetRefListItem(m.parameterAnnotation));
         }
      }

   }

   private void collectField(Map<FieldIdItem, AnnotationSetItem> fieldAnnotations, List<ClassDataItem.EncodedField> fs, ConstPool cp) {
      Iterator var4 = fs.iterator();

      while(var4.hasNext()) {
         ClassDataItem.EncodedField f = (ClassDataItem.EncodedField)var4.next();
         if (f.annotationSetItem != null) {
            fieldAnnotations.put(f.field, cp.uniqAnnotationSetItem(f.annotationSetItem));
         }
      }

   }

   public void write(DataOut out) {
      out.uint("class_idx", this.clazz.index);
      out.uint("access_flags", this.accessFlags);
      out.uint("superclass_idx", this.superclazz == null ? -1 : this.superclazz.index);
      out.uint("interfaces_off", this.interfaces != null && this.interfaces.items.size() != 0 ? this.interfaces.offset : 0);
      out.uint("source_file_idx", this.sourceFile == null ? -1 : this.sourceFile.index);
      out.uint("annotations_off", this.annotations == null ? 0 : this.annotations.offset);
      out.uint("class_data_off", this.classData == null ? 0 : this.classData.offset);
      out.uint("static_values_off", this.staticValues == null ? 0 : this.staticValues.offset);
   }
}
