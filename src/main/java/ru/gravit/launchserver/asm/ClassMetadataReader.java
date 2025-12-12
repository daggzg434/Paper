package ru.gravit.launchserver.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

public class ClassMetadataReader {
   private final List<JarFile> classPath;

   public ClassMetadataReader(List<JarFile> classPath) {
      this.classPath = classPath;
   }

   public List<JarFile> getCp() {
      return this.classPath;
   }

   public ClassMetadataReader() {
      this.classPath = new ArrayList();
   }

   public void acceptVisitor(byte[] classData, ClassVisitor visitor) {
      (new ClassReader(classData)).accept(visitor, 0);
   }

   public void acceptVisitor(String className, ClassVisitor visitor) throws IOException, ClassNotFoundException {
      this.acceptVisitor(this.getClassData(className), visitor);
   }

   private static byte[] read(InputStream input) throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      Throwable var2 = null;

      try {
         byte[] buffer = new byte[4096];

         for(int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
            output.write(buffer, 0, length);
         }

         byte[] var15 = output.toByteArray();
         return var15;
      } catch (Throwable var13) {
         var2 = var13;
         throw var13;
      } finally {
         if (output != null) {
            if (var2 != null) {
               try {
                  output.close();
               } catch (Throwable var12) {
                  var2.addSuppressed(var12);
               }
            } else {
               output.close();
            }
         }

      }
   }

   public byte[] getClassData(String className) throws IOException, ClassNotFoundException {
      Iterator var2 = this.classPath.iterator();

      JarFile file;
      do {
         if (!var2.hasNext()) {
            throw new ClassNotFoundException(className);
         }

         file = (JarFile)var2.next();
      } while(file.getEntry(className + ".class") == null);

      InputStream in = file.getInputStream(file.getEntry(className + ".class"));
      Throwable var5 = null;

      byte[] var6;
      try {
         var6 = read(in);
      } catch (Throwable var15) {
         var5 = var15;
         throw var15;
      } finally {
         if (in != null) {
            if (var5 != null) {
               try {
                  in.close();
               } catch (Throwable var14) {
                  var5.addSuppressed(var14);
               }
            } else {
               in.close();
            }
         }

      }

      return var6;
   }

   public String getSuperClass(String type) {
      if (type.equals("java/lang/Object")) {
         return null;
      } else {
         try {
            return this.getSuperClassASM(type);
         } catch (ClassNotFoundException | IOException var3) {
            return "java/lang/Object";
         }
      }
   }

   protected String getSuperClassASM(String type) throws IOException, ClassNotFoundException {
      ClassMetadataReader.CheckSuperClassVisitor cv = new ClassMetadataReader.CheckSuperClassVisitor();
      this.acceptVisitor((String)type, cv);
      return cv.superClassName;
   }

   public ArrayList<String> getSuperClasses(String type) {
      ArrayList<String> superclasses = new ArrayList(1);
      superclasses.add(type);

      while((type = this.getSuperClass(type)) != null) {
         superclasses.add(type);
      }

      Collections.reverse(superclasses);
      return superclasses;
   }

   public void close() {
      this.classPath.forEach((file) -> {
         try {
            file.close();
         } catch (IOException var2) {
         }

      });
   }

   private class CheckSuperClassVisitor extends ClassVisitor {
      String superClassName;

      public CheckSuperClassVisitor() {
         super(327680);
      }

      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
         this.superClassName = superName;
      }
   }
}
