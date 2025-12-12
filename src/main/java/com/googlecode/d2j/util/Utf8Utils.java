package com.googlecode.d2j.util;

import java.io.IOException;
import java.io.Writer;

public final class Utf8Utils {
   private static char[] tempBuffer = null;

   public static byte[] stringToUtf8Bytes(String string) {
      int len = string.length();
      byte[] bytes = new byte[len * 3];
      int outAt = 0;

      for(int i = 0; i < len; ++i) {
         char c = string.charAt(i);
         if (c != 0 && c < 128) {
            bytes[outAt] = (byte)c;
            ++outAt;
         } else if (c < 2048) {
            bytes[outAt] = (byte)(c >> 6 & 31 | 192);
            bytes[outAt + 1] = (byte)(c & 63 | 128);
            outAt += 2;
         } else {
            bytes[outAt] = (byte)(c >> 12 & 15 | 224);
            bytes[outAt + 1] = (byte)(c >> 6 & 63 | 128);
            bytes[outAt + 2] = (byte)(c & 63 | 128);
            outAt += 3;
         }
      }

      byte[] result = new byte[outAt];
      System.arraycopy(bytes, 0, result, 0, outAt);
      return result;
   }

   public static String utf8BytesToString(byte[] bytes, int start, int length) {
      if (tempBuffer == null || tempBuffer.length < length) {
         tempBuffer = new char[length];
      }

      char[] chars = tempBuffer;
      int outAt = 0;

      for(int at = start; length > 0; ++outAt) {
         int v0 = bytes[at] & 255;
         char out;
         int v1;
         int v2;
         switch(v0 >> 4) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
            --length;
            if (v0 == 0) {
               return throwBadUtf8(v0, at);
            }

            out = (char)v0;
            ++at;
            break;
         case 8:
         case 9:
         case 10:
         case 11:
         default:
            return throwBadUtf8(v0, at);
         case 12:
         case 13:
            length -= 2;
            if (length < 0) {
               return throwBadUtf8(v0, at);
            }

            v1 = bytes[at + 1] & 255;
            if ((v1 & 192) != 128) {
               return throwBadUtf8(v1, at + 1);
            }

            v2 = (v0 & 31) << 6 | v1 & 63;
            if (v2 != 0 && v2 < 128) {
               return throwBadUtf8(v1, at + 1);
            }

            out = (char)v2;
            at += 2;
            break;
         case 14:
            length -= 3;
            if (length < 0) {
               return throwBadUtf8(v0, at);
            }

            v1 = bytes[at + 1] & 255;
            if ((v1 & 192) != 128) {
               return throwBadUtf8(v1, at + 1);
            }

            v2 = bytes[at + 2] & 255;
            if ((v1 & 192) != 128) {
               return throwBadUtf8(v2, at + 2);
            }

            int value = (v0 & 15) << 12 | (v1 & 63) << 6 | v2 & 63;
            if (value < 2048) {
               return throwBadUtf8(v2, at + 2);
            }

            out = (char)value;
            at += 3;
         }

         chars[outAt] = out;
      }

      return new String(chars, 0, outAt);
   }

   private static String throwBadUtf8(int value, int offset) {
      throw new IllegalArgumentException("bad utf-8 byte " + String.format("%02x", value) + " at offset " + String.format("%08x", offset));
   }

   public static void writeEscapedChar(Writer writer, char c) throws IOException {
      if (c >= ' ' && c < 127) {
         if (c == '\'' || c == '"' || c == '\\') {
            writer.write(92);
         }

         writer.write(c);
      } else {
         if (c <= 127) {
            switch(c) {
            case '\t':
               writer.write("\\t");
               return;
            case '\n':
               writer.write("\\n");
               return;
            case '\u000b':
            case '\f':
            default:
               break;
            case '\r':
               writer.write("\\r");
               return;
            }
         }

         writer.write("\\u");
         writer.write(Character.forDigit(c >> 12, 16));
         writer.write(Character.forDigit(c >> 8 & 15, 16));
         writer.write(Character.forDigit(c >> 4 & 15, 16));
         writer.write(Character.forDigit(c & 15, 16));
      }
   }

   public static void writeEscapedString(Writer writer, String value) throws IOException {
      for(int i = 0; i < value.length(); ++i) {
         char c = value.charAt(i);
         if (c >= ' ' && c < 127) {
            if (c == '\'' || c == '"' || c == '\\') {
               writer.write(92);
            }

            writer.write(c);
         } else {
            if (c <= 127) {
               switch(c) {
               case '\t':
                  writer.write("\\t");
                  continue;
               case '\n':
                  writer.write("\\n");
                  continue;
               case '\u000b':
               case '\f':
               default:
                  break;
               case '\r':
                  writer.write("\\r");
                  continue;
               }
            }

            writer.write("\\u");
            writer.write(Character.forDigit(c >> 12, 16));
            writer.write(Character.forDigit(c >> 8 & 15, 16));
            writer.write(Character.forDigit(c >> 4 & 15, 16));
            writer.write(Character.forDigit(c & 15, 16));
         }
      }

   }

   public static String escapeString(String value) {
      int len = value.length();
      StringBuilder sb = new StringBuilder(len * 3 / 2);

      for(int i = 0; i < len; ++i) {
         char c = value.charAt(i);
         if (c >= ' ' && c < 127) {
            if (c == '\'' || c == '"' || c == '\\') {
               sb.append('\\');
            }

            sb.append(c);
         } else {
            if (c <= 127) {
               switch(c) {
               case '\t':
                  sb.append("\\t");
                  continue;
               case '\n':
                  sb.append("\\n");
                  continue;
               case '\u000b':
               case '\f':
               default:
                  break;
               case '\r':
                  sb.append("\\r");
                  continue;
               }
            }

            sb.append("\\u");
            sb.append(Character.forDigit(c >> 12, 16));
            sb.append(Character.forDigit(c >> 8 & 15, 16));
            sb.append(Character.forDigit(c >> 4 & 15, 16));
            sb.append(Character.forDigit(c & 15, 16));
         }
      }

      return sb.toString();
   }
}
