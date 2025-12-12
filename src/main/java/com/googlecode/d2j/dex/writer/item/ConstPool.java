package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.dex.writer.DexWriteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

public class ConstPool {
   public List<EncodedArrayItem> encodedArrayItems = new ArrayList();
   public Map<AnnotationSetRefListItem, AnnotationSetRefListItem> annotationSetRefListItems = new HashMap();
   public List<CodeItem> codeItems = new ArrayList();
   public List<ClassDataItem> classDataItems = new ArrayList();
   public List<DebugInfoItem> debugInfoItems = new ArrayList();
   public Map<AnnotationItem, AnnotationItem> annotationItems = new HashMap();
   public List<AnnotationsDirectoryItem> annotationsDirectoryItems = new ArrayList();
   public Map<AnnotationSetItem, AnnotationSetItem> annotationSetItems = new HashMap();
   public Map<FieldIdItem, FieldIdItem> fields = new TreeMap();
   public Map<MethodIdItem, MethodIdItem> methods = new TreeMap();
   public Map<ProtoIdItem, ProtoIdItem> protos = new TreeMap();
   public List<StringDataItem> stringDatas = new ArrayList(100);
   public Map<String, StringIdItem> strings = new TreeMap();
   public Map<TypeListItem, TypeListItem> typeLists = new TreeMap();
   public Map<String, TypeIdItem> types = new TreeMap();
   public Map<TypeIdItem, ClassDefItem> classDefs = new HashMap();
   private static final TypeListItem ZERO_SIZE_TYPE_LIST;

   public Object wrapEncodedItem(Object value) {
      if (value instanceof DexType) {
         value = this.uniqType(((DexType)value).desc);
      } else if (value instanceof Field) {
         value = this.uniqField((Field)value);
      } else if (value instanceof String) {
         value = this.uniqString((String)value);
      } else if (value instanceof Method) {
         value = this.uniqMethod((Method)value);
      }

      return value;
   }

   public void clean() {
      this.encodedArrayItems.clear();
      this.annotationSetRefListItems.clear();
      this.codeItems.clear();
      this.classDataItems.clear();
      this.debugInfoItems.clear();
      this.annotationItems.clear();
      this.annotationsDirectoryItems.clear();
      this.annotationSetItems.clear();
      this.fields.clear();
      this.methods.clear();
      this.protos.clear();
      this.stringDatas.clear();
      this.typeLists.clear();
      this.types.clear();
      this.classDefs.clear();
   }

   private String buildShorty(String ret, String[] types2) {
      StringBuilder sb = new StringBuilder();
      if (ret.length() == 1) {
         sb.append(ret);
      } else {
         sb.append("L");
      }

      String[] var4 = types2;
      int var5 = types2.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String s = var4[var6];
         if (s.length() == 1) {
            sb.append(s);
         } else {
            sb.append("L");
         }
      }

