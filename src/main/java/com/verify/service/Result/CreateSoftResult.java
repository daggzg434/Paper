package com.verify.service.Result;

public class CreateSoftResult {
   public int code;
   public String msg;
   public String uuid;

   public CreateSoftResult(int code, String msg, String uuid) {
      this.code = code;
      this.msg = msg;
      this.uuid = uuid;
   }
}
