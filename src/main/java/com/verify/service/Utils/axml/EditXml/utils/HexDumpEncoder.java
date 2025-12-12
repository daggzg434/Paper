package com.verify.service.Utils.axml.EditXml.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class HexDumpEncoder {
   static final HexEncoder encoder = new HexEncoder();

   public static String encode(byte[] data) {
      ByteArrayOutputStream baos = null;

      String var19;
      try {
         baos = new ByteArrayOutputStream();
         encoder.encode(data, 0, data.length, baos);
         byte[] hex = baos.toByteArray();
         StringBuilder hexDumpOut = new StringBuilder();
         StringBuilder hexOut = new StringBuilder();
         StringBuilder chrOut = new StringBuilder();

         for(int i = 0; i < hex.length; i += 32) {
            int max = Math.min(i + 32, hex.length);
            hexOut.setLength(0);
            chrOut.setLength(0);
            hexOut.append(format08x(i / 2)).append(':').append(' ');

            int k;
            for(k = i; k < max; k += 2) {
               hexOut.append((char)hex[k]);
               hexOut.append((char)hex[k + 1]);
               if ((k + 2) % 4 == 0) {
                  hexOut.append(' ');
               }

               int dataChar = data[k / 2];
               if (dataChar >= 32 && dataChar < 127) {
                  chrOut.append((char)dataChar);
               } else {
                  chrOut.append('.');
               }
            }

            hexDumpOut.append(hexOut.toString());

            for(k = hexOut.length(); k < 50; ++k) {
               hexDumpOut.append(' ');
            }

            hexDumpOut.append("  ");
            hexDumpOut.append(chrOut);
            hexDumpOut.append('\n');
         }

         var19 = hexDumpOut.toString();
      } catch (IOException var17) {
         throw new IllegalStateException(var17.getClass().getName() + ": " + var17.getMessage());
      } finally {
         try {
            if (baos != null) {
               baos.close();
            }
         } catch (Exception var16) {
         }

      }

      return var19;
   }

   private static String format08x(int value) {
      char[] buf = new char[32];
      int shift = true;
      int charPos = 32;
      int fillLength = true;
      char fillChar = true;
      int radix = true;
      boolean var7 = true;

      do {
         --charPos;
         buf[charPos] = HexEncoder.digits[value & 15];
         value >>>= 4;
      } while(value != 0);

      char[] c = new char[8];
      char[] s = Arrays.copyOfRange(buf, charPos, 32);
      int len = s.length;
      int fl = 8 - len;

      for(int i = 0; i < fl; ++i) {
         c[i] = '0';
      }

      System.arraycopy(s, 0, c, fl, len);
      return new String(c);
   }
}
