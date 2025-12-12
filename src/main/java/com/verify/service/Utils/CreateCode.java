package com.verify.service.Utils;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

@Component
public class CreateCode {
   public String RandomPoint() {
      return DigestUtils.md5DigestAsHex((UUID.randomUUID().toString() + System.currentTimeMillis()).getBytes()).substring(5, 15).toUpperCase();
   }
}
