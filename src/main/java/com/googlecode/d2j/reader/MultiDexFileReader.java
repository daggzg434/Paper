package com.googlecode.d2j.reader;

import com.googlecode.d2j.util.zip.AccessBufByteArrayOutputStream;
import com.googlecode.d2j.util.zip.ZipEntry;
import com.googlecode.d2j.util.zip.ZipFile;
import com.googlecode.d2j.visitors.DexFileVisitor;
import com.verify.service.Utils.InputToByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class MultiDexFileReader implements BaseDexFileReader {
   private final List<DexFileReader> readers = new ArrayList();
   private final List<MultiDexFileReader.Item> items = new ArrayList();

   private static byte[] getBytes(String filePath) {
      byte[] buffer = null;

      try {
         File file = new File(filePath);
         FileInputStream fis = new FileInputStream(file);
         ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
         byte[] b = new byte[1000];

         int n;
         while((n = fis.read(b)) != -1) {
            bos.write(b, 0, n);
         }

         fis.close();
         bos.close();
         buffer = bos.toByteArray();
      } catch (FileNotFoundException var7) {
         var7.printStackTrace();
      } catch (IOException var8) {
         var8.printStackTrace();
      }

      return buffer;
   }

   public MultiDexFileReader(Collection<DexFileReader> readers) {
      this.readers.addAll(readers);
      this.init();
   }

   private static byte[] toByteArray(InputStream is) throws IOException {
      AccessBufByteArrayOutputStream out = new AccessBufByteArrayOutputStream();
      byte[] buff = new byte[1024];

      for(int c = is.read(buff); c > 0; c = is.read(buff)) {
         out.write(buff, 0, c);
      }

      return out.getBuf();
   }

   public static BaseDexFileReader open(String file) throws IOException {
      byte[] data = getBytes(file);
      if (data.length < 3) {
         throw new IOException("File too small to be a dex/zip");
      } else if ("dex".equals(new String(data, 0, 3, StandardCharsets.ISO_8859_1))) {
         return new DexFileReader(data);
      } else if ("PK".equals(new String(data, 0, 2, StandardCharsets.ISO_8859_1))) {
         TreeMap<String, DexFileReader> dexFileReaders = new TreeMap();
         ZipFile zipFile = new ZipFile(data);
         Throwable var4 = null;

         try {
            Iterator var5 = zipFile.entries().iterator();

            while(var5.hasNext()) {
               ZipEntry e = (ZipEntry)var5.next();
               String entryName = e.getName();
               if (entryName.startsWith("classes") && entryName.endsWith(".dex") && !dexFileReaders.containsKey(entryName)) {
                  dexFileReaders.put(entryName, new DexFileReader(toByteArray(zipFile.getInputStream(e))));
               }
            }
         } catch (Throwable var15) {
            var4 = var15;
            throw var15;
         } finally {
            if (zipFile != null) {
               if (var4 != null) {
                  try {
                     zipFile.close();
                  } catch (Throwable var14) {
                     var4.addSuppressed(var14);
                  }
               } else {
                  zipFile.close();
               }
            }

         }

         if (dexFileReaders.size() == 0) {
            throw new IOException("Can not find classes.dex in zip file");
         } else {
            return (BaseDexFileReader)(dexFileReaders.size() == 1 ? (BaseDexFileReader)dexFileReaders.firstEntry().getValue() : new MultiDexFileReader(dexFileReaders.values()));
         }
      } else {
         throw new IOException("the src file not a .dex or zip file");
      }
   }

   public static BaseDexFileReader open(InputStream file) throws IOException {
      byte[] data = InputToByte.toByte(file);
      return new DexFileReader(data);
   }

   void init() {
      Set<String> classes = new HashSet();
      Iterator var2 = this.readers.iterator();

      while(var2.hasNext()) {
         DexFileReader reader = (DexFileReader)var2.next();
         List<String> classNames = reader.getClassNames();

         for(int i = 0; i < classNames.size(); ++i) {
            String className = (String)classNames.get(i);
            if (classes.add(className)) {
               this.items.add(new MultiDexFileReader.Item(i, reader, className));
            }
         }
      }

   }

   public int getDexVersion() {
      int max = 3158837;
      Iterator var2 = this.readers.iterator();

      while(var2.hasNext()) {
         DexFileReader r = (DexFileReader)var2.next();
         int v = r.getDexVersion();
         if (v > max) {
            max = v;
         }
      }

      return max;
   }

   public void accept(DexFileVisitor dv) {
      this.accept(dv, 0);
   }

   public List<String> getClassNames() {
      return new AbstractList<String>() {
         public String get(int index) {
            return ((MultiDexFileReader.Item)MultiDexFileReader.this.items.get(index)).className;
         }

         public int size() {
            return MultiDexFileReader.this.items.size();
         }
      };
   }

   public void accept(DexFileVisitor dv, int config) {
      int size = this.items.size();

      for(int i = 0; i < size; ++i) {
         this.accept(dv, i, config);
      }

   }

   public void accept(DexFileVisitor dv, int classIdx, int config) {
      MultiDexFileReader.Item item = (MultiDexFileReader.Item)this.items.get(classIdx);
      item.reader.accept(dv, item.idx, config);
   }

   static class Item {
      int idx;
      DexFileReader reader;
      String className;

      public Item(int i, DexFileReader reader, String className) {
         this.idx = i;
         this.reader = reader;
         this.className = className;
      }
   }
}
