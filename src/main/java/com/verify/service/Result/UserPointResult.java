package com.verify.service.Result;

import com.verify.service.Entity.UserPoint;
import java.util.List;

public class UserPointResult extends BaseResult {
   public UserPointResult.data data;

   public UserPointResult(int code, String msg) {
      super(code, msg);
   }

   public static class data {
      public List<UserPoint> pointlist;

      public data(List<UserPoint> pointlist) {
         this.pointlist = pointlist;
      }

      public List<UserPoint> getPointlist() {
         return this.pointlist;
      }

      public void setPointlist(List<UserPoint> pointlist) {
         this.pointlist = pointlist;
      }
   }
}
