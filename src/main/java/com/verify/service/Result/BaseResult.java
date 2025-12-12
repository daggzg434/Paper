package com.verify.service.Result;

public class BaseResult {
   public int code;
   public String msg;
   public Object data;

   public Object getData() {
      return this.data;
   }

   public void setData(Object data) {
      this.data = data;
   }

   public BaseResult(int code, String msg) {
      this.code = code;
      this.msg = msg;
   }

   public BaseResult(int code, String msg, Object data) {
      this.code = code;
      this.msg = msg;
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
}
