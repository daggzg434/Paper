package com.verify.service.Entity;

import com.verify.service.Utils.SHAUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.DigestUtils;

@DynamicInsert
@DynamicUpdate
@Entity
@Table(
   name = "pangolin_software"
)
public class UserSoft implements Serializable {
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
   public String appkey = SHAUtils.SHA1(DigestUtils.md5DigestAsHex((UUID.randomUUID().toString() + System.currentTimeMillis()).getBytes()));
   @Column(
      name = "soft_name",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String soft_name;
   @Column(
      name = "soft_package_name",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String softpackagename;
   @Column(
      name = "bj_url",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String bj_url;
   @Column(
      name = "authmode",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int authmode = 0;
   @Temporal(TemporalType.DATE)
   @Column(
      name = "create_time",
      updatable = false,
      nullable = false
   )
   public Date create_time = new Date();
   @Column(
      name = "user_id",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int userid = 0;
   @Column(
      name = "ico_name",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String ico_name;
   @Column(
      name = "open_update",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_update;
   @Column(
      name = "update_title",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String update_title;
   @Column(
      name = "version",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int version;
   @Column(
      name = "update_url",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String update_url;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "update_msg"
   )
   public String update_msg;
   @Column(
      name = "updatemode",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int updatemode = 0;
   @Column(
      name = "update_ver_mode",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int update_ver_mode = 0;
   @Column(
      name = "update_dialog_style",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int update_dialog_style = 0;
   @Column(
      name = "frozen",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int frozen = 0;
   @Column(
      name = "try_minutes",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int try_minutes = 0;
   @Column(
      name = "try_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int try_count = 0;
   @Column(
      name = "bindmode",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int bindmode = 0;
   @Column(
      name = "title",
      columnDefinition = "varchar(50) default ''",
      nullable = false
   )
   public String title;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "notice"
   )
   public String notice;
   @Column(
      name = "dialog_style",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int dialog_style = 0;
   @Column(
      name = "weburl",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String weburl;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "diy_body"
   )
   public String diy_body = "";
   @Column(
      name = "share_count",
      columnDefinition = "int default 5",
      nullable = false
   )
   public int share_count = 5;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "share_msg"
   )
   public String share_msg;
   @Column(
      name = "delay_time",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int delay_time = 0;
   @Column(
      name = "show_count",
      columnDefinition = "int default 10",
      nullable = false
   )
   public int show_count = 10;
   @Column(
      name = "more_url",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String more_url;
   @Column(
      name = "qq_key",
      columnDefinition = "varchar(11) default 0",
      nullable = false
   )
   public String qq_key;
   @Column(
      name = "group_key",
      columnDefinition = "varchar(11) default 0",
      nullable = false
   )
   public String group_key;
   @Column(
      name = "group_style",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int group_style = 0;
   @Column(
      name = "rebindmode",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int rebindmode = 0;
   @Column(
      name = "reggive_time",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int reggive_time = 0;
   @Column(
      name = "unbind_time",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int unbind_time = 0;
   @Column(
      name = "use_point_card_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int use_point_card_count = 0;
   @Column(
      name = "all_point_card_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int all_point_card_count = 0;
   @Column(
      name = "use_recode_card_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int use_recode_card_count = 0;
   @Column(
      name = "all_recode_card_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int all_recode_card_count = 0;
   @Column(
      name = "use_recard_card_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int use_recard_card_count = 0;
   @Column(
      name = "all_recard_card_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int all_recard_card_count = 0;
   @Column(
      name = "all_user_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int all_user_count = 0;
   @Column(
      name = "soft_use_count",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int soft_use_count = 0;
   @Column(
      name = "open_bugly",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_bugly = 0;
   @Column(
      name = "bugly_appkey",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String bugly_appkey;
   @Column(
      name = "open_vpn_check",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_vpn_check = 0;
   @Column(
      name = "open_xposed_check",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_xposed_check = 0;
   @Column(
      name = "open_secure_check",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_secure_check = 0;
   @Column(
      name = "open_vxp_check",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_vxp_check = 0;
   @Column(
      name = "open_splach",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_splach = 0;
   @Column(
      name = "splach_url",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String splach_url;
   @Column(
      name = "splach_time",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int splach_time = 0;
   @Column(
      name = "open_notice",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_notice = 0;
   @Column(
      name = "open_notice_title",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_notice_title;
   @Column(
      name = "open_notice_body",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_notice_body;
   @Column(
      name = "open_notice_auto",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_notice_auto = 0;
   @Column(
      name = "open_notice_dialog_style",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_notice_dialog_style = 0;
   @Column(
      name = "open_notice_cancel",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_notice_cancel = 0;
   @Column(
      name = "open_notice_confirm_text",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_notice_confirm_text;
   @Column(
      name = "open_notice_cancel_text",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_notice_cancel_text;
   @Column(
      name = "open_notice_additional_text",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_notice_additional_text;
   @Column(
      name = "open_notice_confirm_action",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_notice_confirm_action = 0;
   @Column(
      name = "open_notice_additional_action",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_notice_additional_action = 0;
   @Column(
      name = "open_notice_confirm_action_body",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_notice_confirm_action_body;
   @Column(
      name = "open_notice_additional_action_body",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_notice_additional_action_body;
   @Column(
      name = "open_top_lamp",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_top_lamp = 0;
   @Column(
      name = "open_top_lamp_msg",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_top_lamp_msg;
   @Column(
      name = "open_top_lamp_text_color",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_top_lamp_text_color;
   @Column(
      name = "open_top_lamp_bj_color",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_top_lamp_bj_color;
   @Column(
      name = "open_top_lamp_action",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_top_lamp_action = 0;
   @Column(
      name = "open_top_lamp_action_body",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String open_top_lamp_action_body;
   @Column(
      name = "dialog_title_color",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String dialog_title_color;
   @Column(
      name = "dialog_msg_color",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String dialog_msg_color;
   @Column(
      name = "dialog_confirm_color",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String dialog_confirm_color;
   @Column(
      name = "dialog_cancel_color",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String dialog_cancel_color;
   @Column(
      name = "dialog_additional_color",
      columnDefinition = "varchar(255) default ''",
      nullable = false
   )
   public String dialog_additional_color;
   @Column(
      name = "open_apps_check",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_apps_check = 0;
   @Column(
      name = "open_apps_sign_check",
      columnDefinition = "int default 0",
      nullable = false
   )
   public int open_apps_sign_check = 0;

   public int getSoft_use_count() {
      return this.soft_use_count;
   }

   public void setSoft_use_count(int soft_use_count) {
      this.soft_use_count = soft_use_count;
   }

   public int getOpen_apps_check() {
      return this.open_apps_check;
   }

   public void setOpen_apps_check(int open_apps_check) {
      this.open_apps_check = open_apps_check;
   }

   public int getOpen_apps_sign_check() {
      return this.open_apps_sign_check;
   }

   public void setOpen_apps_sign_check(int open_apps_sign_check) {
      this.open_apps_sign_check = open_apps_sign_check;
   }

   public String getDialog_title_color() {
      return this.dialog_title_color;
   }

   public void setDialog_title_color(String dialog_title_color) {
      this.dialog_title_color = dialog_title_color;
   }

   public String getDialog_msg_color() {
      return this.dialog_msg_color;
   }

   public void setDialog_msg_color(String dialog_msg_color) {
      this.dialog_msg_color = dialog_msg_color;
   }

   public String getDialog_confirm_color() {
      return this.dialog_confirm_color;
   }

   public void setDialog_confirm_color(String dialog_confirm_color) {
      this.dialog_confirm_color = dialog_confirm_color;
   }

   public String getDialog_cancel_color() {
      return this.dialog_cancel_color;
   }

   public void setDialog_cancel_color(String dialog_cancel_color) {
      this.dialog_cancel_color = dialog_cancel_color;
   }

   public String getDialog_additional_color() {
      return this.dialog_additional_color;
   }

   public void setDialog_additional_color(String dialog_additional_color) {
      this.dialog_additional_color = dialog_additional_color;
   }

   public int getOpen_top_lamp() {
      return this.open_top_lamp;
   }

   public void setOpen_top_lamp(int open_top_lamp) {
      this.open_top_lamp = open_top_lamp;
   }

   public String getOpen_top_lamp_msg() {
      return this.open_top_lamp_msg;
   }

   public void setOpen_top_lamp_msg(String open_top_lamp_msg) {
      this.open_top_lamp_msg = open_top_lamp_msg;
   }

   public String getOpen_top_lamp_text_color() {
      return this.open_top_lamp_text_color;
   }

   public void setOpen_top_lamp_text_color(String open_top_lamp_text_color) {
      this.open_top_lamp_text_color = open_top_lamp_text_color;
   }

   public String getOpen_top_lamp_bj_color() {
      return this.open_top_lamp_bj_color;
   }

   public void setOpen_top_lamp_bj_color(String open_top_lamp_bj_color) {
      this.open_top_lamp_bj_color = open_top_lamp_bj_color;
   }

   public int getOpen_top_lamp_action() {
      return this.open_top_lamp_action;
   }

   public void setOpen_top_lamp_action(int open_top_lamp_action) {
      this.open_top_lamp_action = open_top_lamp_action;
   }

   public String getOpen_top_lamp_action_body() {
      return this.open_top_lamp_action_body;
   }

   public void setOpen_top_lamp_action_body(String open_top_lamp_action_body) {
      this.open_top_lamp_action_body = open_top_lamp_action_body;
   }

   public int getOpen_update() {
      return this.open_update;
   }

   public void setOpen_update(int open_update) {
      this.open_update = open_update;
   }

   public String getUpdate_title() {
      return this.update_title;
   }

   public void setUpdate_title(String update_title) {
      this.update_title = update_title;
   }

   public int getUpdate_ver_mode() {
      return this.update_ver_mode;
   }

   public void setUpdate_ver_mode(int update_ver_mode) {
      this.update_ver_mode = update_ver_mode;
   }

   public int getUpdate_dialog_style() {
      return this.update_dialog_style;
   }

   public void setUpdate_dialog_style(int update_dialog_style) {
      this.update_dialog_style = update_dialog_style;
   }

   public String getOpen_notice_confirm_action_body() {
      return this.open_notice_confirm_action_body;
   }

   public void setOpen_notice_confirm_action_body(String open_notice_confirm_action_body) {
      this.open_notice_confirm_action_body = open_notice_confirm_action_body;
   }

   public String getOpen_notice_additional_action_body() {
      return this.open_notice_additional_action_body;
   }

   public void setOpen_notice_additional_action_body(String open_notice_additional_action_body) {
      this.open_notice_additional_action_body = open_notice_additional_action_body;
   }

   public int getOpen_secure_check() {
      return this.open_secure_check;
   }

   public void setOpen_secure_check(int open_secure_check) {
      this.open_secure_check = open_secure_check;
   }

   public int getOpen_vxp_check() {
      return this.open_vxp_check;
   }

   public void setOpen_vxp_check(int open_vxp_check) {
      this.open_vxp_check = open_vxp_check;
   }

   public int getOpen_splach() {
      return this.open_splach;
   }

   public void setOpen_splach(int open_splach) {
      this.open_splach = open_splach;
   }

   public String getSplach_url() {
      return this.splach_url;
   }

   public void setSplach_url(String splach_url) {
      this.splach_url = splach_url;
   }

   public int getSplach_time() {
      return this.splach_time;
   }

   public void setSplach_time(int splach_time) {
      this.splach_time = splach_time;
   }

   public int getOpen_notice() {
      return this.open_notice;
   }

   public void setOpen_notice(int open_notice) {
      this.open_notice = open_notice;
   }

   public String getOpen_notice_title() {
      return this.open_notice_title;
   }

   public void setOpen_notice_title(String open_notice_title) {
      this.open_notice_title = open_notice_title;
   }

   public String getOpen_notice_body() {
      return this.open_notice_body;
   }

   public void setOpen_notice_body(String open_notice_body) {
      this.open_notice_body = open_notice_body;
   }

   public int getOpen_notice_auto() {
      return this.open_notice_auto;
   }

   public void setOpen_notice_auto(int open_notice_auto) {
      this.open_notice_auto = open_notice_auto;
   }

   public int getOpen_notice_dialog_style() {
      return this.open_notice_dialog_style;
   }

   public void setOpen_notice_dialog_style(int open_notice_dialog_style) {
      this.open_notice_dialog_style = open_notice_dialog_style;
   }

   public int getOpen_notice_cancel() {
      return this.open_notice_cancel;
   }

   public void setOpen_notice_cancel(int open_notice_cancel) {
      this.open_notice_cancel = open_notice_cancel;
   }

   public String getOpen_notice_confirm_text() {
      return this.open_notice_confirm_text;
   }

   public void setOpen_notice_confirm_text(String open_notice_confirm_text) {
      this.open_notice_confirm_text = open_notice_confirm_text;
   }

   public String getOpen_notice_cancel_text() {
      return this.open_notice_cancel_text;
   }

   public void setOpen_notice_cancel_text(String open_notice_cancel_text) {
      this.open_notice_cancel_text = open_notice_cancel_text;
   }

   public String getOpen_notice_additional_text() {
      return this.open_notice_additional_text;
   }

   public void setOpen_notice_additional_text(String open_notice_additional_text) {
      this.open_notice_additional_text = open_notice_additional_text;
   }

   public int getOpen_notice_confirm_action() {
      return this.open_notice_confirm_action;
   }

   public void setOpen_notice_confirm_action(int open_notice_confirm_action) {
      this.open_notice_confirm_action = open_notice_confirm_action;
   }

   public int getOpen_notice_additional_action() {
      return this.open_notice_additional_action;
   }

   public void setOpen_notice_additional_action(int open_notice_additional_action) {
      this.open_notice_additional_action = open_notice_additional_action;
   }

   public int getOpen_bugly() {
      return this.open_bugly;
   }

   public void setOpen_bugly(int open_bugly) {
      this.open_bugly = open_bugly;
   }

   public String getBugly_appkey() {
      return this.bugly_appkey;
   }

   public void setBugly_appkey(String bugly_appkey) {
      this.bugly_appkey = bugly_appkey;
   }

   public int getOpen_vpn_check() {
      return this.open_vpn_check;
   }

   public void setOpen_vpn_check(int open_vpn_check) {
      this.open_vpn_check = open_vpn_check;
   }

   public int getOpen_xposed_check() {
      return this.open_xposed_check;
   }

   public void setOpen_xposed_check(int open_xposed_check) {
      this.open_xposed_check = open_xposed_check;
   }

   public int getUse_point_card_count() {
      return this.use_point_card_count;
   }

   public void setUse_point_card_count(int use_point_card_count) {
      this.use_point_card_count = use_point_card_count;
   }

   public int getAll_point_card_count() {
      return this.all_point_card_count;
   }

   public void setAll_point_card_count(int all_point_card_count) {
      this.all_point_card_count = all_point_card_count;
   }

   public int getUse_recode_card_count() {
      return this.use_recode_card_count;
   }

   public void setUse_recode_card_count(int use_recode_card_count) {
      this.use_recode_card_count = use_recode_card_count;
   }

   public int getAll_recode_card_count() {
      return this.all_recode_card_count;
   }

   public void setAll_recode_card_count(int all_recode_card_count) {
      this.all_recode_card_count = all_recode_card_count;
   }

   public int getUse_recard_card_count() {
      return this.use_recard_card_count;
   }

   public void setUse_recard_card_count(int use_recard_card_count) {
      this.use_recard_card_count = use_recard_card_count;
   }

   public int getAll_recard_card_count() {
      return this.all_recard_card_count;
   }

   public void setAll_recard_card_count(int all_recard_card_count) {
      this.all_recard_card_count = all_recard_card_count;
   }

   public int getAll_user_count() {
      return this.all_user_count;
   }

   public void setAll_user_count(int all_user_count) {
      this.all_user_count = all_user_count;
   }

   public String getBj_url() {
      return this.bj_url;
   }

   public void setBj_url(String bj_url) {
      this.bj_url = bj_url;
   }

   public int getShare_count() {
      return this.share_count;
   }

   public void setShare_count(int share_count) {
      this.share_count = share_count;
   }

   public String getShare_msg() {
      return this.share_msg;
   }

   public void setShare_msg(String share_msg) {
      this.share_msg = share_msg;
   }

   public int getDelay_time() {
      return this.delay_time;
   }

   public void setDelay_time(int delay_time) {
      this.delay_time = delay_time;
   }

   public int getShow_count() {
      return this.show_count;
   }

   public void setShow_count(int show_count) {
      this.show_count = show_count;
   }

   public String getMore_url() {
      return this.more_url;
   }

   public void setMore_url(String more_url) {
      this.more_url = more_url;
   }

   public String getQq_key() {
      return this.qq_key;
   }

   public void setQq_key(String qq_key) {
      this.qq_key = qq_key;
   }

   public String getGroup_key() {
      return this.group_key;
   }

   public void setGroup_key(String group_key) {
      this.group_key = group_key;
   }

   public int getGroup_style() {
      return this.group_style;
   }

   public void setGroup_style(int group_style) {
      this.group_style = group_style;
   }

   public int getRebindmode() {
      return this.rebindmode;
   }

   public void setRebindmode(int rebindmode) {
      this.rebindmode = rebindmode;
   }

   public int getReggive_time() {
      return this.reggive_time;
   }

   public void setReggive_time(int reggive_time) {
      this.reggive_time = reggive_time;
   }

   public int getUnbind_time() {
      return this.unbind_time;
   }

   public void setUnbind_time(int unbind_time) {
      this.unbind_time = unbind_time;
   }

   public int getVersion() {
      return this.version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public int getFrozen() {
      return this.frozen;
   }

   public void setFrozen(int frozen) {
      this.frozen = frozen;
   }

   public int getTry_minutes() {
      return this.try_minutes;
   }

   public void setTry_minutes(int try_minutes) {
      this.try_minutes = try_minutes;
   }

   public int getTry_count() {
      return this.try_count;
   }

   public void setTry_count(int try_count) {
      this.try_count = try_count;
   }

   public int getBindmode() {
      return this.bindmode;
   }

   public void setBindmode(int bindmode) {
      this.bindmode = bindmode;
   }

   public int getUpdatemode() {
      return this.updatemode;
   }

   public void setUpdatemode(int updatemode) {
      this.updatemode = updatemode;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getNotice() {
      return this.notice;
   }

   public void setNotice(String notice) {
      this.notice = notice;
   }

   public int getDialog_style() {
      return this.dialog_style;
   }

   public void setDialog_style(int dialog_style) {
      this.dialog_style = dialog_style;
   }

   public String getWeburl() {
      return this.weburl;
   }

   public void setWeburl(String weburl) {
      this.weburl = weburl;
   }

   public String getUpdate_url() {
      return this.update_url;
   }

   public void setUpdate_url(String update_url) {
      this.update_url = update_url;
   }

   public String getUpdate_msg() {
      return this.update_msg;
   }

   public void setUpdate_msg(String update_msg) {
      this.update_msg = update_msg;
   }

   public String getDiy_body() {
      return this.diy_body;
   }

   public void setDiy_body(String diy_body) {
      this.diy_body = diy_body;
   }

   public String getIco_name() {
      return this.ico_name;
   }

   public void setIco_name(String ico_name) {
      this.ico_name = ico_name;
   }

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

   public String getSoft_name() {
      return this.soft_name;
   }

   public void setSoft_name(String soft_name) {
      this.soft_name = soft_name;
   }

   public String getSoftpackagename() {
      return this.softpackagename;
   }

   public void setSoftpackagename(String softpackagename) {
      this.softpackagename = softpackagename;
   }

   public int getAuthmode() {
      return this.authmode;
   }

   public void setAuthmode(int authmode) {
      this.authmode = authmode;
   }

   public Date getCreate_time() {
      return this.create_time;
   }

   public void setCreate_time(Date create_time) {
      this.create_time = create_time;
   }

   public int getUserid() {
      return this.userid;
   }

   public void setUserid(int userid) {
      this.userid = userid;
   }

   public String toString() {
      return "UserSoft{id=" + this.id + ", appkey='" + this.appkey + '\'' + ", soft_name='" + this.soft_name + '\'' + ", softpackagename='" + this.softpackagename + '\'' + ", bj_url='" + this.bj_url + '\'' + ", authmode=" + this.authmode + ", create_time=" + this.create_time + ", userid=" + this.userid + ", ico_name='" + this.ico_name + '\'' + ", open_update=" + this.open_update + ", update_title='" + this.update_title + '\'' + ", version=" + this.version + ", update_url='" + this.update_url + '\'' + ", update_msg='" + this.update_msg + '\'' + ", updatemode=" + this.updatemode + ", update_ver_mode=" + this.update_ver_mode + ", update_dialog_style=" + this.update_dialog_style + ", frozen=" + this.frozen + ", try_minutes=" + this.try_minutes + ", try_count=" + this.try_count + ", bindmode=" + this.bindmode + ", title='" + this.title + '\'' + ", notice='" + this.notice + '\'' + ", dialog_style=" + this.dialog_style + ", weburl='" + this.weburl + '\'' + ", diy_body='" + this.diy_body + '\'' + ", share_count=" + this.share_count + ", share_msg='" + this.share_msg + '\'' + ", delay_time=" + this.delay_time + ", show_count=" + this.show_count + ", more_url='" + this.more_url + '\'' + ", qq_key='" + this.qq_key + '\'' + ", group_key='" + this.group_key + '\'' + ", group_style=" + this.group_style + ", rebindmode=" + this.rebindmode + ", reggive_time=" + this.reggive_time + ", unbind_time=" + this.unbind_time + ", use_point_card_count=" + this.use_point_card_count + ", all_point_card_count=" + this.all_point_card_count + ", use_recode_card_count=" + this.use_recode_card_count + ", all_recode_card_count=" + this.all_recode_card_count + ", use_recard_card_count=" + this.use_recard_card_count + ", all_recard_card_count=" + this.all_recard_card_count + ", all_user_count=" + this.all_user_count + ", open_bugly=" + this.open_bugly + ", bugly_appkey='" + this.bugly_appkey + '\'' + ", open_vpn_check=" + this.open_vpn_check + ", open_xposed_check=" + this.open_xposed_check + ", open_secure_check=" + this.open_secure_check + ", open_vxp_check=" + this.open_vxp_check + ", open_splach=" + this.open_splach + ", splach_url='" + this.splach_url + '\'' + ", splach_time=" + this.splach_time + ", open_notice=" + this.open_notice + ", open_notice_title='" + this.open_notice_title + '\'' + ", open_notice_body='" + this.open_notice_body + '\'' + ", open_notice_auto=" + this.open_notice_auto + ", open_notice_dialog_style=" + this.open_notice_dialog_style + ", open_notice_cancel=" + this.open_notice_cancel + ", open_notice_confirm_text='" + this.open_notice_confirm_text + '\'' + ", open_notice_cancel_text='" + this.open_notice_cancel_text + '\'' + ", open_notice_additional_text='" + this.open_notice_additional_text + '\'' + ", open_notice_confirm_action=" + this.open_notice_confirm_action + ", open_notice_additional_action=" + this.open_notice_additional_action + ", open_notice_confirm_action_body='" + this.open_notice_confirm_action_body + '\'' + ", open_notice_additional_action_body='" + this.open_notice_additional_action_body + '\'' + ", open_top_lamp=" + this.open_top_lamp + ", open_top_lamp_msg='" + this.open_top_lamp_msg + '\'' + ", open_top_lamp_text_color='" + this.open_top_lamp_text_color + '\'' + ", open_top_lamp_bj_color='" + this.open_top_lamp_bj_color + '\'' + ", open_top_lamp_action=" + this.open_top_lamp_action + ", open_top_lamp_action_body='" + this.open_top_lamp_action_body + '\'' + '}';
   }
}
