package com.verify.service.Result;

import java.util.Date;

public class VerifyRecodeResult extends BaseResult {
   public String token;
   public int card_type;
   public Date time;

   public VerifyRecodeResult(int code, String msg) {
      super(code, msg);
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public int getCard_type() {
      return this.card_type;
   }

   public void setCard_type(int card_type) {
      this.card_type = card_type;
   }

   public Date getTime() {
      return this.time;
   }

   public void setTime(Date time) {
      this.time = time;
   }
}
