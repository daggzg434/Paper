package com.googlecode.d2j.dex.writer.ev;

import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.dex.writer.item.BaseItem;
import com.googlecode.d2j.dex.writer.item.FieldIdItem;
import com.googlecode.d2j.dex.writer.item.MethodIdItem;
import com.googlecode.d2j.dex.writer.item.StringIdItem;
import com.googlecode.d2j.dex.writer.item.TypeIdItem;

public class EncodedValue {
   public static final int VALUE_ANNOTATION = 29;
   public static final int VALUE_ARRAY = 28;
   public static final int VALUE_BOOLEAN = 31;
   public static final int VALUE_BYTE = 0;
   public static final int VALUE_CHAR = 3;
   public static final int VALUE_DOUBLE = 17;
   public static final int VALUE_ENUM = 27;
   public static final int VALUE_FIELD = 25;
   public static final int VALUE_FLOAT = 16;
   public static final int VALUE_INT = 4;
   public static final int VALUE_LONG = 6;
   public static final int VALUE_METHOD = 26;
   public static final int VALUE_NULL = 30;
   public static final int VALUE_SHORT = 2;
   public static final int VALUE_STRING = 23;
   public static final int VALUE_TYPE = 24;
   public final int valueType;
   public Object value;

   public EncodedValue(int valueType, Object value) {
      this.valueType = valueType;
      this.value = value;
   }

   public static int lengthOfDouble(double value) {
      int requiredBits = 64 - Long.numberOfTrailingZeros(Double.doubleToRawLongBits(value));
      if (requiredBits == 0) {
         requiredBits = 1;
      }

      return requiredBits + 7 >> 3;
   }

   public static int lengthOfFloat(float value) {
      int requiredBits = 64 - Long.numberOfTrailingZeros((long)Float.floatToRawIntBits(value) << 32);
      if (requiredBits == 0) {
         requiredBits = 1;
      }

      return requiredBits + 7 >> 3;
   }

   public static int lengthOfSint(int value) {
      int nbBits = 33 - Integer.numberOfLeadingZeros(value ^ value >> 31);
      return nbBits + 7 >> 3;
   }

   public static int lengthOfSint(long value) {
      int nbBits = 65 - Long.numberOfLeadingZeros(value ^ value >> 63);
      return nbBits + 7 >> 3;
   }

   public static final int lengthOfUint(int val) {
      int size = 1;
      if (val != 0) {
         val >>>= 8;
         if (val != 0) {
            ++size;
            val >>>= 8;
            if (val != 0) {
               ++size;
               val >>>= 8;
               if (val != 0) {
                  ++size;
               }
            }
         }
      }

      return size;
   }

   public static EncodedValue wrap(Object v) {
      if (v == null) {
         return new EncodedValue(30, (Object)null);
      } else if (v instanceof Integer) {
         return new EncodedValue(4, v);
      } else if (v instanceof Short) {
         return new EncodedValue(2, v);
      } else if (v instanceof Character) {
         return new EncodedValue(3, v);
      } else if (v instanceof Long) {
         return new EncodedValue(6, v);
      } else if (v instanceof Float) {
         return new EncodedValue(16, v);
      } else if (v instanceof Double) {
         return new EncodedValue(17, v);
      } else if (v instanceof Boolean) {
         return new EncodedValue(31, v);
      } else if (v instanceof Byte) {
         return new EncodedValue(0, v);
      } else if (v instanceof TypeIdItem) {
         return new EncodedValue(24, v);
      } else if (v instanceof StringIdItem) {
         return new EncodedValue(23, v);
      } else if (v instanceof FieldIdItem) {
         return new EncodedValue(25, v);
      } else if (v instanceof MethodIdItem) {
         return new EncodedValue(26, v);
      } else {
         throw new RuntimeException("not support");
      }
   }

   public static EncodedValue defaultValueForType(String typeString) {
      switch(typeString.charAt(0)) {
      case 'B':
         return new EncodedValue(0, (byte)0);
      case 'C':
         return new EncodedValue(3, '\u0000');
      case 'D':
         return new EncodedValue(17, 0.0D);
      case 'E':
      case 'G':
      case 'H':
      case 'K':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      default:
         throw new RuntimeException();
      case 'F':
         return new EncodedValue(16, 0.0F);
      case 'I':
         return new EncodedValue(4, 0);
      case 'J':
         return new EncodedValue(6, 0L);
      case 'L':
      case '[':
         return new EncodedValue(30, (Object)null);
      case 'S':
         return new EncodedValue(2, Short.valueOf((short)0));
      case 'Z':
         return new EncodedValue(31, false);
      }
   }

   static byte[] encodeLong(int length, long value) {
      byte[] data = new byte[length];
      switch(length) {
      case 8:
         data[7] = (byte)((int)(value >> 56));
      case 7:
         data[6] = (byte)((int)(value >> 48));
      case 6:
         data[5] = (byte)((int)(value >> 40));
      case 5:
         data[4] = (byte)((int)(value >> 32));
      case 4:
         data[3] = (byte)((int)(value >> 24));
      case 3:
         data[2] = (byte)((int)(value >> 16));
      case 2:
         data[1] = (byte)((int)(value >> 8));
      case 1:
         data[0] = (byte)((int)(value >> 0));
         return data;
      default:
         throw new RuntimeException();
      }
   }

