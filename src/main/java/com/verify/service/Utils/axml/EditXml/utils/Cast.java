package com.verify.service.Utils.axml.EditXml.utils;

public class Cast {
   public static final CharSequence toCharSequence(String string) {
      return string == null ? null : new CSString(string);
   }
}
