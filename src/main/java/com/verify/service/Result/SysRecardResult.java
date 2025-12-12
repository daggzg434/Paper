package com.verify.service.Result;

import com.verify.service.Entity.SysRecard;
import java.util.List;

public class SysRecardResult {
   public int code;
   public String msg;
   public int count;
   public List<SysRecard> data;

   public SysRecardResult(int code, String msg, int count, List<SysRecard> data) {
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

   public List<SysRecard> getData() {
      return this.data;
   }

   public void setData(List<SysRecard> data) {
      this.data = data;
   }
}
