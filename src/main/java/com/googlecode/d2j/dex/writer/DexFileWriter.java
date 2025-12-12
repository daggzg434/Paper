package com.googlecode.d2j.dex.writer;

import com.googlecode.d2j.dex.writer.io.ByteBufferOut;
import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.AnnotationItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetItem;
import com.googlecode.d2j.dex.writer.item.AnnotationSetRefListItem;
import com.googlecode.d2j.dex.writer.item.AnnotationsDirectoryItem;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import com.googlecode.d2j.dex.writer.item.ClassDataItem;
import com.googlecode.d2j.dex.writer.item.ClassDefItem;
import com.googlecode.d2j.dex.writer.item.CodeItem;
import com.googlecode.d2j.dex.writer.item.ConstPool;
import com.googlecode.d2j.dex.writer.item.DebugInfoItem;
import com.googlecode.d2j.dex.writer.item.EncodedArrayItem;
import com.googlecode.d2j.dex.writer.item.FieldIdItem;
import com.googlecode.d2j.dex.writer.item.HeadItem;
import com.googlecode.d2j.dex.writer.item.MapListItem;
import com.googlecode.d2j.dex.writer.item.MethodIdItem;
import com.googlecode.d2j.dex.writer.item.ProtoIdItem;
import com.googlecode.d2j.dex.writer.item.SectionItem;
import com.googlecode.d2j.dex.writer.item.StringDataItem;
import com.googlecode.d2j.dex.writer.item.StringIdItem;
import com.googlecode.d2j.dex.writer.item.TypeIdItem;
import com.googlecode.d2j.dex.writer.item.TypeListItem;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Adler32;

public class DexFileWriter extends DexFileVisitor {
   private static final boolean DEBUG = false;
   MapListItem mapItem;
   HeadItem headItem;
   public ConstPool cp = new ConstPool();

   private static DataOut wrapDumpOut(final DataOut out0) {
      return (DataOut)Proxy.newProxyInstance(DexFileWriter.class.getClassLoader(), new Class[]{DataOut.class}, new InvocationHandler() {
         int indent = 0;

         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0].equals(String.class)) {
               StringBuilder sb = new StringBuilder();

               for(int ix = 0; ix < this.indent; ++ix) {
                  sb.append("  ");
               }

               sb.append(String.format("%05d ", out0.offset()));
               sb.append(method.getName() + " [");
               Object[] var13 = args;
               int var6 = args.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  Object arg = var13[var7];
                  if (!(arg instanceof byte[])) {
                     sb.append(arg).append(", ");
                  } else {
                     byte[] data = (byte[])((byte[])arg);
                     sb.append("0x[");
                     int start = 0;
                     int size = data.length;
                     if (args.length > 2) {
                        start = (Integer)args[2];
                        size = (Integer)args[3];
                     }

                     for(int i = 0; i < size; ++i) {
                        sb.append(String.format("%02x", data[start + i] & 255));
                        if (i != size - 1) {
                           sb.append(", ");
                        }
                     }

                     sb.append("], ");
                  }
               }

               sb.append("]");
               System.out.println(sb);
            }

            if (method.getName().equals("begin")) {
               ++this.indent;
            }

            if (method.getName().equals("end")) {
               --this.indent;
            }

