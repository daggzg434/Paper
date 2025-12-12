package com.verify.service.Entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
   name = "pangolin_softuser"
)
public class SoftUser implements Serializable {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "username"
   )
   public String username;
   @Column(
      name = "password"
   )
   public String password;
   @Column(
      name = "email"
   )
   public String email;
   @Column(
      name = "reg_time"
   )
   public Date reg_time = new Date();
   @Column(
      name = "expire_time"
   )
   public Date expiretime = new Date(System.currentTimeMillis());
   @Column(
      name = "login_count"
   )
   public int login_count = 0;
   @Column(
      name = "currentlogin_ip"
   )
   public String currentlogin_ip;
   @Column(
      name = "currentlogin_time"
   )
   public Date currentlogin_time;
   @Column(
      name = "lastlogin_ip"
   )
   public String lastlogin_ip;
   @Column(
      name = "lastlogin_time"
   )
   public Date lastlogin_time;
   @Column(
      name = "mac"
   )
   public String mac;
   @Column(
      name = "soft_appkey"
   )
   public String softappkey;
   @Column(
      name = "frozen"
   )
   public int frozen;
   @Column(
      name = "tokne"
   )
   public String token;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getEmail() {
      return this.email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public Date getReg_time() {
      return this.reg_time;
   }

   public void setReg_time(Date reg_time) {
      this.reg_time = reg_time;
   }

   public Date getExpire_time() {
      return this.expiretime;
   }

   public void setExpire_time(Date expire_time) {
      this.expiretime = expire_time;
   }

   public int getLogin_count() {
      return this.login_count;
   }

   public void setLogin_count(int login_count) {
      this.login_count = login_count;
   }

   public String getCurrentlogin_ip() {
      return this.currentlogin_ip;
   }

   public void setCurrentlogin_ip(String currentlogin_ip) {
      this.currentlogin_ip = currentlogin_ip;
   }

   public Date getCurrentlogin_time() {
      return this.currentlogin_time;
   }

   public void setCurrentlogin_time(Date currentlogin_time) {
      this.currentlogin_time = currentlogin_time;
   }

   public String getLastlogin_ip() {
      return this.lastlogin_ip;
   }

   public void setLastlogin_ip(String lastlogin_ip) {
      this.lastlogin_ip = lastlogin_ip;
   }

   public Date getLastlogin_time() {
      return this.lastlogin_time;
   }

   public void setLastlogin_time(Date lastlogin_time) {
      this.lastlogin_time = lastlogin_time;
   }

   public String getMac() {
      return this.mac;
   }

   public void setMac(String mac) {
      this.mac = mac;
   }

   public String getSoftappkey() {
      return this.softappkey;
   }

   public void setSoftappkey(String softappkey) {
      this.softappkey = softappkey;
   }

   public int getFrozen() {
      return this.frozen;
   }

   public void setFrozen(int frozen) {
      this.frozen = frozen;
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }
}
