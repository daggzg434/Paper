package com.verify.service.Utils.axml.AutoXml.xml.decode;

public interface XmlPullParser {
   String NO_NAMESPACE = "";
   int START_DOCUMENT = 0;
   int END_DOCUMENT = 1;
   int START_TAG = 2;
   int END_TAG = 3;
   int TEXT = 4;
   int CDSECT = 5;
   int ENTITY_REF = 6;
   int IGNORABLE_WHITESPACE = 7;
   int PROCESSING_INSTRUCTION = 8;
   int COMMENT = 9;
   int DOCDECL = 10;
   String[] TYPES = new String[]{"START_DOCUMENT", "END_DOCUMENT", "START_TAG", "END_TAG", "TEXT", "CDSECT", "ENTITY_REF", "IGNORABLE_WHITESPACE", "PROCESSING_INSTRUCTION", "COMMENT", "DOCDECL"};
   String FEATURE_PROCESS_NAMESPACES = "http://xmlpull.org/v1/doc/features.html#process-namespaces";
   String FEATURE_REPORT_NAMESPACE_ATTRIBUTES = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes";
   String FEATURE_PROCESS_DOCDECL = "http://xmlpull.org/v1/doc/features.html#process-docdecl";
   String FEATURE_VALIDATION = "http://xmlpull.org/v1/doc/features.html#validation";
}
