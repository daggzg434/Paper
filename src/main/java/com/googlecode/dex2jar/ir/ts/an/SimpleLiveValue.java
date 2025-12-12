package com.googlecode.dex2jar.ir.ts.an;

import java.util.List;

public class SimpleLiveValue implements AnalyzeValue {
   public boolean used = false;
   public SimpleLiveValue parent;
   public List<SimpleLiveValue> otherParents;

   public char toRsp() {
      return (char)(this.used ? 'x' : '.');
   }
}
