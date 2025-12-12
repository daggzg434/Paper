package com.googlecode.d2j.reader;

enum InstructionIndexType {
   kIndexUnknown,
   kIndexNone,
   kIndexVaries,
   kIndexTypeRef,
   kIndexStringRef,
   kIndexMethodRef,
   kIndexFieldRef,
   kIndexInlineMethod,
   kIndexVtableOffset,
   kIndexFieldOffset,
   kIndexMethodAndProtoRef,
   kIndexCallSiteRef;
}
