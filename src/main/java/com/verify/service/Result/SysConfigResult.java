package com.verify.service.Result;

import com.verify.service.Entity.SysConfig;

public class SysConfigResult {
   public int code;
   public String msg;
   public SysConfig sysConfig;

   public SysConfigResult(int code, String msg, SysConfig sysConfig) {
      this.code = code;
      this.msg = msg;
      this.sysConfig = sysConfig;
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

   public SysConfig getSysConfig() {
      return this.sysConfig;
   }

   public void setSysConfig(SysConfig sysConfig) {
      this.sysConfig = sysConfig;
   }
}