      return sb.toString();
   }

   ConstPool.PE iterateParent(ClassDefItem p) {
      List<TypeIdItem> list = new ArrayList(6);
      list.add(p.superclazz);
      if (p.interfaces != null) {
         list.addAll(p.interfaces.items);
      }

      return new ConstPool.PE(p, list.iterator());
   }

   public void addDebugInfoItem(DebugInfoItem debugInfoItem) {
      this.debugInfoItems.add(debugInfoItem);
   }

   public List<ClassDefItem> buildSortedClassDefItems() {
      List<ClassDefItem> added = new ArrayList();
      Stack<ConstPool.PE> stack1 = new Stack();
      Set<ClassDefItem> children = new HashSet();
      Iterator var4 = this.classDefs.values().iterator();

      while(true) {
         ClassDefItem c;
         do {
            if (!var4.hasNext()) {
               return added;
            }

            c = (ClassDefItem)var4.next();
         } while(added.contains(c));

         children.add(c);
         stack1.push(this.iterateParent(c));

         while(!stack1.empty()) {
            ConstPool.PE e = (ConstPool.PE)stack1.peek();
            boolean canPop = true;

            while(e.it.hasNext()) {
               TypeIdItem tid = (TypeIdItem)e.it.next();
               if (tid != null) {
                  ClassDefItem superDef = (ClassDefItem)this.classDefs.get(tid);
                  if (superDef != null && !added.contains(superDef)) {
                     if (!children.contains(superDef)) {
                        canPop = false;
                        children.add(superDef);
                        stack1.push(this.iterateParent(superDef));
                        break;
                     }

                     System.err.println("WARN: dep-loop " + e.owner.clazz.descriptor.stringData.string + " -> " + superDef.clazz.descriptor.stringData.string);
                  }
               }
            }

            if (canPop) {
               stack1.pop();
               added.add(e.owner);
               children.remove(e.owner);
            }
         }

         children.clear();
      }
   }

   public AnnotationsDirectoryItem putAnnotationDirectoryItem() {
      AnnotationsDirectoryItem aDirectoryItem = new AnnotationsDirectoryItem();
      this.annotationsDirectoryItems.add(aDirectoryItem);
      return aDirectoryItem;
   }

   public AnnotationItem uniqAnnotationItem(AnnotationItem key) {
      AnnotationItem v = (AnnotationItem)this.annotationItems.get(key);
      if (v == null) {
         this.annotationItems.put(key, key);
         return key;
      } else {
         return v;
      }
   }

   public ClassDefItem putClassDefItem(int accessFlag, String name, String superClass, String[] itfClass) {
      TypeIdItem type = this.uniqType(name);
      if (this.classDefs.containsKey(type)) {
         throw new DexWriteException("dup clz: " + name);
      } else {
         ClassDefItem classDefItem = new ClassDefItem();
         classDefItem.accessFlags = accessFlag;
         classDefItem.clazz = type;
         if (superClass != null) {
            classDefItem.superclazz = this.uniqType(superClass);
         }

         if (itfClass != null && itfClass.length > 0) {
            classDefItem.interfaces = this.putTypeList(Arrays.asList(itfClass));
         }

         this.classDefs.put(type, classDefItem);
         return classDefItem;
      }
   }

   public FieldIdItem uniqField(Field field) {
      return this.uniqField(field.getOwner(), field.getName(), field.getType());
   }

   public FieldIdItem uniqField(String owner, String name, String type) {
      FieldIdItem key = new FieldIdItem(this.uniqType(owner), this.uniqString(name), this.uniqType(type));
      FieldIdItem item = (FieldIdItem)this.fields.get(key);
      if (item != null) {
         return item;
      } else {
         this.fields.put(key, key);
         return key;
      }
   }

   public MethodIdItem uniqMethod(Method method) {
      MethodIdItem key = new MethodIdItem(this.uniqType(method.getOwner()), this.uniqString(method.getName()), this.uniqProto(method));
      return this.uniqMethod(key);
   }

   public MethodIdItem uniqMethod(String owner, String name, String[] parms, String ret) {
      MethodIdItem key = new MethodIdItem(this.uniqType(owner), this.uniqString(name), this.uniqProto(parms, ret));
      return this.uniqMethod(key);
   }

   public MethodIdItem uniqMethod(MethodIdItem key) {
      MethodIdItem item = (MethodIdItem)this.methods.get(key);
      if (item != null) {
         return item;
      } else {
         this.methods.put(key, key);
         return key;
      }
   }

   private ProtoIdItem uniqProto(Method method) {
      return this.uniqProto(method.getParameterTypes(), method.getReturnType());
   }

   public ProtoIdItem uniqProto(String[] types, String retDesc) {
      TypeIdItem ret = this.uniqType(retDesc);
      StringIdItem shorty = this.uniqString(this.buildShorty(retDesc, types));
      TypeListItem params = this.putTypeList(types);
      ProtoIdItem key = new ProtoIdItem(params, ret, shorty);
      ProtoIdItem item = (ProtoIdItem)this.protos.get(key);
      if (item != null) {
         return item;
      } else {
         this.protos.put(key, key);
         return key;
      }
   }

   public StringIdItem uniqString(String data) {
      StringIdItem item = (StringIdItem)this.strings.get(data);
      if (item != null) {
         return item;
      } else {
         StringDataItem sd = new StringDataItem(data);
         this.stringDatas.add(sd);
         item = new StringIdItem(sd);
         this.strings.put(data, item);
         return item;
      }
   }

   public TypeIdItem uniqType(String type) {
      TypeIdItem item = (TypeIdItem)this.types.get(type);
      if (item != null) {
         return item;
      } else {
         item = new TypeIdItem(this.uniqString(type));
         this.types.put(type, item);
         return item;
      }
   }

   private TypeListItem putTypeList(String... subList) {
      if (subList.length == 0) {
         return ZERO_SIZE_TYPE_LIST;
      } else {
         List<TypeIdItem> idItems = new ArrayList(subList.length);
         String[] var3 = subList;
         int var4 = subList.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String s = var3[var5];
            idItems.add(this.uniqType(s));
         }

         TypeListItem key = new TypeListItem(idItems);
         TypeListItem item = (TypeListItem)this.typeLists.get(key);
         if (item != null) {
            return item;
         } else {
            this.typeLists.put(key, key);
            return key;
         }
      }
   }

   private TypeListItem putTypeList(List<String> subList) {
      if (subList.size() == 0) {
         return ZERO_SIZE_TYPE_LIST;
      } else {
         List<TypeIdItem> idItems = new ArrayList(subList.size());
         Iterator var3 = subList.iterator();

         while(var3.hasNext()) {
            String s = (String)var3.next();
            idItems.add(this.uniqType(s));
         }

         TypeListItem key = new TypeListItem(idItems);
         TypeListItem item = (TypeListItem)this.typeLists.get(key);
         if (item != null) {
            return item;
         } else {
            this.typeLists.put(key, key);
            return key;
         }
      }
   }

   public ClassDataItem addClassDataItem(ClassDataItem dataItem) {
      this.classDataItems.add(dataItem);
      return dataItem;
   }

   public EncodedArrayItem putEnCodedArrayItem() {
      EncodedArrayItem arrayItem = new EncodedArrayItem();
      this.encodedArrayItems.add(arrayItem);
      return arrayItem;
   }

   public AnnotationSetItem uniqAnnotationSetItem(AnnotationSetItem key) {
      List<AnnotationItem> copy = new ArrayList(key.annotations);
      key.annotations.clear();
      Iterator var3 = copy.iterator();

      while(var3.hasNext()) {
         AnnotationItem annotationItem = (AnnotationItem)var3.next();
         key.annotations.add(this.uniqAnnotationItem(annotationItem));
      }

      AnnotationSetItem v = (AnnotationSetItem)this.annotationSetItems.get(key);
      if (v != null) {
         return v;
      } else {
         this.annotationSetItems.put(key, key);
         return key;
      }
   }

   public AnnotationSetRefListItem uniqAnnotationSetRefListItem(AnnotationSetRefListItem key) {
      for(int i = 0; i < key.annotationSets.length; ++i) {
         AnnotationSetItem anno = key.annotationSets[i];
         if (anno != null) {
            key.annotationSets[i] = this.uniqAnnotationSetItem(anno);
         }
      }

      AnnotationSetRefListItem v = (AnnotationSetRefListItem)this.annotationSetRefListItems.get(key);
      if (v == null) {
         this.annotationSetRefListItems.put(key, key);
         return key;
      } else {
         return v;
      }
   }

   public void addCodeItem(CodeItem code) {
      this.codeItems.add(code);
   }

   static {
      ZERO_SIZE_TYPE_LIST = new TypeListItem(Collections.EMPTY_LIST);
      ZERO_SIZE_TYPE_LIST.offset = 0;
   }

   static class PE {
      final ClassDefItem owner;
      final Iterator<TypeIdItem> it;

      PE(ClassDefItem owner, Iterator<TypeIdItem> it) {
         this.owner = owner;
         this.it = it;
      }
   }
}
