package com.soa.order;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.servlet.HandlerInterceptor;
@Component public class AuthInterceptor implements HandlerInterceptor{
 @Value("${auth.service.url}") String authUrl;
 private final RestTemplate http=new RestTemplate();
 @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler)throws Exception{
  String token=request.getHeader("Authorization");
  if(token==null||!token.startsWith("Bearer ")){send(response,401,"Missing Bearer token");return false;}
  try{http.exchange(authUrl,HttpMethod.GET,new HttpEntity<>(new HttpHeaders(){ {set("Authorization",token);} }),String.class);return true;}
  catch(HttpClientErrorException e){send(response,401,"Invalid token");return false;}
  catch(RestClientException e){send(response,503,"Auth service unavailable");return false;}
 }
 private void send(HttpServletResponse response,int code,String message)throws Exception{response.setStatus(code);response.setContentType("application/json;charset=UTF-8");response.getWriter().write("{\"error\":\""+message+"\"}");}
}
