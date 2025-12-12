package com.verify.service.Model;

import org.springframework.web.multipart.MultipartFile;

public class InjectModel {
   public MultipartFile file;
   public String token;
   public boolean global;
   public boolean activity;
   public String single;
   public String name;

   public MultipartFile getFile() {
      return this.file;
   }

   public void setFile(MultipartFile file) {
      this.file = file;
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public boolean isGlobal() {
      return this.global;
   }

   public void setGlobal(boolean global) {
      this.global = global;
   }

   public boolean isActivity() {
      return this.activity;
   }

   public void setActivity(boolean activity) {
      this.activity = activity;
   }

   public String getSingle() {
      return this.single;
   }

   public void setSingle(String single) {
      this.single = single;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }
}
