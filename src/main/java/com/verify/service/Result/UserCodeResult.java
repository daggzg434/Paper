package com.verify.service.Result;

import com.verify.service.Entity.UserRecode;
import java.util.List;

public class UserCodeResult extends BaseResult {
   public UserCodeResult.data data;

   public UserCodeResult(int code, String msg) {
      super(code, msg);
   }

   public static class data {
      public List<UserRecode> codelist;

      public data(List<UserRecode> codelist) {
         this.codelist = codelist;
      }

      public List<UserRecode> getCodelist() {
         return this.codelist;
      }

      public void setCodelist(List<UserRecode> codelist) {
         this.codelist = codelist;
      }
   }
}
