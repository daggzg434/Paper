package com.verify.service.Result;

public class VerifyRecardResult extends BaseResult {
   public String time;

   public String getTime() {
      return this.time;
   }

   public void setTime(String time) {
      this.time = time;
   }

   public VerifyRecardResult(int code, String msg) {
      super(code, msg);
   }
}
