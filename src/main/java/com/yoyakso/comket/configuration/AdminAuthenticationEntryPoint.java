package com.yoyakso.comket.configuration;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        
        // 요청 경로가 /admin으로 시작하는 경우에만 로그인 페이지로 리다이렉트
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/admin") && !requestURI.equals("/admin-login")) {
            response.sendRedirect("/admin-login");
        } else {
            // API 요청의 경우 401 Unauthorized 응답
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}