package com.verify.service.Config;

import java.nio.charset.Charset;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Component
public class MvcConfigurer extends WebMvcConfigurationSupport {
   @Autowired
   public PermissionInterceptor permissionInterceptor;

   protected void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(this.permissionInterceptor);
      super.addInterceptors(registry);
   }

   @Bean
   public HttpMessageConverter<String> responseBodyConverter() {
      return new StringHttpMessageConverter(Charset.forName("UTF-8"));
   }

   protected void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler(new String[]{"/**"}).addResourceLocations(new String[]{"classpath:/static/assets/"});
      super.addResourceHandlers(registry);
   }

   public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
      converters.add(this.responseBodyConverter());
      this.addDefaultHttpMessageConverters(converters);
   }

   public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
      configurer.favorPathExtension(false);
   }
}