   static byte[] encodeSint(int length, int value) {
      byte[] data = new byte[length];
      switch(length) {
      case 4:
         data[3] = (byte)(value >> 24);
      case 3:
         data[2] = (byte)(value >> 16);
      case 2:
         data[1] = (byte)(value >> 8);
      case 1:
         data[0] = (byte)(value >> 0);
         return data;
      default:
         throw new RuntimeException();
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EncodedValue that = (EncodedValue)o;
         if (this.valueType != that.valueType) {
            return false;
         } else {
            if (this.value != null) {
               if (!this.value.equals(that.value)) {
                  return false;
               }
            } else if (that.value != null) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.valueType;
      result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
      return result;
   }

   public boolean isDefaultValueForType() {
      if (this.valueType == 30) {
         return true;
      } else {
         switch(this.valueType) {
         case 0:
         case 2:
         case 4:
            return ((Number)this.value).intValue() == 0;
         case 3:
            Character c = (Character)this.value;
            return c == 0;
         case 6:
            return ((Number)this.value).longValue() == 0L;
         case 16:
            return ((Number)this.value).floatValue() == 0.0F;
         case 17:
            return ((Number)this.value).doubleValue() == 0.0D;
         case 31:
            Boolean z = (Boolean)this.value;
            return Boolean.FALSE.equals(z);
         default:
            return false;
         }
      }
   }

   protected int doPlace(int offset) {
      switch(this.valueType) {
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
      default:
         return offset + this.getValueArg() + 1;
      case 28:
         EncodedArray ea = (EncodedArray)this.value;
         return ea.place(offset);
      case 29:
         EncodedAnnotation ea = (EncodedAnnotation)this.value;
         return ea.place(offset);
      case 30:
      case 31:
         return offset;
      }
   }

   protected int getValueArg() {
      switch(this.valueType) {
      case 0:
         return 0;
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
      case 21:
      case 22:
      default:
         return 0;
      case 2:
      case 4:
         return lengthOfSint(((Number)this.value).intValue()) - 1;
      case 3:
         return lengthOfUint((Character)this.value) - 1;
      case 6:
         return lengthOfSint(((Number)this.value).longValue()) - 1;
      case 16:
         return lengthOfFloat(((Number)this.value).floatValue()) - 1;
      case 17:
         return lengthOfDouble(((Number)this.value).doubleValue()) - 1;
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
         BaseItem bi = (BaseItem)this.value;
         return lengthOfUint(bi.index) - 1;
      case 28:
      case 29:
      case 30:
         return 0;
      case 31:
         return Boolean.TRUE.equals(this.value) ? 1 : 0;
      }
   }

   public final int place(int offset) {
      ++offset;
      return this.doPlace(offset);
   }

   public void write(DataOut out) {
      int valueArg = this.getValueArg();
      out.ubyte("(value_arg << 5 | value_type", valueArg << 5 | this.valueType);
      switch(this.valueType) {
      case 0:
         out.ubyte("value_byte", (Byte)this.value);
         break;
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
      case 21:
      case 22:
      default:
         throw new RuntimeException();
      case 2:
         out.bytes("value_short", encodeSint(valueArg + 1, (Short)this.value));
         break;
      case 3:
         out.bytes("value_char", encodeSint(valueArg + 1, (Character)this.value));
         break;
      case 4:
         out.bytes("value_int", encodeSint(valueArg + 1, (Integer)this.value));
         break;
      case 6:
         out.bytes("value_long", encodeLong(valueArg + 1, (Long)this.value));
         break;
      case 16:
         out.bytes("value_float", this.writeRightZeroExtendedValue(valueArg + 1, (long)Float.floatToIntBits(((Number)this.value).floatValue()) << 32));
         break;
      case 17:
         out.bytes("value_double", this.writeRightZeroExtendedValue(valueArg + 1, Double.doubleToLongBits(((Number)this.value).doubleValue())));
         break;
      case 23:
      case 24:
      case 25:
      case 26:
      case 27:
         out.bytes("value_xidx", encodeLong(valueArg + 1, (long)((BaseItem)this.value).index));
         break;
      case 28:
         EncodedArray ea = (EncodedArray)this.value;
         ea.write(out);
         break;
      case 29:
         EncodedAnnotation ea = (EncodedAnnotation)this.value;
         ea.write(out);
      case 30:
      case 31:
      }

   }

   private byte[] writeRightZeroExtendedValue(int requiredBytes, long value) {
      value >>= 64 - requiredBytes * 8;
      byte[] s = new byte[requiredBytes];

      for(int i = 0; i < requiredBytes; ++i) {
         s[i] = (byte)((int)value);
         value >>= 8;
      }

      return s;
   }
}
