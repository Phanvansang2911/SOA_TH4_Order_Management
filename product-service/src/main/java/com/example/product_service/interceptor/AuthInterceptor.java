package com.example.product_service.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${auth.service.url}")
    private String authUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws Exception {

        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendError(res, 401, "Thiếu token");
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", header);
            restTemplate.exchange(authUrl, HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);
            return true; // token hợp lệ, cho đi tiếp vào controller
        } catch (HttpClientErrorException e) {
            sendError(res, 401, "Token không hợp lệ hoặc hết hạn");
        } catch (RestClientException e) {
            sendError(res, 503, "Auth Service không khả dụng");
        }
        return false;
    }

    private void sendError(HttpServletResponse res, int status, String msg) throws Exception {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"" + msg + "\"}");
    }
}