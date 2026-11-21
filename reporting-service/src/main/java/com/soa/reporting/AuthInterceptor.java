package com.soa.reporting;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

 @Value("${auth.service.url}")
 private String authUrl;

 private final RestTemplate http = new RestTemplate();

 @Override
 public boolean preHandle(
         HttpServletRequest req,
         HttpServletResponse res,
         Object handler) throws IOException {

  String bearer = req.getHeader("Authorization");

  if (bearer == null ||
          !bearer.startsWith("Bearer ") ||
          bearer.substring(7).isBlank()) {

   fail(res, 401, "Missing Bearer token");
   return false;
  }

  HttpHeaders headers = new HttpHeaders();
  headers.set("Authorization", bearer);

  try {
   http.exchange(
           authUrl,
           HttpMethod.GET,
           new HttpEntity<>(headers),
           String.class
   );
   return true;

  } catch (HttpClientErrorException e) {
   fail(res, 401, "Invalid or expired token");
   return false;

  } catch (RestClientException e) {
   fail(res, 503, "Auth Service unavailable");
   return false;
  }
 }

 private void fail(
         HttpServletResponse res,
         int status,
         String msg) throws IOException {

  res.setStatus(status);
  res.setContentType("application/json;charset=UTF-8");

  res.getWriter().write(
          "{\"error\":\"" + msg + "\"}"
  );
 }
}
