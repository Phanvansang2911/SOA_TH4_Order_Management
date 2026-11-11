package com.soa.order;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration public class WebConfig implements WebMvcConfigurer{
 private final AuthInterceptor auth;
 public WebConfig(AuthInterceptor auth){this.auth=auth;}
 @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(auth).addPathPatterns("/orders", "/orders/**", "/order_items", "/order_items/**");}
}
