package com.verify.service.Entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(
   name = "pangolin_softstatistical"
)
public class SoftStatistical {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "appkey",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String appkey;
   @Column(
      name = "ip",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String ip;
   @Column(
      name = "time"
   )
   public Date time = new Date(System.currentTimeMillis());

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getAppkey() {
      return this.appkey;
   }

   public void setAppkey(String appkey) {
      this.appkey = appkey;
   }

   public String getIp() {
      return this.ip;
   }

   public void setIp(String ip) {
      this.ip = ip;
   }

   public Date getTime() {
      return this.time;
   }

   public void setTime(Date time) {
      this.time = time;
   }
}
