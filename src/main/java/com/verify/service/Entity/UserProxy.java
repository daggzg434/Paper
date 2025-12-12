package com.verify.service.Entity;

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
   name = "pangolin_proxy"
)
public class UserProxy {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "username",
      columnDefinition = "varchar(20) default ''",
      nullable = false
   )
   public String username;
   @Column(
      name = "password",
      columnDefinition = "varchar(20) default ''",
      nullable = false
   )
   public String password;
   @Column(
      name = "money",
      columnDefinition = "decimal(10,2) default 0.0",
      nullable = false
   )
   public double money;
   @Column(
      name = "point_rate",
      columnDefinition = "decimal(10,2) default 0.0",
      nullable = false
   )
   public double point_rate;
   @Column(
      name = "recode_rate",
      columnDefinition = "decimal(10,2) default 0.0",
      nullable = false
   )
   public double recode_rate;
   @Column(
      name = "recard_rate",
      columnDefinition = "decimal(10,2) default 0.0",
      nullable = false
   )
   public double recard_rate;
   @Column(
      name = "soft",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String soft;
   @Column(
      name = "user_id",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int userid;
   @Column(
      name = "is_addproxy",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int is_addproxy;
   @Column(
      name = "proxy_id",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int proxyid;
}
