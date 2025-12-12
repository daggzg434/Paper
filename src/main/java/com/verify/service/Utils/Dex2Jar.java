package com.verify.service.Utils;

import com.googlecode.d2j.dex.Dex2jar;
import com.googlecode.d2j.reader.BaseDexFileReader;
import com.googlecode.d2j.reader.MultiDexFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Dex2Jar {
   private static boolean reuseReg = false;
   private static boolean debugInfo = false;
   private static boolean printIR = false;
   private static boolean optmizeSynchronized = false;
   private static boolean skipExceptions = false;
   private static boolean noCode = false;

   public static void from(File in, File out) throws IOException {
      long baseTS = System.currentTimeMillis();
      BaseDexFileReader reader = MultiDexFileReader.open(in.getAbsolutePath());
      Dex2jar.from(reader).reUseReg(reuseReg).topoLogicalSort().skipDebug(!debugInfo).optimizeSynchronized(optmizeSynchronized).printIR(printIR).noCode(noCode).skipExceptions(skipExceptions).to(out);
      long endTS = System.currentTimeMillis();
      System.out.println(String.format("Dex2Jar耗时->%.2f", (float)(endTS - baseTS) / 1000.0F));
   }

   public static void from(InputStream in, File out) throws IOException {
      long baseTS = System.currentTimeMillis();
      BaseDexFileReader reader = MultiDexFileReader.open(in);
      Dex2jar.from(reader).reUseReg(reuseReg).topoLogicalSort().skipDebug(!debugInfo).optimizeSynchronized(optmizeSynchronized).printIR(printIR).noCode(noCode).skipExceptions(skipExceptions).to(out);
      long endTS = System.currentTimeMillis();
      System.out.println(String.format("Dex2Jar耗时->%.2f", (float)(endTS - baseTS) / 1000.0F));
   }

   public static void from(String in, String out) throws IOException {
      from(new File(in), new File(out));
   }
}
