package com.verify.service.Config;

import com.verify.service.Entity.UserInfo;
import com.verify.service.Repository.UserInfoRepository;
import com.verify.service.annotation.AuthCheck;
import com.verify.service.annotation.BasicCheck;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
   @Value("${admin.username}")
   public String admin_username;
   @Autowired
   UserInfoRepository userInfoRepository;

   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws LoginException, BasicException {
      if (handler instanceof HandlerMethod) {
         AuthCheck check = (AuthCheck)((HandlerMethod)handler).getMethod().getAnnotation(AuthCheck.class);
         if (check != null) {
            String token = (String)request.getSession().getAttribute("token");
            if (token == null) {
               throw new LoginException("not login");
            }

            UserInfo userInfo = this.userInfoRepository.findByToken(token);
            if (userInfo == null) {
               throw new LoginException("not login");
            }

            if (!userInfo.getName().equals(this.admin_username)) {
               throw new LoginException("login info error");
            }

            return true;
         }

         BasicCheck basicCheck = (BasicCheck)((HandlerMethod)handler).getMethod().getAnnotation(BasicCheck.class);
         if (basicCheck != null) {
            String token = request.getParameter("token");
            if (token == null) {
               token = request.getHeader("token");
            }

            if (token == null) {
               throw new BasicException("Token无效");
            }

            UserInfo userInfo = this.userInfoRepository.findByToken(token);
            if (userInfo == null) {
               throw new BasicException("Token无效");
            }

            if (userInfo.getExpire_time().getTime() < System.currentTimeMillis()) {
               throw new BasicException("用户已过期");
            }
         }
      }

      return true;
   }
}
