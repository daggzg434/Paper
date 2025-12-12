package com.verify.service.Result;

import com.verify.service.Entity.UserSoft;
import java.util.List;

public class UserSoftResult {
   public int code;
   public String msg;
   public UserSoftResult.data data;

   public UserSoftResult(int code, String msg) {
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

   public UserSoftResult.data getData() {
      return this.data;
   }

   public void setData(UserSoftResult.data data) {
      this.data = data;
   }

   public static class data {
      public List<UserSoft> softList;

      public data(List<UserSoft> softList) {
         this.softList = softList;
      }

      public List<UserSoft> getSoftList() {
         return this.softList;
      }

      public void setSoftList(List<UserSoft> softList) {
         this.softList = softList;
      }
   }
}
