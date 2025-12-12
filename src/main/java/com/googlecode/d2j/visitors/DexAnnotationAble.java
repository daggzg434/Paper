package com.googlecode.d2j.visitors;

import com.googlecode.d2j.Visibility;

public interface DexAnnotationAble {
   DexAnnotationVisitor visitAnnotation(String name, Visibility visibility);
}
