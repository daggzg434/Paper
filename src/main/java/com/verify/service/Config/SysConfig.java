package com.verify.service.Config;

import com.verify.service.Utils.OsUtils;
import java.io.File;
import javax.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SysConfig {
   @Bean
   MultipartConfigElement multipartConfigElement() {
      File tmp = null;
      if (OsUtils.isOSLinux()) {
         tmp = new File("/apps/tmp");
      } else {
         tmp = new File(System.getenv("SYSTEMDRIVE") + "/apps/tmp");
      }

      if (!tmp.exists()) {
         tmp.mkdirs();
      }

      MultipartConfigFactory factory = new MultipartConfigFactory();
      factory.setLocation(tmp.getAbsolutePath());
      return factory.createMultipartConfig();
   }
}
