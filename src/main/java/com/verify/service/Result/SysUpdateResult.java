package com.verify.service.Result;

import com.verify.service.Entity.SysUpdate;
import java.util.List;

public class SysUpdateResult {
   public int code;
   public String msg;
   public int count;
   public List<SysUpdate> data;

   public SysUpdateResult(int code, String msg, int count, List<SysUpdate> data) {
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

   public List<SysUpdate> getData() {
      return this.data;
   }

   public void setData(List<SysUpdate> data) {
      this.data = data;
   }
}
