package com.googlecode.d2j.reader;

import com.googlecode.d2j.DexException;
import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.DexType;
import com.googlecode.d2j.Field;
import com.googlecode.d2j.Method;
import com.googlecode.d2j.MethodHandle;
import com.googlecode.d2j.Proto;
import com.googlecode.d2j.Visibility;
import com.googlecode.d2j.node.DexAnnotationNode;
import com.googlecode.d2j.util.Mutf8;
import com.googlecode.d2j.visitors.DexAnnotationAble;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexCodeVisitor;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import com.googlecode.d2j.visitors.DexFieldVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

public class DexFileReader implements BaseDexFileReader {
   public static final int SKIP_DEBUG = 1;
   public static final int SKIP_CODE = 4;
   public static final int SKIP_ANNOTATION = 8;
   public static final int SKIP_FIELD_CONSTANT = 16;
   public static final int IGNORE_READ_EXCEPTION = 32;
   public static final int KEEP_ALL_METHODS = 64;
   public static final int KEEP_CLINIT = 128;
   public static final int SKIP_EXCEPTION = 256;
   static final int DBG_END_SEQUENCE = 0;
   static final int DBG_ADVANCE_PC = 1;
   static final int DBG_ADVANCE_LINE = 2;
   static final int DBG_START_LOCAL = 3;
   static final int DBG_START_LOCAL_EXTENDED = 4;
   static final int DBG_END_LOCAL = 5;
   static final int DBG_RESTART_LOCAL = 6;
   static final int DBG_SET_PROLOGUE_END = 7;
   static final int DBG_SET_EPILOGUE_BEGIN = 8;
   static final int DBG_SET_FILE = 9;
   static final int DBG_FIRST_SPECIAL = 10;
   static final int DBG_LINE_BASE = -4;
   static final int DBG_LINE_RANGE = 15;
   private static final int ENDIAN_CONSTANT = 305419896;
   private static final int VALUE_BYTE = 0;
   private static final int VALUE_SHORT = 2;
   private static final int VALUE_CHAR = 3;
   private static final int VALUE_INT = 4;
   private static final int VALUE_LONG = 6;
   private static final int VALUE_FLOAT = 16;
   private static final int VALUE_DOUBLE = 17;
   private static final int VALUE_METHOD_TYPE = 21;
   private static final int VALUE_METHOD_HANDLE = 22;
   private static final int VALUE_STRING = 23;
   private static final int VALUE_TYPE = 24;
   private static final int VALUE_FIELD = 25;
   private static final int VALUE_METHOD = 26;
   private static final int VALUE_ENUM = 27;
   private static final int VALUE_ARRAY = 28;
   private static final int VALUE_ANNOTATION = 29;
   private static final int VALUE_NULL = 30;
   private static final int VALUE_BOOLEAN = 31;
   private static final int TYPE_CALL_SITE_ID_ITEM = 7;
   private static final int TYPE_METHOD_HANDLE_ITEM = 8;
   final ByteBuffer annotationSetRefListIn;
   final ByteBuffer annotationsDirectoryItemIn;
   final ByteBuffer annotationSetItemIn;
   final ByteBuffer annotationItemIn;
   final ByteBuffer classDataIn;
   final ByteBuffer codeItemIn;
   final ByteBuffer encodedArrayItemIn;
   final ByteBuffer stringIdIn;
   final ByteBuffer typeIdIn;
   final ByteBuffer protoIdIn;
   final ByteBuffer fieldIdIn;
   final ByteBuffer methoIdIn;
   final ByteBuffer classDefIn;
   final ByteBuffer typeListIn;
   final ByteBuffer stringDataIn;
   final ByteBuffer debugInfoIn;
   final ByteBuffer callSiteIdIn;
   final ByteBuffer methodHandleIdIn;
   final int string_ids_size;
   final int type_ids_size;
   final int proto_ids_size;
   final int field_ids_size;
   final int method_ids_size;
   private final int class_defs_size;
   final int call_site_ids_size;
   final int method_handle_ids_size;
   final int dex_version;

