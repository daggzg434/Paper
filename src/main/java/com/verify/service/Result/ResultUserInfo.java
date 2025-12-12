package com.verify.service.Result;

public class ResultUserInfo {
   public int code;
   public String msg;
   public String username;
   public String vip_time;
   public int soft_count;
   public int inject_count;
   public int valid_invitation;
   public String invitation;

   public ResultUserInfo(int code, String msg) {
      this.code = code;
      this.msg = msg;
   }

   public ResultUserInfo(int code, String msg, String username, String vip_time, int soft_count, int inject_count, int valid_invitation, String invitation) {
      this.code = code;
      this.msg = msg;
      this.username = username;
      this.vip_time = vip_time;
      this.soft_count = soft_count;
      this.inject_count = inject_count;
      this.valid_invitation = valid_invitation;
      this.invitation = invitation;
   }

   public int getValid_invitation() {
      return this.valid_invitation;
   }

   public void setValid_invitation(int valid_invitation) {
      this.valid_invitation = valid_invitation;
   }

   public String getInvitation() {
      return this.invitation;
   }

   public void setInvitation(String invitation) {
      this.invitation = invitation;
   }

   public int getCode() {
      return this.code;
   }

   public void setCode(int code) {
      this.code = code;
   }

   public String getMsg() {
      return this.msg;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getVip_time() {
      return this.vip_time;
   }

   public void setVip_time(String vip_time) {
      this.vip_time = vip_time;
   }

   public int getSoft_count() {
      return this.soft_count;
   }

   public void setSoft_count(int soft_count) {
      this.soft_count = soft_count;
   }

   public int getInject_count() {
      return this.inject_count;
   }

   public void setInject_count(int inject_count) {
      this.inject_count = inject_count;
   }
}
