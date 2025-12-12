package com.googlecode.dex2jar.ir.ts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class UniqueQueue<T> extends LinkedList<T> {
   Set<T> set = new HashSet();

   public boolean addAll(Collection<? extends T> c) {
      boolean result = false;
      Iterator var3 = c.iterator();

      while(var3.hasNext()) {
         T t = var3.next();
         if (this.add(t)) {
            result = true;
         }
      }

      return result;
   }

   public boolean add(T t) {
      if (this.set.add(t)) {
         super.add(t);
      }

      return true;
   }

   public T poll() {
      T t = super.poll();
      this.set.remove(t);
      return t;
   }

   public T pop() {
      T t = super.pop();
      this.set.remove(t);
      return t;
   }
}