            return method.invoke(out0, args);
         }
      });
   }

   void buildMapListItem() {
      if (this.cp.classDefs.isEmpty()) {
         System.err.println("WARN: no classdef on the dex");
      }

      if (this.cp.methods.isEmpty()) {
         this.cp.uniqMethod("Ljava/lang/Object;", "<init>", new String[0], "V");
      }

      if (this.cp.fields.isEmpty()) {
         this.cp.uniqField("Ljava/lang/System;", "out", "Ljava/io/PrintStream;");
      }

      if (this.cp.protos.isEmpty()) {
         this.cp.uniqProto(new String[0], "V");
      }

      if (this.cp.types.isEmpty()) {
         this.cp.uniqType("V");
      }

      if (this.cp.strings.isEmpty()) {
         this.cp.uniqString("V");
      }

      this.mapItem = new MapListItem();
      this.headItem = new HeadItem();
      SectionItem<HeadItem> headSection = new SectionItem(SectionItem.SectionType.TYPE_HEADER_ITEM);
      headSection.items.add(this.headItem);
      SectionItem<MapListItem> mapSection = new SectionItem(SectionItem.SectionType.TYPE_MAP_LIST);
      mapSection.items.add(this.mapItem);
      SectionItem<StringIdItem> stringIdSection = new SectionItem(SectionItem.SectionType.TYPE_STRING_ID_ITEM, this.cp.strings.values());
      SectionItem<TypeIdItem> typeIdSection = new SectionItem(SectionItem.SectionType.TYPE_TYPE_ID_ITEM, this.cp.types.values());
      SectionItem<ProtoIdItem> protoIdSection = new SectionItem(SectionItem.SectionType.TYPE_PROTO_ID_ITEM, this.cp.protos.values());
      SectionItem<FieldIdItem> fieldIdSection = new SectionItem(SectionItem.SectionType.TYPE_FIELD_ID_ITEM, this.cp.fields.values());
      SectionItem<MethodIdItem> methodIdSection = new SectionItem(SectionItem.SectionType.TYPE_METHOD_ID_ITEM, this.cp.methods.values());
      SectionItem<ClassDefItem> classDefSection = new SectionItem(SectionItem.SectionType.TYPE_CLASS_DEF_ITEM, this.cp.buildSortedClassDefItems());
      SectionItem<TypeListItem> typeListSection = new SectionItem(SectionItem.SectionType.TYPE_TYPE_LIST, this.cp.typeLists.values());
      SectionItem<AnnotationSetRefListItem> annotationSetRefListItemSection = new SectionItem(SectionItem.SectionType.TYPE_ANNOTATION_SET_REF_LIST, this.cp.annotationSetRefListItems.values());
      SectionItem<AnnotationSetItem> annotationSetSection = new SectionItem(SectionItem.SectionType.TYPE_ANNOTATION_SET_ITEM, this.cp.annotationSetItems.values());
      SectionItem<ClassDataItem> classDataItemSection = new SectionItem(SectionItem.SectionType.TYPE_CLASS_DATA_ITEM, this.cp.classDataItems);
      SectionItem<CodeItem> codeItemSection = new SectionItem(SectionItem.SectionType.TYPE_CODE_ITEM, this.cp.codeItems);
      SectionItem<StringDataItem> stringDataItemSection = new SectionItem(SectionItem.SectionType.TYPE_STRING_DATA_ITEM, this.cp.stringDatas);
      SectionItem<DebugInfoItem> debugInfoSection = new SectionItem(SectionItem.SectionType.TYPE_DEBUG_INFO_ITEM, this.cp.debugInfoItems);
      SectionItem<AnnotationItem> annotationItemSection = new SectionItem(SectionItem.SectionType.TYPE_ANNOTATION_ITEM, this.cp.annotationItems.values());
      SectionItem<EncodedArrayItem> encodedArrayItemSection = new SectionItem(SectionItem.SectionType.TYPE_ENCODED_ARRAY_ITEM, this.cp.encodedArrayItems);
      SectionItem<AnnotationsDirectoryItem> annotationsDirectoryItemSection = new SectionItem(SectionItem.SectionType.TYPE_ANNOTATIONS_DIRECTORY_ITEM, this.cp.annotationsDirectoryItems);
      this.headItem.mapSection = mapSection;
      this.headItem.stringIdSection = stringIdSection;
      this.headItem.typeIdSection = typeIdSection;
      this.headItem.protoIdSection = protoIdSection;
      this.headItem.fieldIdSection = fieldIdSection;
      this.headItem.methodIdSection = methodIdSection;
      this.headItem.classDefSection = classDefSection;
      List<SectionItem<?>> dataSectionItems = new ArrayList();
      dataSectionItems.add(mapSection);
      dataSectionItems.add(typeListSection);
      dataSectionItems.add(annotationSetRefListItemSection);
      dataSectionItems.add(annotationSetSection);
      dataSectionItems.add(codeItemSection);
      dataSectionItems.add(classDataItemSection);
      dataSectionItems.add(stringDataItemSection);
      dataSectionItems.add(debugInfoSection);
      dataSectionItems.add(annotationItemSection);
      dataSectionItems.add(encodedArrayItemSection);
      dataSectionItems.add(annotationsDirectoryItemSection);
      List<SectionItem<?>> items = this.mapItem.items;
      items.add(headSection);
      items.add(stringIdSection);
      items.add(typeIdSection);
      items.add(protoIdSection);
      items.add(fieldIdSection);
      items.add(methodIdSection);
      items.add(classDefSection);
      items.addAll(dataSectionItems);
      this.cp.clean();
      this.cp = null;
   }

   public byte[] toByteArray() {
      this.buildMapListItem();
      int size = this.place();
      ByteBuffer buffer = ByteBuffer.allocate(size);
      DataOut out = new ByteBufferOut(buffer);
      this.write(out);
      if (size != buffer.position()) {
         throw new RuntimeException("generated different file size, planned " + size + ", but is " + buffer.position());
      } else {
         updateChecksum(buffer, size);
         return buffer.array();
      }
   }

   public static void updateChecksum(ByteBuffer buffer, int size) {
      byte[] data = buffer.array();

      MessageDigest digest;
      try {
         digest = MessageDigest.getInstance("SHA-1");
      } catch (NoSuchAlgorithmException var7) {
         throw new AssertionError();
      }

      digest.update(data, 32, size - 32);
      byte[] sha1 = digest.digest();
      System.arraycopy(sha1, 0, data, 12, sha1.length);
      Adler32 adler32 = new Adler32();
      adler32.update(data, 12, size - 12);
      int v = (int)adler32.getValue();
      buffer.position(8);
      buffer.putInt(v);
   }

   private void write(DataOut out) {
      List<SectionItem<?>> list = new ArrayList(this.mapItem.items);
      this.mapItem = null;

      for(int i = 0; i < list.size(); ++i) {
         SectionItem<?> section = (SectionItem)list.get(i);
         list.set(i, (Object)null);
         BaseItem.addPadding(out, out.offset(), section.sectionType.alignment);
         if (out.offset() != section.offset) {
            throw new RuntimeException(section.sectionType + " start with different position, planned:" + section.offset + ", but is:" + out.offset());
         }

         section.write(out);
      }

   }

   private int place() {
      this.mapItem.cleanZeroSizeEntry();
      int offset = 0;

      SectionItem section;
      for(Iterator var2 = this.mapItem.items.iterator(); var2.hasNext(); offset = section.place(offset)) {
         section = (SectionItem)var2.next();
         offset = BaseItem.padding(offset, section.sectionType.alignment);
         section.offset = offset;
      }

      this.headItem.fileSize = offset;
      this.headItem = null;
      return offset;
   }

   public DexClassVisitor visit(int accessFlag, String name, String superClass, String[] itfClass) {
      ClassDefItem defItem = this.cp.putClassDefItem(accessFlag, name, superClass, itfClass);
      return new ClassWriter(defItem, this.cp);
   }
}
