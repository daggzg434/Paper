package com.googlecode.d2j.util;

import java.util.ArrayList;
import java.util.List;

public class ArrayOut implements Out {
   int i = 0;
   public List<String> array = new ArrayList();
   public List<Integer> is = new ArrayList();

   public void push() {
      ++this.i;
   }

   public void s(String s) {
      this.is.add(this.i);
      this.array.add(s);
   }

   public void s(String format, Object... arg) {
      this.s(String.format(format, arg));
   }

   public void pop() {
      --this.i;
   }
}
