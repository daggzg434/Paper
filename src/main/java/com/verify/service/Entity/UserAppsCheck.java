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
   name = "pangolin_userapps"
)
public class UserAppsCheck {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @Column(
      name = "apps_name",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String apps_name;
   @Column(
      name = "apps_is_install",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int apps_is_install;
   @Column(
      name = "apps_action",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int apps_action;
   @Column(
      name = "apps_action_body",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String apps_action_body;
   @Column(
      name = "apps_action_title",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String apps_action_title;
   @Column(
      name = "apps_action_msg",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String apps_action_msg;
   @Column(
      name = "apps_action_confirm_text",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String apps_action_confirm_text;
   @Column(
      name = "apps_action_confirm_action",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int apps_action_confirm_action;
   @Column(
      name = "apps_action_confirm_action_body",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String apps_action_confirm_action_body;
   @Column(
      name = "apps_dialog_style",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int apps_dialog_style;
   @Column(
      name = "soft_appkey",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String softappkey;

   public int getApps_action_confirm_action() {
      return this.apps_action_confirm_action;
   }

   public void setApps_action_confirm_action(int apps_action_confirm_action) {
      this.apps_action_confirm_action = apps_action_confirm_action;
   }

   public String getApps_action_confirm_action_body() {
      return this.apps_action_confirm_action_body;
   }

   public void setApps_action_confirm_action_body(String apps_action_confirm_action_body) {
      this.apps_action_confirm_action_body = apps_action_confirm_action_body;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getApps_name() {
      return this.apps_name;
   }

   public void setApps_name(String apps_name) {
      this.apps_name = apps_name;
   }

   public int getApps_is_install() {
      return this.apps_is_install;
   }

   public void setApps_is_install(int apps_is_install) {
      this.apps_is_install = apps_is_install;
   }

   public int getApps_action() {
      return this.apps_action;
   }

   public void setApps_action(int apps_action) {
      this.apps_action = apps_action;
   }

   public String getApps_action_title() {
      return this.apps_action_title;
   }

   public void setApps_action_title(String apps_action_title) {
      this.apps_action_title = apps_action_title;
   }

   public String getApps_action_msg() {
      return this.apps_action_msg;
   }

   public void setApps_action_msg(String apps_action_msg) {
      this.apps_action_msg = apps_action_msg;
   }

   public String getApps_action_confirm_text() {
      return this.apps_action_confirm_text;
   }

   public void setApps_action_confirm_text(String apps_action_confirm_text) {
      this.apps_action_confirm_text = apps_action_confirm_text;
   }

   public String getApps_action_body() {
      return this.apps_action_body;
   }

   public void setApps_action_body(String apps_action_body) {
      this.apps_action_body = apps_action_body;
   }

   public int getApps_dialog_style() {
      return this.apps_dialog_style;
   }

   public void setApps_dialog_style(int apps_dialog_style) {
      this.apps_dialog_style = apps_dialog_style;
   }

   public String getSoftappkey() {
      return this.softappkey;
   }

   public void setSoftappkey(String softappkey) {
      this.softappkey = softappkey;
   }
}
