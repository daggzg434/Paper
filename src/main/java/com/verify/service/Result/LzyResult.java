package com.verify.service.Result;

public class LzyResult extends BaseResult {
   public LzyResult.data data;

   public LzyResult.data getData() {
      return this.data;
   }

   public void setData(LzyResult.data data) {
      this.data = data;
   }

   public LzyResult(int code, String msg) {
      super(code, msg);
   }

   public LzyResult(int code, String msg, LzyResult.data data) {
      super(code, msg);
      this.data = data;
   }

   public static class data {
      public int zt;
      public String dom;
      public String url;
      public int inf;
      public String dow;

      public String getDow() {
         return this.dow;
      }

      public void setDow(String dow) {
         this.dow = dow;
      }

      public int getZt() {
         return this.zt;
      }

      public void setZt(int zt) {
         this.zt = zt;
      }

      public String getDom() {
         return this.dom;
      }

      public void setDom(String dom) {
         this.dom = dom;
      }

      public String getUrl() {
         return this.url;
      }

      public void setUrl(String url) {
         this.url = url;
      }

      public int getInf() {
         return this.inf;
      }

      public void setInf(int inf) {
         this.inf = inf;
      }
   }
}
