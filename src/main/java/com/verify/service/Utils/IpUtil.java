package com.verify.service.Utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.http.HttpServletRequest;

public class IpUtil {
   private static final String UNKNOWN = "unknown";
   private static final String LOCALHOST = "127.0.0.1";
   private static final String SEPARATOR = ",";

   public static String getIpAddr(HttpServletRequest request) {
      String ipAddress;
      try {
         ipAddress = request.getHeader("x-forwarded-for");
         if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
         }

         if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
         }

         if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if ("127.0.0.1".equals(ipAddress)) {
               InetAddress inet = null;

               try {
                  inet = InetAddress.getLocalHost();
               } catch (UnknownHostException var4) {
                  var4.printStackTrace();
               }

               ipAddress = inet.getHostAddress();
            }
         }

         if (ipAddress != null && ipAddress.length() > 15 && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
         }
      } catch (Exception var5) {
         ipAddress = "";
      }

      return ipAddress;
   }
}
