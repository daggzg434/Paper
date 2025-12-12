package com.verify.service.Result;

import com.verify.service.Entity.SysUpdate;

public class UpdateResult extends BaseResult {
   public SysUpdate data;

   public UpdateResult(int code, String msg) {
      super(code, msg);
   }

   public UpdateResult(int code, String msg, SysUpdate data) {
      super(code, msg);
      this.data = data;
   }

   public SysUpdate getData() {
      return this.data;
   }

   public void setData(SysUpdate data) {
      this.data = data;
   }
}
