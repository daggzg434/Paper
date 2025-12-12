package com.verify.service.Entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
   name = "pangolin_trial"
)
public class TrialInfo {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "mac"
   )
   public String mac;
   @Column(
      name = "appkey"
   )
   public String appkey;
   @Column(
      name = "has_try_count"
   )
   public int has_try_count;
   @Column(
      name = "token"
   )
   public String token;
   @Column(
      name = "last_time"
   )
   public Date last_time;

   public Date getLast_time() {
      return this.last_time;
   }

   public void setLast_time(Date last_time) {
      this.last_time = last_time;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getMac() {
      return this.mac;
   }

   public void setMac(String mac) {
      this.mac = mac;
   }

   public String getAppkey() {
      return this.appkey;
   }

   public void setAppkey(String appkey) {
      this.appkey = appkey;
   }

   public int getHas_try_count() {
      return this.has_try_count;
   }

   public void setHas_try_count(int has_try_count) {
      this.has_try_count = has_try_count;
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }
}
