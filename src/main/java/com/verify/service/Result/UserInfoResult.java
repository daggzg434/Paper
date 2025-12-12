package com.verify.service.Result;

public class UserInfoResult {
   public int code;
   public String msg;
   public UserInfoResult.data data;

   public UserInfoResult.data getData() {
      return this.data;
   }

   public void setData(UserInfoResult.data data) {
      this.data = data;
   }

   public UserInfoResult(int code, String msg) {
      this.code = code;
      this.msg = msg;
   }

   public int getCode() {
      return this.code;
   }

   public void setCode(int code) {
      this.code = code;
   }

   public String getMsg() {
      return this.msg;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }

   public static class data {
      public String token;

      public data(String token) {
         this.token = token;
      }

      public String getToken() {
         return this.token;
      }

      public void setToken(String token) {
         this.token = token;
      }
   }
}
