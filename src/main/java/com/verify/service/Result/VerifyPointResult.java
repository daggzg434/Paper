package com.verify.service.Result;

public class VerifyPointResult extends BaseResult {
   public String token;
   public int card_type;
   public int point;

   public VerifyPointResult(int code, String msg) {
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

   public int getPoint() {
      return this.point;
   }

   public void setPoint(int point) {
      this.point = point;
   }
}
