package com.verify.service.Utils.axml.EditXml.utils;

public class CSString implements CharSequence {
   private String m_string;

   public CSString(String string) {
      if (string == null) {
         string = "";
      }

      this.m_string = string;
   }

   public int length() {
      return this.m_string.length();
   }

   public char charAt(int index) {
      return this.m_string.charAt(index);
   }

   public CharSequence subSequence(int start, int end) {
      return new CSString(this.m_string.substring(start, end));
   }

   public String toString() {
      return this.m_string;
   }
}
