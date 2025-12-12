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
import javax.validation.constraints.NotNull;

@Entity
@Table(
   name = "pangolin_sysconfig"
)
public class SysConfig {
   @Id
   @GeneratedValue(
      strategy = GenerationType.IDENTITY
   )
   public int id;
   @NotNull(
      message = "QQ群号不能为空"
   )
   @Column(
      name = "group_key"
   )
   public String group;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "update_msg"
   )
   public String update_msg;
   @Column(
      name = "buy_url"
   )
   public String buy_url;
   @Column(
      name = "full_url"
   )
   public String full_url;
   @Column(
      name = "hot_fix_url"
   )
   public String hot_fix_url;
   @Column(
      name = "x86full_url"
   )
   public String x86full_url;
   @Column(
      name = "x86hot_fix_url"
   )
   public String x86hot_fix_url;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "help"
   )
   public String help;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "about"
   )
   public String about;
   @Lob
   @Basic(
      fetch = FetchType.LAZY
   )
   @Column(
      name = "share"
   )
   public String share;

   public String getX86full_url() {
      return this.x86full_url;
   }

   public void setX86full_url(String x86full_url) {
      this.x86full_url = x86full_url;
   }

   public String getX86hot_fix_url() {
      return this.x86hot_fix_url;
   }

   public void setX86hot_fix_url(String x86hot_fix_url) {
      this.x86hot_fix_url = x86hot_fix_url;
   }

   public String getFull_url() {
      return this.full_url;
   }

   public void setFull_url(String full_url) {
      this.full_url = full_url;
   }

   public String getHot_fix_url() {
      return this.hot_fix_url;
   }

   public void setHot_fix_url(String hot_fix_url) {
      this.hot_fix_url = hot_fix_url;
   }

   public String getGroup() {
      return this.group;
   }

   public void setGroup(String group) {
      this.group = group;
   }

   public String getShare() {
      return this.share;
   }

   public void setShare(String share) {
      this.share = share;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getUpdate_msg() {
      return this.update_msg;
   }

   public void setUpdate_msg(String update_msg) {
      this.update_msg = update_msg;
   }

   public String getBuy_url() {
      return this.buy_url;
   }

   public void setBuy_url(String buy_url) {
      this.buy_url = buy_url;
   }

   public String getHelp() {
      return this.help;
   }

   public void setHelp(String help) {
      this.help = help;
   }

   public String getAbout() {
      return this.about;
   }

   public void setAbout(String about) {
      this.about = about;
   }
}
