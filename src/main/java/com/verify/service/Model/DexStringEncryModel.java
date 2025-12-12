package com.verify.service.Model;

import org.springframework.web.multipart.MultipartFile;

public class DexStringEncryModel {
   public MultipartFile file;
   public String token;
   public String config;
   public int encry_type;

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

   public String getConfig() {
      return this.config;
   }

   public void setConfig(String config) {
      this.config = config;
   }

   public int getEncry_type() {
      return this.encry_type;
   }

   public void setEncry_type(int encry_type) {
      this.encry_type = encry_type;
   }
}
