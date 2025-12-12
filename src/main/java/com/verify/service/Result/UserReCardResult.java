package com.verify.service.Result;

import com.verify.service.Entity.UserRecard;
import java.util.List;

public class UserReCardResult extends BaseResult {
   public UserReCardResult.data data;

   public UserReCardResult(int code, String msg) {
      super(code, msg);
   }

   public static class data {
      public List<UserRecard> recardlist;

      public data(List<UserRecard> recardlist) {
         this.recardlist = recardlist;
      }

      public List<UserRecard> getRecardlist() {
         return this.recardlist;
      }

      public void setRecardlist(List<UserRecard> recardlist) {
         this.recardlist = recardlist;
      }
   }
}
