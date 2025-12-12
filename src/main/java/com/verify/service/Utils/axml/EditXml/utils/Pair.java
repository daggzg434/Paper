package com.verify.service.Utils.axml.EditXml.utils;

public class Pair<T1, T2> {
   public T1 first;
   public T2 second;

   public Pair(T1 t1, T2 t2) {
      this.first = t1;
      this.second = t2;
   }

   public Pair() {
   }

   public static <A, B> Pair<A, B> create(A a, B b) {
      return new Pair(a, b);
   }
}
