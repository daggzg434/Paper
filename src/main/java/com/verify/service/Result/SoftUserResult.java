package com.verify.service.Result;

import com.verify.service.Entity.SoftUser;
import java.util.List;

public class SoftUserResult extends BaseResult {
   public SoftUserResult.data data;

   public SoftUserResult(int code, String msg) {
      super(code, msg);
   }

   public static class data {
      public List<SoftUser> userlist;

      public data(List<SoftUser> userlist) {
         this.userlist = userlist;
      }

      public List<SoftUser> getUserlist() {
         return this.userlist;
      }

      public void setUserlist(List<SoftUser> userlist) {
         this.userlist = userlist;
      }
   }
}
