package com.verify.service.Result;

import com.verify.service.Entity.UserInfo;
import java.util.List;

public class SysUserInfoResult {
   public int code;
   public String msg;
   public int count;
   public List<UserInfo> data;

   public SysUserInfoResult(int code, String msg, int count, List<UserInfo> data) {
      this.code = code;
      this.msg = msg;
      this.count = count;
      this.data = data;
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

   public int getCount() {
      return this.count;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public List<UserInfo> getData() {
      return this.data;
   }

   public void setData(List<UserInfo> data) {
      this.data = data;
   }
}
