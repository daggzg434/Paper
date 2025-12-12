package com.verify.service.Result;

public class QueryResult extends BaseResult {
   public int card_type;

   public QueryResult(int code, String msg) {
      super(code, msg);
   }

   public int getCard_type() {
      return this.card_type;
   }

   public void setCard_type(int card_type) {
      this.card_type = card_type;
   }
}
