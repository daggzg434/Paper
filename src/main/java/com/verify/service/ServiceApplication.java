package com.verify.service;

import com.verify.service.Utils.OsUtils;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServiceApplication extends SpringBootServletInitializer implements ApplicationRunner {
   public static File error;
   public static File ico;
   public static File temp;
   public static File temp_static;
   public static ThreadPoolExecutor executor;

   public static void main(String[] args) {
      SpringApplication.run(ServiceApplication.class, args);
   }

   protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      return builder.sources(new Class[]{ServiceApplication.class});
   }

   public void run(ApplicationArguments args) {
      System.out.println("初始化基础配置");
      System.out.println("当前核心线程数:" + Runtime.getRuntime().availableProcessors());
      if (OsUtils.isOSLinux()) {
         error = new File("/apps/error");
         ico = new File("/apps/ico");
         temp = new File("/apps/temp");
         temp_static = new File("/apps/static");
      } else {
         error = new File(System.getenv("SYSTEMDRIVE") + "/apps/error");
         ico = new File(System.getenv("SYSTEMDRIVE") + "/apps/ico");
         temp = new File(System.getenv("SYSTEMDRIVE") + "/apps/temp");
         temp_static = new File(System.getenv("SYSTEMDRIVE") + "/apps/static");
      }

      if (!error.exists()) {
         error.mkdirs();
      }

      if (!ico.exists()) {
         ico.mkdirs();
      }

      if (!temp.exists()) {
         temp.mkdirs();
      }

      if (!temp_static.exists()) {
         temp_static.mkdirs();
      }

   }

   public void onStartup(ServletContext servletContext) throws ServletException {
      super.onStartup(servletContext);
      servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));
      SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
      sessionCookieConfig.setHttpOnly(true);
   }

   static {
      executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
   }
}
