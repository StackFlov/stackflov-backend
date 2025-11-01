package com.stackflov.jwt;

import com.stackflov.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.stackflov.config.CustomUserPrincipal;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🔑 Swagger, API Docs, Health 패스
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            String email = jwtProvider.getEmail(token);
            // 컨트롤러의 @RequestAttribute("email") 호환 유지
            request.setAttribute("email", email);
            userRepository.findByEmailAndActiveTrue(email).ifPresent(user -> {
                CustomUserPrincipal principal = CustomUserPrincipal.from(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1) Authorization: Bearer <token>
        String h = request.getHeader("Authorization");
        if (h != null && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String v = h.substring(7).trim();
            if (StringUtils.hasText(v)) return v;
        }

        // 2) Cookie: ACCESS_TOKEN
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("ACCESS_TOKEN".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    return c.getValue();
                }
            }
        }

        // 3) (옵션) 개발 편의용: /?access_token=...
        String qp = request.getParameter("access_token");
        return StringUtils.hasText(qp) ? qp : null;
    }
}
