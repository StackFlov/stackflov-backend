package com.stackflov.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {
        // --- 디버그 로그 추가 ---
        System.out.println(">>> JwtAuthenticationEntryPoint 진입 - 요청 URL: " + request.getRequestURI());
        System.out.println(">>> JwtAuthenticationEntryPoint 진입 - 에러 메시지: " + authException.getMessage());
        System.out.println(">>> JwtAuthenticationEntryPoint 진입 - 예외 타입: " + authException.getClass().getName());
        // --
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증되지 않은 사용자입니다.");
    }
}
