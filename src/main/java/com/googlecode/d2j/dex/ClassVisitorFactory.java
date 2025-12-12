package com.googlecode.d2j.dex;

import org.objectweb.asm.ClassVisitor;

public interface ClassVisitorFactory {
   ClassVisitor create(String classInternalName);
}
