package com.googlecode.d2j.dex;

import com.googlecode.d2j.converter.IR2JConverter;
import com.googlecode.d2j.node.DexFileNode;
import com.googlecode.d2j.node.DexMethodNode;
import com.googlecode.d2j.reader.BaseDexFileReader;
import com.googlecode.d2j.reader.DexFileReader;
import com.googlecode.d2j.reader.zip.ZipUtil;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.stmt.LabelStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class Dex2jar {
   private DexExceptionHandler exceptionHandler;
   private final BaseDexFileReader reader;
   private int readerConfig;
   private int v3Config;
   private JarOutputStream zos;

   public static Dex2jar from(byte[] in) throws IOException {
      return from((BaseDexFileReader)(new DexFileReader(ZipUtil.readDex(in))));
   }

   public static Dex2jar from(ByteBuffer in) throws IOException {
      return from((BaseDexFileReader)(new DexFileReader(in)));
   }

   public static Dex2jar from(BaseDexFileReader reader) {
      return new Dex2jar(reader);
   }

   public static Dex2jar from(InputStream in) throws IOException {
      return from((BaseDexFileReader)(new DexFileReader(in)));
   }

   private Dex2jar(BaseDexFileReader reader) {
      this.reader = reader;
      this.readerConfig |= 1;
   }

   private void doTranslate(final File dist) throws IOException {
      DexFileNode fileNode = new DexFileNode();

      try {
         this.reader.accept(fileNode, this.readerConfig | 32);
      } catch (Exception var4) {
         this.exceptionHandler.handleFileException(var4);
      }

      ClassVisitorFactory cvf = new ClassVisitorFactory() {
         public ClassVisitor create(final String name) {
            final ClassWriter cw = new ClassWriter(1);
            final LambadaNameSafeClassAdapter rca = new LambadaNameSafeClassAdapter(cw);
            return new ClassVisitor(327680, rca) {
               public void visitEnd() {
                  super.visitEnd();
                  String className = rca.getClassName();

                  byte[] data;
                  try {
                     data = cw.toByteArray();
                  } catch (Exception var5) {
                     Dex2jar.this.exceptionHandler.handleFileException(var5);
                     return;
                  }

                  try {
                     Dex2jar.this.zos.putNextEntry(new ZipEntry(className + ".class"));
                     Dex2jar.this.zos.write(data);
                  } catch (Exception var4) {
                     var4.printStackTrace(System.err);
                  }

               }
            };
         }
      };
      (new ExDex2Asm(this.exceptionHandler) {
         public void convertCode(DexMethodNode methodNode, MethodVisitor mv) {
            if ((Dex2jar.this.readerConfig & 4) == 0 || !methodNode.method.getName().equals("<clinit>")) {
               super.convertCode(methodNode, mv);
            }
         }

         public void optimize(IrMethod irMethod) {
            T_cleanLabel.transform(irMethod);
            if (0 != (Dex2jar.this.v3Config & 2)) {
            }

            T_deadCode.transform(irMethod);
            T_removeLocal.transform(irMethod);
            T_removeConst.transform(irMethod);
            T_zero.transform(irMethod);
            if (T_npe.transformReportChanged(irMethod)) {
               T_deadCode.transform(irMethod);
               T_removeLocal.transform(irMethod);
               T_removeConst.transform(irMethod);
            }

            T_new.transform(irMethod);
            T_fillArray.transform(irMethod);
            T_agg.transform(irMethod);
            T_multiArray.transform(irMethod);
            T_voidInvoke.transform(irMethod);
            if (0 != (Dex2jar.this.v3Config & 4)) {
               int i = 0;
               Iterator var3 = irMethod.stmts.iterator();

               while(var3.hasNext()) {
                  Stmt p = (Stmt)var3.next();
                  if (p.st == Stmt.ST.LABEL) {
                     LabelStmt labelStmt = (LabelStmt)p;
                     labelStmt.displayName = "L" + i++;
                  }
               }

               System.out.println(irMethod);
            }

            T_type.transform(irMethod);
            T_unssa.transform(irMethod);
            T_ir2jRegAssign.transform(irMethod);
            T_trimEx.transform(irMethod);
         }

         public void ir2j(IrMethod irMethod, MethodVisitor mv) {
            (new IR2JConverter(0 != (8 & Dex2jar.this.v3Config))).convert(irMethod, mv);
         }
      }).convertDex(fileNode, cvf);
   }

   public DexExceptionHandler getExceptionHandler() {
      return this.exceptionHandler;
   }

   public BaseDexFileReader getReader() {
      return this.reader;
   }

   public Dex2jar reUseReg(boolean b) {
      if (b) {
         this.v3Config |= 1;
      } else {
         this.v3Config &= -2;
      }

      return this;
   }

   public Dex2jar topoLogicalSort(boolean b) {
      if (b) {
         this.v3Config |= 2;
      } else {
         this.v3Config &= -3;
      }

      return this;
   }

   public Dex2jar noCode(boolean b) {
      if (b) {
         this.readerConfig |= 132;
      } else {
         this.readerConfig &= -133;
      }

      return this;
   }

   public Dex2jar optimizeSynchronized(boolean b) {
      if (b) {
         this.v3Config |= 8;
      } else {
         this.v3Config &= -9;
      }

      return this;
   }

   public Dex2jar printIR(boolean b) {
      if (b) {
         this.v3Config |= 4;
      } else {
         this.v3Config &= -5;
      }

      return this;
   }

   public Dex2jar reUseReg() {
      this.v3Config |= 1;
      return this;
   }

   public Dex2jar optimizeSynchronized() {
      this.v3Config |= 8;
      return this;
   }

   public Dex2jar printIR() {
      this.v3Config |= 4;
      return this;
   }

   public Dex2jar topoLogicalSort() {
      this.v3Config |= 2;
      return this;
   }

   public void setExceptionHandler(DexExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
   }

   public Dex2jar skipDebug(boolean b) {
      if (b) {
         this.readerConfig |= 1;
      } else {
         this.readerConfig &= -2;
      }

      return this;
   }

   public Dex2jar skipDebug() {
      this.readerConfig |= 1;
      return this;
   }

   public void to(File file) throws IOException {
      this.zos = new JarOutputStream(new FileOutputStream(file));
      this.doTranslate(file);
      this.zos.close();
   }

   public Dex2jar withExceptionHandler(DexExceptionHandler exceptionHandler) {
      this.exceptionHandler = exceptionHandler;
      return this;
   }

   public Dex2jar skipExceptions(boolean b) {
      if (b) {
         this.readerConfig |= 256;
      } else {
         this.readerConfig &= -257;
      }

      return this;
   }
}
