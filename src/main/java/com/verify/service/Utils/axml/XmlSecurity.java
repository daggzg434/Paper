package com.verify.service.Utils.axml;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlSecurity {
   private static DocumentBuilderFactory secureDbf = null;

   private XmlSecurity() {
   }

   public static DocumentBuilderFactory getSecureDbf() throws ParserConfigurationException {
      Class var0 = XmlSecurity.class;
      synchronized(XmlSecurity.class) {
         if (secureDbf == null) {
            secureDbf = DocumentBuilderFactory.newInstance();
         }
      }

      return secureDbf;
   }
}
