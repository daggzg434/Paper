package com.verify.service.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.DigestUtils;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(
   name = "pangolin_user"
)
public class UserInfo implements Serializable {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "username",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String username;
   @Column(
      name = "password",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String password;
   @Column(
      name = "email",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String email;
   @JsonFormat(
      pattern = "yyyy-MM-dd HH:mm:ss",
      timezone = "GMT+8"
   )
   @Column(
      name = "reg_time"
   )
   public Date reg_time = new Date();
   @JsonFormat(
      pattern = "yyyy-MM-dd HH:mm:ss",
      timezone = "GMT+8"
   )
   @Column(
      name = "expire_time"
   )
   public Date expire_time = new Date();
   @Column(
      name = "login_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int login_count = 0;
   @Column(
      name = "currentlogin_ip",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String currentlogin_ip;
   @JsonFormat(
      pattern = "yyyy-MM-dd HH:mm:ss",
      timezone = "GMT+8"
   )
   @Column(
      name = "currentlogin_time"
   )
   public Date currentlogin_time;
   @Column(
      name = "lastlogin_ip",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String lastlogin_ip;
   @JsonFormat(
      pattern = "yyyy-MM-dd HH:mm:ss",
      timezone = "GMT+8"
   )
   @Column(
      name = "lastlogin_time"
   )
   public Date lastlogin_time;
   @Column(
      name = "tokne",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String token;
   @Column(
      name = "inject_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int inject_count = 0;
   @Column(
      name = "invitation_code",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String invitation = DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes()).substring(0, 8);
   @Column(
      name = "valid_invitation",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int valid_invitation = 0;

   public String getInvitation() {
      return this.invitation;
   }

   public void setInvitation(String invitation) {
      this.invitation = invitation;
   }

   public int getValid_invitation() {
      return this.valid_invitation;
   }

   public void setValid_invitation(int valid_invitation) {
      this.valid_invitation = valid_invitation;
   }

   public int getInject_count() {
      return this.inject_count;
   }

   public void setInject_count(int inject_count) {
      this.inject_count = inject_count;
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return this.username;
   }

   public void setName(String name) {
      this.username = name;
   }

   public String getPass() {
      return this.password;
   }

   public void setPass(String pass) {
      this.password = pass;
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
      return this.expire_time;
   }

   public void setExpire_time(Date expire_time) {
      this.expire_time = expire_time;
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
}
