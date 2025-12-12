package com.verify.service.Result;

import com.verify.service.Entity.UserAppsCheck;
import java.util.List;

public class UserAppsCheckResult extends BaseResult {
   public UserAppsCheckResult(int code, String msg) {
      super(code, msg);
   }

   public UserAppsCheckResult(int code, String msg, Object data) {
      super(code, msg, data);
   }

   public static class data {
      public List<UserAppsCheck> AppsList;

      public List<UserAppsCheck> getAppsList() {
         return this.AppsList;
      }

      public void setAppsList(List<UserAppsCheck> appsList) {
         this.AppsList = appsList;
      }
   }
}
