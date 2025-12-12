package com.verify.service.Utils.axml.EditXml.utils;

import java.nio.charset.Charset;

public class Charsets {
   /** @deprecated */
   @Deprecated
   public static final Charset UTF_8 = Charset.forName("UTF-8");

   public static Charset toCharset(final Charset charset) {
      return charset == null ? Charset.defaultCharset() : charset;
   }

   public static Charset toCharset(final String charset) {
      return charset == null ? Charset.defaultCharset() : Charset.forName(charset);
   }
}
