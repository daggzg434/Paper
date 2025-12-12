package com.verify.service.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
   name = "pangolin_sysrecard"
)
public class SysRecard {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "recard"
   )
   public String recard;
   @Column(
      name = "all_minutes"
   )
   public int all_minutes = 0;
   @JsonFormat(
      pattern = "yyyy-MM-dd HH:mm:ss",
      timezone = "GMT+8"
   )
   @Column(
      name = "use_time"
   )
   public Date use_time;
   @JsonFormat(
      pattern = "yyyy-MM-dd HH:mm:ss",
      timezone = "GMT+8"
   )
   @Column(
      name = "create_time"
   )
   public Date create_time = new Date();
   @Column(
      name = "username"
   )
   public String username;
   @Column(
      name = "frozen"
   )
   public int frozen = 0;
   @Column(
      name = "mark"
   )
   public String mark;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getRecard() {
      return this.recard;
   }

   public void setRecard(String recard) {
      this.recard = recard;
   }

   public int getAll_minutes() {
      return this.all_minutes;
   }

   public void setAll_minutes(int all_minutes) {
      this.all_minutes = all_minutes;
   }

   public Date getUse_time() {
      return this.use_time;
   }

   public void setUse_time(Date use_time) {
      this.use_time = use_time;
   }

   public Date getCreate_time() {
      return this.create_time;
   }

   public void setCreate_time(Date create_time) {
      this.create_time = create_time;
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public int getFrozen() {
      return this.frozen;
   }

   public void setFrozen(int frozen) {
      this.frozen = frozen;
   }

   public String getMark() {
      return this.mark;
   }

   public void setMark(String mark) {
      this.mark = mark;
   }
}
