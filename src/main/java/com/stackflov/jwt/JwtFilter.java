package com.stackflov.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(">>> JwtFilter: doFilterInternal 호출됨. 요청 URL: " + request.getRequestURI()); // ✅ 1번 로그

        String token = resolveToken(request);
        // 토큰이 너무 길 수 있으므로, 일부만 출력
        System.out.println(">>> JwtFilter: 추출된 토큰: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "없음")); // ✅ 2번 로그

        if (token != null && jwtProvider.validateToken(token)) {
            System.out.println(">>> JwtFilter: 토큰 유효성 검사 성공!"); // ✅ 3번 로그
            String email = jwtProvider.getEmail(token);
            System.out.println(">>> JwtFilter: 토큰에서 추출된 이메일: " + email); // ✅ 4번 로그

            request.setAttribute("email", email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println(">>> JwtFilter: SecurityContextHolder에 인증 정보 설정 완료."); // ✅ 5번 로그
        } else {
            System.out.println(">>> JwtFilter: 토큰이 유효하지 않거나 존재하지 않음."); // ✅ 6번 로그
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        System.out.println(">>> JwtFilter: Authorization 헤더 값: " + bearerToken); // ✅ 7번 로그
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}