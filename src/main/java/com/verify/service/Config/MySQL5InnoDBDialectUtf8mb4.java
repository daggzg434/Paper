package com.verify.service.Config;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class MySQL5InnoDBDialectUtf8mb4 extends MySQL5InnoDBDialect {
   public String getTableTypeString() {
      return "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci";
   }
}
