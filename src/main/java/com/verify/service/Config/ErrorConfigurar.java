package com.verify.service.Config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ErrorConfigurar implements ErrorPageRegistrar {
   public void registerErrorPages(ErrorPageRegistry registry) {
      ErrorPage[] errorPages = new ErrorPage[]{new ErrorPage(HttpStatus.NOT_FOUND, "/"), new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/")};
      registry.addErrorPages(errorPages);
   }
}
