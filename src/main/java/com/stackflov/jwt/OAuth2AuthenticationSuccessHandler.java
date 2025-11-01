package com.stackflov.jwt;

import com.stackflov.oauth2.CustomOAuth2User;
import com.stackflov.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;



@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        CustomOAuth2User p = (CustomOAuth2User) auth.getPrincipal();

        String access  = jwtProvider.createAccessToken(p.getEmail(), p.getRole());
        String refresh = jwtProvider.createRefreshToken(p.getEmail());
        redisService.save("RT:" + p.getEmail(), refresh, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        // 로컬 여부 (8080)
        boolean isLocal = "localhost".equalsIgnoreCase(req.getServerName())
                || "127.0.0.1".equals(req.getServerName());

        // ★ 로컬: Secure=false, SameSite=Lax, domain 생략(= host-only: localhost), path="/"
        ResponseCookie accessCookie = (isLocal
                ? ResponseCookie.from("ACCESS_TOKEN", access)
                .httpOnly(true).secure(false).sameSite("Lax")
                .path("/").maxAge(15 * 60)
                : ResponseCookie.from("ACCESS_TOKEN", access)
                .httpOnly(true).secure(true).sameSite("None")
                .domain(".stackflov.com").path("/").maxAge(15 * 60)
        ).build();

        ResponseCookie refreshCookie = (isLocal
                ? ResponseCookie.from("REFRESH_TOKEN", refresh)
                .httpOnly(true).secure(false).sameSite("Lax")
                .path("/").maxAge(14L * 24 * 60 * 60)
                : ResponseCookie.from("REFRESH_TOKEN", refresh)
                .httpOnly(true).secure(true).sameSite("None")
                .domain(".stackflov.com").path("/").maxAge(14L * 24 * 60 * 60)
        ).build();

        // ★ Set-Cookie 먼저 붙이고
        res.addHeader("Set-Cookie", accessCookie.toString());
        res.addHeader("Set-Cookie", refreshCookie.toString());
        res.setHeader("Cache-Control", "no-store");

        // ★ 그다음 리다이렉트 (지금은 localhost:8080/callback)
        res.sendRedirect("http://localhost:8080/auth/callback?ok=1");  // 운영은 app.stackflov.com으로
    }

}
