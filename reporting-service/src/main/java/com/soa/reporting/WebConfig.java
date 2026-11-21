package com.soa.reporting;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration public class WebConfig implements WebMvcConfigurer{
 private final AuthInterceptor interceptor;
 public WebConfig(AuthInterceptor interceptor){this.interceptor=interceptor;}
 @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(interceptor).addPathPatterns("/reports/**");}
}
