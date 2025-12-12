package com.verify.service.Controller;

import com.verify.service.Config.BasicException;
import com.verify.service.Config.LoginException;
import com.verify.service.Result.BaseResult;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHadlers {
   @ExceptionHandler({Exception.class})
   public BaseResult errorHandler(Exception ex) {
      BaseResult baseResult = new BaseResult(404, "服务器错误");
      ex.printStackTrace();
      return baseResult;
   }

   @ExceptionHandler({BasicException.class})
   public BaseResult BasicerrorHandler(Exception ex) {
      BaseResult baseResult = new BaseResult(404, ex.getMessage());
      return baseResult;
   }

   @ExceptionHandler({LoginException.class})
   public void errorHandler2(HttpServletResponse response) {
      try {
         response.sendRedirect("/");
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }
}