   public DexFileReader(ByteBuffer in) {
      in.position(0);
      in = in.asReadOnlyBuffer().order(ByteOrder.BIG_ENDIAN);
      int magic = in.getInt() & -256;
      int MAGIC_DEX = 1684371456;
      int MAGIC_ODEX = 1684371712;
      if (magic != 1684371456) {
         if (magic == 1684371712) {
            throw new DexException("Not support odex");
         } else {
            throw new DexException("not support magic.");
         }
      } else {
         int version = in.getInt() >> 8;
         if (version >= 0 && version >= 3158837) {
            this.dex_version = version;
            in.order(ByteOrder.LITTLE_ENDIAN);
            skip(in, 32);
            int endian_tag = in.getInt();
            if (endian_tag != 305419896) {
               throw new DexException("not support endian_tag");
            } else {
               skip(in, 8);
               int map_off = in.getInt();
               this.string_ids_size = in.getInt();
               int string_ids_off = in.getInt();
               this.type_ids_size = in.getInt();
               int type_ids_off = in.getInt();
               this.proto_ids_size = in.getInt();
               int proto_ids_off = in.getInt();
               this.field_ids_size = in.getInt();
               int field_ids_off = in.getInt();
               this.method_ids_size = in.getInt();
               int method_ids_off = in.getInt();
               this.class_defs_size = in.getInt();
               int class_defs_off = in.getInt();
               int call_site_ids_off = 0;
               int call_site_ids_size = 0;
               int method_handle_ids_off = 0;
               int method_handle_ids_size = 0;
               if (this.dex_version > 3158839) {
                  in.position(map_off);
                  int size = in.getInt();

                  for(int i = 0; i < size; ++i) {
                     int type = in.getShort() & '\uffff';
                     in.getShort();
                     int item_size = in.getInt();
                     int item_offset = in.getInt();
                     switch(type) {
                     case 7:
                        call_site_ids_off = item_offset;
                        call_site_ids_size = item_size;
                        break;
                     case 8:
                        method_handle_ids_off = item_offset;
                        method_handle_ids_size = item_size;
                     }
                  }
               }

               this.call_site_ids_size = call_site_ids_size;
               this.method_handle_ids_size = method_handle_ids_size;
               this.stringIdIn = slice(in, string_ids_off, this.string_ids_size * 4);
               this.typeIdIn = slice(in, type_ids_off, this.type_ids_size * 4);
               this.protoIdIn = slice(in, proto_ids_off, this.proto_ids_size * 12);
               this.fieldIdIn = slice(in, field_ids_off, this.field_ids_size * 8);
               this.methoIdIn = slice(in, method_ids_off, this.method_ids_size * 8);
               this.classDefIn = slice(in, class_defs_off, this.class_defs_size * 32);
               this.callSiteIdIn = slice(in, call_site_ids_off, call_site_ids_size * 4);
               this.methodHandleIdIn = slice(in, method_handle_ids_off, method_handle_ids_size * 8);
               in.position(0);
               this.annotationsDirectoryItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.annotationSetItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.annotationItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.annotationSetRefListIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.classDataIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.codeItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.stringDataIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.encodedArrayItemIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.typeListIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
               this.debugInfoIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);
            }
         } else {
            throw new DexException("not support version.");
         }
      }
   }

   public DexFileReader(byte[] data) {
      this(ByteBuffer.wrap(data));
   }

   public DexFileReader(InputStream is) throws IOException {
      this(toByteArray(is));
   }

   private static int readStringIndex(ByteBuffer bs) {
      int offsetIndex = readULeb128i(bs);
      return offsetIndex - 1;
   }

   private static byte[] toByteArray(InputStream is) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buff = new byte[1024];

      for(int c = is.read(buff); c > 0; c = is.read(buff)) {
         out.write(buff, 0, c);
      }

      return out.toByteArray();
   }

   private static ByteBuffer slice(ByteBuffer in, int offset, int length) {
      in.position(offset);
      ByteBuffer b = in.slice();
      b.limit(length);
      b.order(ByteOrder.LITTLE_ENDIAN);
      return b;
   }

   private static void skip(ByteBuffer in, int bytes) {
      in.position(in.position() + bytes);
   }

   public static void niceExceptionMessage(Throwable t, int deep) {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < deep + 1; ++i) {
         sb.append(".");
      }

      sb.append(' ');
      if (t instanceof DexException) {
         sb.append(t.getMessage());
         System.err.println(sb.toString());
         if (t.getCause() != null) {
            niceExceptionMessage(t.getCause(), deep + 1);
         }
      } else if (t != null) {
         System.err.println(sb.append("ROOT cause:").toString());
         t.printStackTrace(System.err);
      }

   }

   private static long readIntBits(ByteBuffer in, int before) {
      int length = (before >> 5 & 7) + 1;
      long value = 0L;

      int shift;
      for(shift = 0; shift < length; ++shift) {
         value |= (long)(255 & in.get()) << shift * 8;
      }

      shift = (8 - length) * 8;
      return value << shift >> shift;
   }

   private static long readUIntBits(ByteBuffer in, int before) {
      int length = (before >> 5 & 7) + 1;
      long value = 0L;

      for(int j = 0; j < length; ++j) {
         value |= (long)(255 & in.get()) << j * 8;
      }

      return value;
   }

   private static long readFloatBits(ByteBuffer in, int before) {
      int bytes = (before >> 5 & 7) + 1;
      long result = 0L;

      for(int i = 0; i < bytes; ++i) {
         result |= (long)(255 & in.get()) << i * 8;
      }

      result <<= (8 - bytes) * 8;
      return result;
   }

   static int sshort(byte[] data, int offset) {
      return data[offset + 1] << 8 | 255 & data[offset];
   }

   static int ushort(byte[] data, int offset) {
      return (255 & data[offset + 1]) << 8 | 255 & data[offset];
   }

   static int sint(byte[] data, int offset) {
      return data[offset + 3] << 24 | (255 & data[offset + 2]) << 16 | (255 & data[offset + 1]) << 8 | 255 & data[offset];
   }

   static int uint(byte[] data, int offset) {
      return sint(data, offset);
   }

   static void WARN(String fmt, Object... args) {
      System.err.println(String.format(fmt, args));
   }

   static int ubyte(byte[] insns, int offset) {
      return 255 & insns[offset];
   }

   static int sbyte(byte[] insns, int offset) {
      return insns[offset];
   }

   private static void order(Map<Integer, DexLabel> labelsMap, int offset) {
      if (!labelsMap.containsKey(offset)) {
         labelsMap.put(offset, new DexLabel(offset));
      }

   }

   public static int readULeb128i(ByteBuffer in) {
      int value = 0;
      int count = 0;

      byte b;
      for(b = in.get(); (b & 128) != 0; b = in.get()) {
         value |= (b & 127) << count;
         count += 7;
      }

      value |= (b & 127) << count;
      return value;
   }

   public static int readLeb128i(ByteBuffer in) {
      int bitpos = 0;
      int vln = 0;

      byte inp;
      do {
         inp = in.get();
         vln |= (inp & 127) << bitpos;
         bitpos += 7;
      } while((inp & 128) != 0);

      if ((1L << bitpos - 1 & (long)vln) != 0L) {
         vln = (int)((long)vln - (1L << bitpos));
      }

      return vln;
   }

   private static void DEBUG_DEBUG(String fmt, Object... args) {
   }

   private void read_debug_info(int offset, int regSize, boolean isStatic, Method method, Map<Integer, DexLabel> labelMap, DexDebugVisitor dcv) {
      ByteBuffer in = this.debugInfoIn;
      in.position(offset);
      int address = 0;
      int line = readULeb128i(in);
      int szParams = readULeb128i(in);
      DexFileReader.LocalEntry[] lastEntryForReg = new DexFileReader.LocalEntry[regSize];
      int argsSize = 0;
      String[] var13 = method.getParameterTypes();
      int var14 = var13.length;

      int opcode;
      String paramType;
      for(opcode = 0; opcode < var14; ++opcode) {
         paramType = var13[opcode];
         if (!paramType.equals("J") && !paramType.equals("D")) {
            ++argsSize;
         } else {
            argsSize += 2;
         }
      }

      int curReg = regSize - argsSize;
      if (!isStatic) {
         DexFileReader.LocalEntry thisEntry = new DexFileReader.LocalEntry("this", method.getOwner(), (String)null);
         lastEntryForReg[curReg - 1] = thisEntry;
         DEBUG_DEBUG("v%d :%s, %s", curReg - 1, "this", method.getOwner());
      }

      String[] params = method.getParameterTypes();

      DexFileReader.LocalEntry le;
      int typeIdx;
      String name;
      for(opcode = 0; opcode < szParams; ++opcode) {
         paramType = params[opcode];
         typeIdx = readStringIndex(in);
         name = this.getString(typeIdx);
         le = new DexFileReader.LocalEntry(name, paramType);
         lastEntryForReg[curReg] = le;
         if (name != null) {
            dcv.visitParameterName(opcode, name);
         }

         DEBUG_DEBUG("v%d :%s, %s", curReg, name, paramType);
         ++curReg;
         if (paramType.equals("J") || paramType.equals("D")) {
            ++curReg;
         }
      }

      while(true) {
         opcode = in.get() & 255;
         String name;
         int reg;
         int nameIdx;
         switch(opcode) {
         case 0:
            return;
         case 1:
            address += readULeb128i(in);
            break;
         case 2:
            line += readLeb128i(in);
            break;
         case 3:
            reg = readULeb128i(in);
            nameIdx = readStringIndex(in);
            typeIdx = readStringIndex(in);
            name = this.getString(nameIdx);
            name = this.getType(typeIdx);
            DEBUG_DEBUG("Start: v%d :%s, %s", reg, name, name);
            DexFileReader.LocalEntry le = new DexFileReader.LocalEntry(name, name);
            lastEntryForReg[reg] = le;
            order(labelMap, address);
            dcv.visitStartLocal(reg, (DexLabel)labelMap.get(address), name, name, (String)null);
            break;
         case 4:
            reg = readULeb128i(in);
            nameIdx = readStringIndex(in);
            typeIdx = readStringIndex(in);
            int sigIdx = readStringIndex(in);
            name = this.getString(nameIdx);
            String type = this.getType(typeIdx);
            String signature = this.getString(sigIdx);
            DEBUG_DEBUG("Start: v%d :%s, %s // %s", reg, name, type, signature);
            DexFileReader.LocalEntry le = new DexFileReader.LocalEntry(name, type, signature);
            order(labelMap, address);
            dcv.visitStartLocal(reg, (DexLabel)labelMap.get(address), name, type, signature);
            lastEntryForReg[reg] = le;
            break;
         case 5:
            reg = readULeb128i(in);
            le = lastEntryForReg[reg];
            if (le == null) {
               throw new RuntimeException("Encountered RESTART_LOCAL on new v" + reg);
            }

            if (le.signature == null) {
               DEBUG_DEBUG("End: v%d :%s, %s", reg, le.name, le.type);
            } else {
               DEBUG_DEBUG("End: v%d :%s, %s // %s", reg, le.name, le.type, le.signature);
            }

            order(labelMap, address);
            dcv.visitEndLocal(reg, (DexLabel)labelMap.get(address));
            break;
         case 6:
            reg = readULeb128i(in);
            le = lastEntryForReg[reg];
            if (le == null) {
               throw new RuntimeException("Encountered RESTART_LOCAL on new v" + reg);
            }

            if (le.signature == null) {
               DEBUG_DEBUG("Start: v%d :%s, %s", reg, le.name, le.type);
            } else {
               DEBUG_DEBUG("Start: v%d :%s, %s // %s", reg, le.name, le.type, le.signature);
            }

            order(labelMap, address);
            dcv.visitRestartLocal(reg, (DexLabel)labelMap.get(address));
            break;
         case 7:
            order(labelMap, address);
            dcv.visitPrologue((DexLabel)labelMap.get(address));
            break;
         case 8:
            order(labelMap, address);
            dcv.visitEpiogue((DexLabel)labelMap.get(address));
         case 9:
            break;
         default:
            if (opcode < 10) {
               throw new RuntimeException("Invalid extended opcode encountered " + opcode);
            }

            reg = opcode - 10;
            address += reg / 15;
            line += -4 + reg % 15;
            order(labelMap, address);
            dcv.visitLineNumber(line, (DexLabel)labelMap.get(address));
         }
      }
   }

   public int getDexVersion() {
      return this.dex_version;
   }

   public void accept(DexFileVisitor dv) {
      this.accept(dv, 0);
   }

   public List<String> getClassNames() {
      List<String> names = new ArrayList(this.class_defs_size);
      ByteBuffer in = this.classDefIn;

      for(int cid = 0; cid < this.class_defs_size; ++cid) {
         in.position(cid * 32);
         String className = this.getType(in.getInt());
         names.add(className);
      }

      return names;
   }

   public void accept(DexFileVisitor dv, int config) {
      dv.visitDexFileVersion(this.dex_version);

      for(int cid = 0; cid < this.class_defs_size; ++cid) {
         this.accept(dv, cid, config);
      }

      dv.visitEnd();
   }

   public void accept(DexFileVisitor dv, int classIdx, int config) {
      this.classDefIn.position(classIdx * 32);
      int class_idx = this.classDefIn.getInt();
      int access_flags = this.classDefIn.getInt();
      int superclass_idx = this.classDefIn.getInt();
      int interfaces_off = this.classDefIn.getInt();
      int source_file_idx = this.classDefIn.getInt();
      int annotations_off = this.classDefIn.getInt();
      int class_data_off = this.classDefIn.getInt();
      int static_values_off = this.classDefIn.getInt();
      String className = this.getType(class_idx);
      if (!this.ignoreClass(className)) {
         String superClassName = this.getType(superclass_idx);
         String[] interfaceNames = this.getTypeList(interfaces_off);

         try {
            DexClassVisitor dcv = dv.visit(access_flags, className, superClassName, interfaceNames);
            if (dcv != null) {
               this.acceptClass(dcv, source_file_idx, annotations_off, class_data_off, static_values_off, config);
               dcv.visitEnd();
            }
         } catch (Exception var17) {
            DexException dexException = new DexException(var17, "Error process class: [%d]%s", new Object[]{class_idx, className});
            if (0 == (config & 32)) {
               throw dexException;
            }

            niceExceptionMessage(dexException, 0);
         }

      }
   }

   public Boolean ignoreClass(String className) {
      return false;
   }

   private Object readEncodedValue(ByteBuffer in) {
      int b = 255 & in.get();
      int type = b & 31;
      int method_id;
      switch(type) {
      case 0:
         return new Byte((byte)((int)readIntBits(in, b)));
      case 1:
      case 5:
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
      case 13:
      case 14:
      case 15:
      case 18:
      case 19:
      case 20:
      default:
         throw new DexException("Not support yet.");
      case 2:
         return new Short((short)((int)readIntBits(in, b)));
      case 3:
         return new Character((char)((int)readUIntBits(in, b)));
      case 4:
         return new Integer((int)readIntBits(in, b));
      case 6:
         return new Long(readIntBits(in, b));
      case 16:
         return Float.intBitsToFloat((int)(readFloatBits(in, b) >> 32));
      case 17:
         return Double.longBitsToDouble(readFloatBits(in, b));
      case 21:
         return this.getProto((int)readUIntBits(in, b));
      case 22:
         return this.getMethodHandle((int)readUIntBits(in, b));
      case 23:
         return this.getString((int)readUIntBits(in, b));
      case 24:
         method_id = (int)readUIntBits(in, b);
         return new DexType(this.getType(method_id));
      case 25:
         method_id = (int)readUIntBits(in, b);
         return this.getField(method_id);
      case 26:
         method_id = (int)readUIntBits(in, b);
         return this.getMethod(method_id);
      case 27:
         return this.getField((int)readUIntBits(in, b));
      case 28:
         return this.read_encoded_array(in);
      case 29:
         return this.read_encoded_annotation(in);
      case 30:
         return null;
      case 31:
         return new Boolean((b >> 5 & 3) != 0);
      }
   }

   private MethodHandle getMethodHandle(int i) {
      this.methodHandleIdIn.position(i * 8);
      int method_handle_type = this.methodHandleIdIn.getShort() & '\uffff';
      this.methodHandleIdIn.getShort();
      int field_or_method_id = this.methodHandleIdIn.getShort() & '\uffff';
      switch(method_handle_type) {
      case 0:
      case 1:
      case 2:
      case 3:
         return new MethodHandle(method_handle_type, this.getField(field_or_method_id));
      case 4:
      case 5:
         return new MethodHandle(method_handle_type, this.getMethod(field_or_method_id));
      default:
         throw new RuntimeException();
      }
   }

   private void acceptClass(DexClassVisitor dcv, int source_file_idx, int annotations_off, int class_data_off, int static_values_off, int config) {
      if ((config & 1) == 0 && source_file_idx != -1) {
         dcv.visitSource(this.getString(source_file_idx));
      }

      HashMap fieldAnnotationPositions;
      HashMap methodAnnotationPositions;
      HashMap paramAnnotationPositions;
      int static_fields;
      int instance_fields;
      int direct_methods;
      int virtual_methods;
      int lastIndex;
      int parameter_annotation_offset;
      if ((config & 8) == 0) {
         fieldAnnotationPositions = new HashMap();
         methodAnnotationPositions = new HashMap();
         paramAnnotationPositions = new HashMap();
         if (annotations_off != 0) {
            this.annotationsDirectoryItemIn.position(annotations_off);
            int class_annotations_off = this.annotationsDirectoryItemIn.getInt();
            static_fields = this.annotationsDirectoryItemIn.getInt();
            instance_fields = this.annotationsDirectoryItemIn.getInt();
            direct_methods = this.annotationsDirectoryItemIn.getInt();

            for(virtual_methods = 0; virtual_methods < static_fields; ++virtual_methods) {
               lastIndex = this.annotationsDirectoryItemIn.getInt();
               parameter_annotation_offset = this.annotationsDirectoryItemIn.getInt();
               fieldAnnotationPositions.put(lastIndex, parameter_annotation_offset);
            }

            for(virtual_methods = 0; virtual_methods < instance_fields; ++virtual_methods) {
               lastIndex = this.annotationsDirectoryItemIn.getInt();
               parameter_annotation_offset = this.annotationsDirectoryItemIn.getInt();
               methodAnnotationPositions.put(lastIndex, parameter_annotation_offset);
            }

            for(virtual_methods = 0; virtual_methods < direct_methods; ++virtual_methods) {
               lastIndex = this.annotationsDirectoryItemIn.getInt();
               parameter_annotation_offset = this.annotationsDirectoryItemIn.getInt();
               paramAnnotationPositions.put(lastIndex, parameter_annotation_offset);
            }

            if (class_annotations_off != 0) {
               try {
                  this.read_annotation_set_item(class_annotations_off, dcv);
               } catch (Exception var19) {
                  throw new DexException("error on reading Annotation of class ", var19);
               }
            }
         }
      } else {
         fieldAnnotationPositions = null;
         methodAnnotationPositions = null;
         paramAnnotationPositions = null;
      }

      if (class_data_off != 0) {
         ByteBuffer in = this.classDataIn;
         in.position(class_data_off);
         static_fields = readULeb128i(in);
         instance_fields = readULeb128i(in);
         direct_methods = readULeb128i(in);
         virtual_methods = readULeb128i(in);
         lastIndex = 0;
         Object[] constant = null;
         if ((config & 16) == 0 && static_values_off != 0) {
            constant = this.read_encoded_array_item(static_values_off);
         }

         int i;
         for(i = 0; i < static_fields; ++i) {
            Object value = null;
            if (constant != null && i < constant.length) {
               value = constant[i];
            }

            lastIndex = this.acceptField(in, lastIndex, dcv, fieldAnnotationPositions, value, config);
         }

         lastIndex = 0;

         for(parameter_annotation_offset = 0; parameter_annotation_offset < instance_fields; ++parameter_annotation_offset) {
            lastIndex = this.acceptField(in, lastIndex, dcv, fieldAnnotationPositions, (Object)null, config);
         }

         lastIndex = 0;
         boolean firstMethod = true;

         for(i = 0; i < direct_methods; ++i) {
            lastIndex = this.acceptMethod(in, lastIndex, dcv, methodAnnotationPositions, paramAnnotationPositions, config, firstMethod);
            firstMethod = false;
         }

         lastIndex = 0;
         firstMethod = true;

         for(i = 0; i < virtual_methods; ++i) {
            lastIndex = this.acceptMethod(in, lastIndex, dcv, methodAnnotationPositions, paramAnnotationPositions, config, firstMethod);
            firstMethod = false;
         }
      }

   }

   private Object[] read_encoded_array_item(int static_values_off) {
      this.encodedArrayItemIn.position(static_values_off);
      return this.read_encoded_array(this.encodedArrayItemIn);
   }

   private Object[] read_encoded_array(ByteBuffer in) {
      int size = readULeb128i(in);
      Object[] constant = new Object[size];

      for(int i = 0; i < size; ++i) {
         constant[i] = this.readEncodedValue(in);
      }

      return constant;
   }

   private void read_annotation_set_item(int offset, DexAnnotationAble daa) {
      ByteBuffer in = this.annotationSetItemIn;
      in.position(offset);
      int size = in.getInt();

      for(int j = 0; j < size; ++j) {
         int annotation_off = in.getInt();
         this.read_annotation_item(annotation_off, daa);
      }

   }

   private void read_annotation_item(int annotation_off, DexAnnotationAble daa) {
      ByteBuffer in = this.annotationItemIn;
      in.position(annotation_off);
      int visibility = 255 & in.get();
      DexAnnotationNode annotation = this.read_encoded_annotation(in);
      annotation.visibility = Visibility.values()[visibility];
      annotation.accept(daa);
   }

   private DexAnnotationNode read_encoded_annotation(ByteBuffer in) {
      int type_idx = readULeb128i(in);
      int size = readULeb128i(in);
      String _typeString = this.getType(type_idx);
      DexAnnotationNode ann = new DexAnnotationNode(_typeString, Visibility.RUNTIME);

      for(int i = 0; i < size; ++i) {
         int name_idx = readULeb128i(in);
         String nameString = this.getString(name_idx);
         Object value = this.readEncodedValue(in);
         ann.items.add(new DexAnnotationNode.Item(nameString, value));
      }

      return ann;
   }

   private Field getField(int id) {
      this.fieldIdIn.position(id * 8);
      int owner_idx = '\uffff' & this.fieldIdIn.getShort();
      int type_idx = '\uffff' & this.fieldIdIn.getShort();
      int name_idx = this.fieldIdIn.getInt();
      return new Field(this.getType(owner_idx), this.getString(name_idx), this.getType(type_idx));
   }

   private String[] getTypeList(int offset) {
      if (offset == 0) {
         return new String[0];
      } else {
         this.typeListIn.position(offset);
         int size = this.typeListIn.getInt();
         String[] types = new String[size];

         for(int i = 0; i < size; ++i) {
            types[i] = this.getType('\uffff' & this.typeListIn.getShort());
         }

         return types;
      }
   }

   private Proto getProto(int proto_idx) {
      this.protoIdIn.position(proto_idx * 12 + 4);
      int return_type_idx = this.protoIdIn.getInt();
      int parameters_off = this.protoIdIn.getInt();
      String returnType = this.getType(return_type_idx);
      String[] parameterTypes = this.getTypeList(parameters_off);
      return new Proto(parameterTypes, returnType);
   }

   private Method getMethod(int id) {
      this.methoIdIn.position(id * 8);
      int owner_idx = '\uffff' & this.methoIdIn.getShort();
      int proto_idx = '\uffff' & this.methoIdIn.getShort();
      int name_idx = this.methoIdIn.getInt();
      return new Method(this.getType(owner_idx), this.getString(name_idx), this.getProto(proto_idx));
   }

   private String getString(int id) {
      if (id == -1) {
         return null;
      } else {
         int offset = this.stringIdIn.getInt(id * 4);
         this.stringDataIn.position(offset);
         int length = readULeb128i(this.stringDataIn);

         try {
            StringBuilder buff = new StringBuilder((int)((double)length * 1.5D));
            return Mutf8.decode(this.stringDataIn, buff);
         } catch (UTFDataFormatException var5) {
            throw new DexException(var5, "fail to load string %d@%08x", new Object[]{id, offset});
         }
      }
   }

   private String getType(int id) {
      return id == -1 ? null : this.getString(this.typeIdIn.getInt(id * 4));
   }

   private int acceptField(ByteBuffer in, int lastIndex, DexClassVisitor dcv, Map<Integer, Integer> fieldAnnotationPositions, Object value, int config) {
      int diff = readULeb128i(in);
      int field_access_flags = readULeb128i(in);
      int field_id = lastIndex + diff;
      Field field = this.getField(field_id);
      DexFieldVisitor dfv = dcv.visitField(field_access_flags, field, value);
      if (dfv != null) {
         if ((config & 8) == 0) {
            Integer annotation_offset = (Integer)fieldAnnotationPositions.get(field_id);
            if (annotation_offset != null) {
               try {
                  this.read_annotation_set_item(annotation_offset, dfv);
               } catch (Exception var14) {
                  throw new DexException(var14, "while accept annotation in field:%s.", new Object[]{field.toString()});
               }
            }
         }

         dfv.visitEnd();
      }

      return field_id;
   }

   private int acceptMethod(ByteBuffer in, int lastIndex, DexClassVisitor cv, Map<Integer, Integer> methodAnnos, Map<Integer, Integer> parameterAnnos, int config, boolean firstMethod) {
      int offset = in.position();
      int diff = readULeb128i(in);
      int method_access_flags = readULeb128i(in);
      int code_off = readULeb128i(in);
      int method_id = lastIndex + diff;
      Method method = this.getMethod(method_id);
      if (!firstMethod && diff == 0) {
         WARN("GLITCH: duplicated method %s @%08x", method.toString(), offset);
         if ((config & 64) == 0) {
            WARN("WARN: skip method %s @%08x", method.toString(), offset);
            return method_id;
         }
      }

      if (0 == (method_access_flags & 65536) && (method.getName().equals("<init>") || method.getName().equals("<clinit>"))) {
         WARN("GLITCH: method %s @%08x not marked as ACC_CONSTRUCTOR", method.toString(), offset);
      }

      try {
         DexMethodVisitor dmv = cv.visitMethod(method_access_flags, method);
         if (dmv != null) {
            if ((config & 8) == 0) {
               Integer annotation_offset = (Integer)methodAnnos.get(method_id);
               if (annotation_offset != null) {
                  try {
                     this.read_annotation_set_item(annotation_offset, dmv);
                  } catch (Exception var20) {
                     throw new DexException(var20, "while accept annotation in method:%s.", new Object[]{method.toString()});
                  }
               }

               Integer parameter_annotation_offset = (Integer)parameterAnnos.get(method_id);
               if (parameter_annotation_offset != null) {
                  try {
                     this.read_annotation_set_ref_list(parameter_annotation_offset, dmv);
                  } catch (Exception var19) {
                     throw new DexException(var19, "while accept parameter annotation in method:%s.", new Object[]{method.toString()});
                  }
               }
            }

            if (code_off != 0) {
               boolean keep = true;
               if (0 != (4 & config)) {
                  keep = 0 != (128 & config) && method.getName().equals("<clinit>");
               }

               if (keep) {
                  DexCodeVisitor dcv = dmv.visitCode();
                  if (dcv != null) {
                     try {
                        this.acceptCode(code_off, dcv, config, (method_access_flags & 8) != 0, method);
                     } catch (Exception var18) {
                        throw new DexException(var18, "while accept code in method:[%s] @%08x", new Object[]{method.toString(), code_off});
                     }
                  }
               }
            }

            dmv.visitEnd();
         }

         return method_id;
      } catch (Exception var21) {
         throw new DexException(var21, "while accept method:[%s]", new Object[]{method.toString()});
      }
   }

   private void read_annotation_set_ref_list(int parameter_annotation_offset, DexMethodVisitor dmv) {
      ByteBuffer in = this.annotationSetRefListIn;
      in.position(parameter_annotation_offset);
      int size = in.getInt();

      for(int j = 0; j < size; ++j) {
         int param_annotation_offset = in.getInt();
         if (param_annotation_offset != 0) {
            DexAnnotationAble dpav = dmv.visitParameterAnnotation(j);

            try {
               if (dpav != null) {
                  this.read_annotation_set_item(param_annotation_offset, dpav);
               }
            } catch (Exception var9) {
               throw new DexException(var9, "while accept parameter annotation in parameter:[%d]", new Object[]{j});
            }
         }
      }

   }

   public final int getClassSize() {
      return this.class_defs_size;
   }

   private void findLabels(byte[] insns, BitSet nextBit, BitSet badOps, Map<Integer, DexLabel> labelsMap, Set<Integer> handlers, Method method) {
      Queue<Integer> q = new LinkedList();
      q.add(0);
      q.addAll(handlers);
      handlers.clear();

      while(!q.isEmpty()) {
         int offset = (Integer)q.poll();
         if (!nextBit.get(offset)) {
            nextBit.set(offset);

            try {
               this.travelInsn(labelsMap, q, insns, offset);
            } catch (IndexOutOfBoundsException var10) {
               badOps.set(offset);
               WARN("GLITCH: %04x %s | not enough space for reading instruction", offset, method.toString());
            } catch (DexFileReader.BadOpException var11) {
               badOps.set(offset);
               WARN("GLITCH: %04x %s | %s", offset, method.toString(), var11.getMessage());
            }
         }
      }

   }

   private void travelInsn(Map<Integer, DexLabel> labelsMap, Queue<Integer> q, byte[] insns, int offset) {
      int u1offset = offset * 2;
      if (u1offset >= insns.length) {
         throw new IndexOutOfBoundsException();
      } else {
         int opcode = 255 & insns[u1offset];
         Op op = null;
         if (opcode < Op.ops.length) {
            op = Op.ops[opcode];
         }

         if (op != null && op.format != null) {
            boolean canContinue = true;
            int target;
            int element_width;
            if (op.canBranch()) {
               switch(op.format) {
               case kFmt10t:
                  target = offset + insns[u1offset + 1];
                  if (target < 0 || target * 2 > insns.length) {
                     throw new DexFileReader.BadOpException("jump out of insns %s -> %04x", new Object[]{op, target});
                  }

                  q.add(target);
                  order(labelsMap, target);
                  break;
               case kFmt20t:
               case kFmt21t:
                  target = offset + sshort(insns, u1offset + 2);
                  if (target < 0 || target * 2 > insns.length) {
                     throw new DexFileReader.BadOpException("jump out of insns %s -> %04x", new Object[]{op, target});
                  }

                  q.add(target);
                  order(labelsMap, target);
                  break;
               case kFmt22t:
                  target = offset + sshort(insns, u1offset + 2);
                  element_width = ubyte(insns, u1offset + 1);
                  boolean cmpSameReg = (element_width & 15) == (element_width >> 4 & 15);
                  boolean skipTarget = false;
                  if (cmpSameReg) {
                     switch(op) {
                     case IF_EQ:
                     case IF_GE:
                     case IF_LE:
                        canContinue = false;
                        break;
                     case IF_NE:
                     case IF_GT:
                     case IF_LT:
                        skipTarget = true;
                     }
                  }

                  if (!skipTarget) {
                     if (target < 0 || target * 2 > insns.length) {
                        throw new DexFileReader.BadOpException("jump out of insns %s -> %04x", new Object[]{op, target});
                     }

                     q.add(target);
                     order(labelsMap, target);
                  }
                  break;
               case kFmt30t:
               case kFmt31t:
                  target = offset + sint(insns, u1offset + 2);
                  if (target < 0 || target * 2 > insns.length) {
                     throw new DexFileReader.BadOpException("jump out of insns %s -> %04x", new Object[]{op, target});
                  }

                  q.add(target);
                  order(labelsMap, target);
               }
            }

            int size;
            if (op.canSwitch()) {
               order(labelsMap, offset + op.format.size);
               element_width = 2 * (offset + sint(insns, u1offset + 2));
               if (element_width + 2 >= insns.length) {
                  throw new DexFileReader.BadOpException("bad payload offset for %s", new Object[]{op});
               }

               int i;
               int b;
               label164:
               switch(insns[element_width + 1]) {
               case 1:
                  size = ushort(insns, element_width + 2);
                  b = element_width + 8;
                  i = 0;

                  while(true) {
                     if (i >= size) {
                        break label164;
                     }

                     target = offset + sint(insns, b + i * 4);
                     if (target < 0 || target * 2 > insns.length) {
                        throw new DexFileReader.BadOpException("jump out of insns %s -> %04x", new Object[]{op, target});
                     }

                     q.add(target);
                     order(labelsMap, target);
                     ++i;
                  }
               case 2:
                  size = ushort(insns, element_width + 2);
                  b = element_width + 4 + 4 * size;
                  i = 0;

                  while(true) {
                     if (i >= size) {
                        break label164;
                     }

                     target = offset + sint(insns, b + i * 4);
                     if (target < 0 || target * 2 > insns.length) {
                        throw new DexFileReader.BadOpException("jump out of insns %s -> %04x", new Object[]{op, target});
                     }

                     q.add(target);
                     order(labelsMap, target);
                     ++i;
                  }
               default:
                  throw new DexFileReader.BadOpException("bad payload for %s", new Object[]{op});
               }
            }

            if (canContinue) {
               element_width = Integer.MAX_VALUE;
               switch(op.indexType) {
               case kIndexStringRef:
                  if (op.format == InstructionFormat.kFmt31c) {
                     element_width = uint(insns, u1offset + 2);
                  } else {
                     element_width = ushort(insns, u1offset + 2);
                  }

                  canContinue = element_width >= 0 && element_width < this.string_ids_size;
                  break;
               case kIndexTypeRef:
                  element_width = ushort(insns, u1offset + 2);
                  canContinue = element_width < this.type_ids_size;
                  break;
               case kIndexMethodRef:
                  element_width = ushort(insns, u1offset + 2);
                  canContinue = element_width < this.method_ids_size;
                  break;
               case kIndexFieldRef:
                  element_width = ushort(insns, u1offset + 2);
                  canContinue = element_width < this.field_ids_size;
                  break;
               case kIndexCallSiteRef:
                  element_width = ushort(insns, u1offset + 2);
                  canContinue = element_width < this.call_site_ids_size;
                  break;
               case kIndexMethodAndProtoRef:
                  element_width = ushort(insns, u1offset + 2);
                  size = ushort(insns, u1offset + 6);
                  canContinue = element_width < this.method_ids_size && size < this.proto_ids_size;
               }

               if (!canContinue) {
                  throw new DexFileReader.BadOpException("index-out-of-range for %s index: %d", new Object[]{op, element_width});
               }
            }

            if (canContinue && op.canContinue()) {
               if (op == Op.NOP) {
                  switch(insns[u1offset + 1]) {
                  case 0:
                     q.add(offset + op.format.size);
                     break;
                  case 1:
                     element_width = ushort(insns, u1offset + 2);
                     q.add(offset + element_width * 2 + 4);
                     break;
                  case 2:
                     element_width = ushort(insns, u1offset + 2);
                     q.add(offset + element_width * 4 + 2);
                     break;
                  case 3:
                     element_width = ushort(insns, u1offset + 2);
                     size = uint(insns, u1offset + 4);
                     q.add(offset + (size * element_width + 1) / 2 + 4);
                  }
               } else {
                  q.add(offset + op.format.size);
               }
            }

         } else {
            throw new DexFileReader.BadOpException("zero-width instruction op=0x%02x", new Object[]{opcode});
         }
      }
   }

   private void findTryCatch(ByteBuffer in, DexCodeVisitor dcv, int tries_size, int insn_size, Map<Integer, DexLabel> labelsMap, Set<Integer> handlers) {
      int encoded_catch_handler_list = in.position() + tries_size * 8;
      ByteBuffer handlerIn = in.duplicate().order(ByteOrder.LITTLE_ENDIAN);

      for(int i = 0; i < tries_size; ++i) {
         int start_addr = in.getInt();
         int insn_count = '\uffff' & in.getShort();
         int handler_offset = '\uffff' & in.getShort();
         if (start_addr <= insn_size) {
            order(labelsMap, start_addr);
            int end = start_addr + insn_count;
            order(labelsMap, end);
            handlerIn.position(encoded_catch_handler_list + handler_offset);
            boolean catchAll = false;
            int listSize = readLeb128i(handlerIn);
            int handlerCount = listSize;
            if (listSize <= 0) {
               listSize = -listSize;
               handlerCount = listSize + 1;
               catchAll = true;
            }

            DexLabel[] labels = new DexLabel[handlerCount];
            String[] types = new String[handlerCount];

            int handler;
            for(handler = 0; handler < listSize; ++handler) {
               int type_id = readULeb128i(handlerIn);
               int handler = readULeb128i(handlerIn);
               order(labelsMap, handler);
               handlers.add(handler);
               types[handler] = this.getType(type_id);
               labels[handler] = (DexLabel)labelsMap.get(handler);
            }

            if (catchAll) {
               handler = readULeb128i(handlerIn);
               order(labelsMap, handler);
               handlers.add(handler);
               labels[listSize] = (DexLabel)labelsMap.get(handler);
            }

            dcv.visitTryCatch((DexLabel)labelsMap.get(start_addr), (DexLabel)labelsMap.get(end), labels, types);
         }
      }

   }

   void acceptCode(int code_off, DexCodeVisitor dcv, int config, boolean isStatic, Method method) {
      ByteBuffer in = this.codeItemIn;
      in.position(code_off);
      int registers_size = '\uffff' & in.getShort();
      in.getShort();
      in.getShort();
      int tries_size = '\uffff' & in.getShort();
      int debug_info_off = in.getInt();
      int insns = in.getInt();
      byte[] insnsArray = new byte[insns * 2];
      in.get(insnsArray);
      dcv.visitRegister(registers_size);
      BitSet nextInsn = new BitSet();
      Map<Integer, DexLabel> labelsMap = new TreeMap();
      Set<Integer> handlers = new HashSet();
      if (tries_size > 0) {
         if ((insns & 1) != 0) {
            in.getShort();
         }

         if (0 == (config & 256)) {
            this.findTryCatch(in, dcv, tries_size, insns, labelsMap, handlers);
         }
      }

      if (debug_info_off != 0 && 0 == (config & 1)) {
         DexDebugVisitor ddv = dcv.visitDebug();
         if (ddv != null) {
            this.read_debug_info(debug_info_off, registers_size, isStatic, method, labelsMap, ddv);
            ddv.visitEnd();
         }
      }

      BitSet badOps = new BitSet();
      this.findLabels(insnsArray, nextInsn, badOps, labelsMap, handlers, method);
      this.acceptInsn(insnsArray, dcv, nextInsn, badOps, labelsMap);
      dcv.visitEnd();
   }

   private void acceptInsn(byte[] insns, DexCodeVisitor dcv, BitSet nextInsn, BitSet badOps, Map<Integer, DexLabel> labelsMap) {
      Iterator<Integer> labelOffsetIterator = labelsMap.keySet().iterator();
      Integer nextLabelOffset = labelOffsetIterator.hasNext() ? (Integer)labelOffsetIterator.next() : null;
      Op[] values = Op.ops;

      for(int offset = nextInsn.nextSetBit(0); offset >= 0; offset = nextInsn.nextSetBit(offset + 1)) {
         while(nextLabelOffset != null && nextLabelOffset <= offset) {
            dcv.visitLabel((DexLabel)labelsMap.get(nextLabelOffset));
            nextLabelOffset = labelOffsetIterator.hasNext() ? (Integer)labelOffsetIterator.next() : null;
         }

         if (badOps.get(offset)) {
            dcv.visitStmt0R(Op.BAD_OP);
         } else {
            int u1offset = offset * 2;
            int opcode = 255 & insns[u1offset];
            Op op = values[opcode];
            int a;
            int b;
            int c;
            int target;
            int[] keys;
            int i;
            int first_key;
            int size;
            int[] data;
            switch(op.format) {
            case kFmt10t:
               target = offset + insns[u1offset + 1];
               dcv.visitJumpStmt(op, -1, -1, (DexLabel)labelsMap.get(target));
               break;
            case kFmt20t:
               target = offset + sshort(insns, u1offset + 2);
               dcv.visitJumpStmt(op, -1, -1, (DexLabel)labelsMap.get(target));
               break;
            case kFmt21t:
               target = offset + sshort(insns, u1offset + 2);
               dcv.visitJumpStmt(op, ubyte(insns, u1offset + 1), -1, (DexLabel)labelsMap.get(target));
               break;
            case kFmt22t:
               target = offset + sshort(insns, u1offset + 2);
               a = ubyte(insns, u1offset + 1);
               b = a & 15;
               c = a >> 4;
               boolean ignore = false;
               if (b == c) {
                  switch(op) {
                  case IF_EQ:
                  case IF_GE:
                  case IF_LE:
                     dcv.visitJumpStmt(Op.GOTO, 0, 0, (DexLabel)labelsMap.get(target));
                     ignore = true;
                     break;
                  case IF_NE:
                  case IF_GT:
                  case IF_LT:
                     ignore = true;
                  }
               }

               if (!ignore) {
                  dcv.visitJumpStmt(op, b, c, (DexLabel)labelsMap.get(target));
               }
               break;
            case kFmt30t:
               target = offset + sint(insns, u1offset + 2);
               dcv.visitJumpStmt(op, -1, -1, (DexLabel)labelsMap.get(target));
               break;
            case kFmt31t:
               target = offset + sint(insns, u1offset + 2);
               a = ubyte(insns, u1offset + 1);
               int u1SwitchData = 2 * target;
               int z;
               int i;
               if (op == Op.FILL_ARRAY_DATA) {
                  size = ushort(insns, u1SwitchData + 2);
                  first_key = uint(insns, u1SwitchData + 4);
                  switch(size) {
                  case 1:
                     byte[] data = new byte[first_key];
                     System.arraycopy(insns, u1SwitchData + 8, data, 0, first_key);
                     dcv.visitFillArrayDataStmt(op, a, data);
                     break;
                  case 2:
                     short[] data = new short[first_key];

                     for(z = 0; z < first_key; ++z) {
                        data[z] = (short)sshort(insns, u1SwitchData + 8 + 2 * z);
                     }

                     dcv.visitFillArrayDataStmt(op, a, data);
                  case 3:
                  case 5:
                  case 6:
                  case 7:
                  default:
                     break;
                  case 4:
                     data = new int[first_key];

                     for(z = 0; z < first_key; ++z) {
                        data[z] = sint(insns, u1SwitchData + 8 + 4 * z);
                     }

                     dcv.visitFillArrayDataStmt(op, a, data);
                     break;
                  case 8:
                     long[] data = new long[first_key];

                     for(z = 0; z < first_key; ++z) {
                        i = u1SwitchData + 8 + 8 * z;
                        long z = 0L;
                        z |= (long)ushort(insns, i + 0) << 0;
                        z |= (long)ushort(insns, i + 2) << 16;
                        z |= (long)ushort(insns, i + 4) << 32;
                        z |= (long)ushort(insns, i + 6) << 48;
                        data[z] = z;
                     }

                     dcv.visitFillArrayDataStmt(op, a, data);
                  }
               } else {
                  DexLabel[] labels;
                  if (op == Op.SPARSE_SWITCH) {
                     size = sshort(insns, u1SwitchData + 2);
                     keys = new int[size];
                     labels = new DexLabel[size];
                     z = u1SwitchData + 4;

                     for(i = 0; i < size; ++i) {
                        keys[i] = sint(insns, z + i * 4);
                     }

                     z += size * 4;

                     for(i = 0; i < size; ++i) {
                        labels[i] = (DexLabel)labelsMap.get(offset + sint(insns, z + i * 4));
                     }

                     dcv.visitSparseSwitchStmt(op, a, keys, labels);
                  } else {
                     size = sshort(insns, u1SwitchData + 2);
                     first_key = sint(insns, u1SwitchData + 4);
                     labels = new DexLabel[size];
                     z = u1SwitchData + 8;

                     for(i = 0; i < size; ++i) {
                        labels[i] = (DexLabel)labelsMap.get(offset + sint(insns, z));
                        z += 4;
                     }

                     dcv.visitPackedSwitchStmt(op, a, first_key, labels);
                  }
               }
               break;
            case kFmt10x:
               dcv.visitStmt0R(op);
               break;
            case kFmt11x:
               dcv.visitStmt1R(op, 255 & insns[u1offset + 1]);
               break;
            case kFmt12x:
               a = ubyte(insns, u1offset + 1);
               dcv.visitStmt2R(op, a & 15, a >> 4);
               break;
            case kFmt21c:
               a = ubyte(insns, u1offset + 1);
               b = ushort(insns, u1offset + 2);
               switch(op.indexType) {
               case kIndexStringRef:
                  dcv.visitConstStmt(op, a, this.getString(b));
                  continue;
               case kIndexTypeRef:
                  if (op == Op.CONST_CLASS) {
                     dcv.visitConstStmt(op, a, new DexType(this.getType(b)));
                  } else {
                     dcv.visitTypeStmt(op, a, -1, this.getType(b));
                  }
               case kIndexMethodRef:
               default:
                  continue;
               case kIndexFieldRef:
                  dcv.visitFieldStmt(op, a, -1, this.getField(b));
                  continue;
               }
            case kFmt22c:
               a = ubyte(insns, u1offset + 1);
               b = ushort(insns, u1offset + 2);
               switch(op.indexType) {
               case kIndexTypeRef:
                  dcv.visitTypeStmt(op, a & 15, a >> 4, this.getType(b));
                  continue;
               case kIndexFieldRef:
                  dcv.visitFieldStmt(op, a & 15, a >> 4, this.getField(b));
               default:
                  continue;
               }
            case kFmt31c:
               if (op.indexType == InstructionIndexType.kIndexStringRef) {
                  a = ubyte(insns, u1offset + 1);
                  b = uint(insns, u1offset + 2);
                  dcv.visitConstStmt(op, a, this.getString(b));
               }
               break;
            case kFmt35c:
               a = ubyte(insns, u1offset + 1);
               b = ushort(insns, u1offset + 2);
               size = ubyte(insns, u1offset + 4);
               first_key = ubyte(insns, u1offset + 5);
               data = new int[a >> 4];
               switch(a >> 4) {
               case 5:
                  data[4] = a & 15;
               case 4:
                  data[3] = 15 & first_key >> 4;
               case 3:
                  data[2] = 15 & first_key >> 0;
               case 2:
                  data[1] = 15 & size >> 4;
               case 1:
                  data[0] = 15 & size >> 0;
               default:
                  if (op.indexType == InstructionIndexType.kIndexTypeRef) {
                     dcv.visitFilledNewArrayStmt(op, data, this.getType(b));
                  } else if (op.indexType == InstructionIndexType.kIndexCallSiteRef) {
                     Object[] callsite = this.getCallSite(b);
                     Object[] constArgs = Arrays.copyOfRange(callsite, 3, callsite.length);
                     dcv.visitMethodStmt(op, data, (String)callsite[1], (Proto)callsite[2], (MethodHandle)callsite[0], constArgs);
                  } else {
                     dcv.visitMethodStmt(op, data, this.getMethod(b));
                  }
                  continue;
               }
            case kFmt3rc:
               a = ubyte(insns, u1offset + 1);
               b = ushort(insns, u1offset + 2);
               c = ushort(insns, u1offset + 4);
               int[] regs = new int[a];

               for(first_key = 0; first_key < a; ++first_key) {
                  regs[first_key] = c + first_key;
               }

               if (op.indexType == InstructionIndexType.kIndexTypeRef) {
                  dcv.visitFilledNewArrayStmt(op, regs, this.getType(b));
               } else if (op.indexType == InstructionIndexType.kIndexCallSiteRef) {
                  Object[] callsite = this.getCallSite(b);
                  Object[] constArgs = Arrays.copyOfRange(callsite, 3, callsite.length - 3);
                  dcv.visitMethodStmt(op, regs, (String)callsite[1], (Proto)callsite[2], (MethodHandle)callsite[0], constArgs);
               } else {
                  dcv.visitMethodStmt(op, regs, this.getMethod(b));
               }
               break;
            case kFmt45cc:
               a = ubyte(insns, u1offset + 1);
               b = ushort(insns, u1offset + 2);
               size = ubyte(insns, u1offset + 4);
               first_key = ubyte(insns, u1offset + 5);
               i = ushort(insns, u1offset + 6);
               int[] regs = new int[a >> 4];
               switch(a >> 4) {
               case 5:
                  regs[4] = a & 15;
               case 4:
                  regs[3] = 15 & first_key >> 4;
               case 3:
                  regs[2] = 15 & first_key >> 0;
               case 2:
                  regs[1] = 15 & size >> 4;
               case 1:
                  regs[0] = 15 & size >> 0;
               default:
                  dcv.visitMethodStmt(op, regs, this.getMethod(b), this.getProto(i));
                  continue;
               }
            case kFmt4rcc:
               a = ubyte(insns, u1offset + 1);
               b = ushort(insns, u1offset + 2);
               c = ushort(insns, u1offset + 4);
               size = ushort(insns, u1offset + 6);
               keys = new int[a];

               for(i = 0; i < a; ++i) {
                  keys[i] = c + i;
               }

               dcv.visitMethodStmt(op, keys, this.getMethod(b), this.getProto(size));
               break;
            case kFmt22x:
               a = ubyte(insns, u1offset + 1);
               b = ushort(insns, u1offset + 2);
               dcv.visitStmt2R(op, a, b);
               break;
            case kFmt23x:
               a = ubyte(insns, u1offset + 1);
               b = ubyte(insns, u1offset + 2);
               c = ubyte(insns, u1offset + 3);
               dcv.visitStmt3R(op, a, b, c);
               break;
            case kFmt32x:
               a = ushort(insns, u1offset + 2);
               b = ushort(insns, u1offset + 4);
               dcv.visitStmt2R(op, a, b);
               break;
            case kFmt11n:
               int a = insns[u1offset + 1];
               dcv.visitConstStmt(op, a & 15, a >> 4);
               break;
            case kFmt21h:
               a = ubyte(insns, u1offset + 1);
               b = sshort(insns, u1offset + 2);
               if (op == Op.CONST_HIGH16) {
                  dcv.visitConstStmt(op, a, b << 16);
               } else {
                  dcv.visitConstStmt(op, a, (long)b << 48);
               }
               break;
            case kFmt21s:
               a = ubyte(insns, u1offset + 1);
               b = sshort(insns, u1offset + 2);
               if (op == Op.CONST_16) {
                  dcv.visitConstStmt(op, a, b);
               } else {
                  dcv.visitConstStmt(op, a, (long)b);
               }
               break;
            case kFmt22b:
               a = ubyte(insns, u1offset + 1);
               b = ubyte(insns, u1offset + 2);
               c = sbyte(insns, u1offset + 3);
               dcv.visitStmt2R1N(op, a, b, c);
               break;
            case kFmt22s:
               a = ubyte(insns, u1offset + 1);
               b = sshort(insns, u1offset + 2);
               dcv.visitStmt2R1N(op, a & 15, a >> 4, b);
               break;
            case kFmt31i:
               a = ubyte(insns, u1offset + 1);
               b = sint(insns, u1offset + 2);
               if (op == Op.CONST) {
                  dcv.visitConstStmt(op, a, b);
               } else {
                  dcv.visitConstStmt(op, a, (long)b);
               }
               break;
            case kFmt51l:
               a = ubyte(insns, u1offset + 1);
               long z = 0L;
               z |= (long)ushort(insns, u1offset + 2) << 0;
               z |= (long)ushort(insns, u1offset + 4) << 16;
               z |= (long)ushort(insns, u1offset + 6) << 32;
               z |= (long)ushort(insns, u1offset + 8) << 48;
               dcv.visitConstStmt(op, a, z);
            }
         }
      }

      while(nextLabelOffset != null) {
         dcv.visitLabel((DexLabel)labelsMap.get(nextLabelOffset));
         if (!labelOffsetIterator.hasNext()) {
            break;
         }

         nextLabelOffset = (Integer)labelOffsetIterator.next();
      }

   }

   private Object[] getCallSite(int b) {
      this.callSiteIdIn.position(b * 4);
      int call_site_off = this.callSiteIdIn.getInt();
      return this.read_encoded_array_item(call_site_off);
   }

   private static class LocalEntry {
      public String name;
      public String type;
      public String signature;

      private LocalEntry(String name, String type) {
         this.name = name;
         this.type = type;
      }

      private LocalEntry(String name, String type, String signature) {
         this.name = name;
         this.type = type;
         this.signature = signature;
      }

      // $FF: synthetic method
      LocalEntry(String x0, String x1, String x2, Object x3) {
         this(x0, x1, x2);
      }

      // $FF: synthetic method
      LocalEntry(String x0, String x1, Object x2) {
         this(x0, x1);
      }
   }

   static class BadOpException extends RuntimeException {
      public BadOpException(String fmt, Object... args) {
         super(String.format(fmt, args));
      }
   }
}
