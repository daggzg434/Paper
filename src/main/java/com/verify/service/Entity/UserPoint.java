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
   name = "pangolin_point"
)
public class UserPoint {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "card"
   )
   public String card;
   @Column(
      name = "point"
   )
   public int point = 0;
   @Column(
      name = "use_time"
   )
   public Date use_time;
   @Column(
      name = "create_time"
   )
   public Date create_time = new Date();
   @Column(
      name = "soft_appkey"
   )
   public String softappkey;
   @Column(
      name = "mac"
   )
   public String mac;
   @Column(
      name = "user_id"
   )
   public int userid;
   @Column(
      name = "frozen"
   )
   public int frozen = 0;
   @Column(
      name = "mark"
   )
   public String mark;
   @Column(
      name = "token"
   )
   public String token;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getCard() {
      return this.card;
   }

   public void setCard(String card) {
      this.card = card;
   }

   public int getPoint() {
      return this.point;
   }

   public void setPoint(int point) {
      this.point = point;
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

   public String getSoftappkey() {
      return this.softappkey;
   }

   public void setSoftappkey(String softappkey) {
      this.softappkey = softappkey;
   }

   public String getMac() {
      return this.mac;
   }

   public void setMac(String mac) {
      this.mac = mac;
   }

   public int getUserid() {
      return this.userid;
   }

   public void setUserid(int userid) {
      this.userid = userid;
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

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }
}
