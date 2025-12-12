package com.verify.service.Result;

import com.verify.service.Entity.UserAppsCheckSign;
import java.util.List;

public class UserAppsSignCheckResult extends BaseResult {
   public UserAppsSignCheckResult(int code, String msg) {
      super(code, msg);
   }

   public UserAppsSignCheckResult(int code, String msg, Object data) {
      super(code, msg, data);
   }

   public static class data {
      public List<UserAppsCheckSign> AppsList;

      public List<UserAppsCheckSign> getAppsList() {
         return this.AppsList;
      }

      public void setAppsList(List<UserAppsCheckSign> appsList) {
         this.AppsList = appsList;
      }
   }
}
