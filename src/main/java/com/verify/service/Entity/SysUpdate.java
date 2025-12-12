package com.verify.service.Entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(
   name = "pangolin_sysupdate"
)
public class SysUpdate {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "version",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int version;
   @Column(
      name = "version_name",
      columnDefinition = "varchar(20) default ''",
      nullable = false
   )
   public String version_name;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "version_msg"
   )
   public String version_msg;
   @Column(
      name = "version_address",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String version_address;
   @Column(
      name = "version_state",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int version_state;
   @Column(
      name = "version_mode",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int version_mode;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getVersion() {
      return this.version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public String getVersion_name() {
      return this.version_name;
   }

   public void setVersion_name(String version_name) {
      this.version_name = version_name;
   }

   public String getVersion_msg() {
      return this.version_msg;
   }

   public void setVersion_msg(String version_msg) {
      this.version_msg = version_msg;
   }

   public String getVersion_address() {
      return this.version_address;
   }

   public void setVersion_address(String version_address) {
      this.version_address = version_address;
   }

   public int getVersion_state() {
      return this.version_state;
   }

   public void setVersion_state(int version_state) {
      this.version_state = version_state;
   }

   public int getVersion_mode() {
      return this.version_mode;
   }

   public void setVersion_mode(int version_mode) {
      this.version_mode = version_mode;
   }
}
