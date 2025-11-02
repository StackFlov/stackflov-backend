package com.stackflov.jwt;

import com.stackflov.oauth2.CustomOAuth2User;
import com.stackflov.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.front-callback}")
    private String frontCallback;          // ← 프로필별로 값 주입
    @Value("${app.cookie-domain:}")        // 예: ".stackflov.com" (로컬은 비움)
    private String cookieDomain;
    @Value("${app.cookie-samesite:Lax}")   // prod: None
    private String cookieSameSite;
    @Value("${app.cookie-secure:false}")   // prod: true
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        CustomOAuth2User p = (CustomOAuth2User) auth.getPrincipal();
        String access  = jwtProvider.createAccessToken(p.getEmail(), p.getRole());
        String refresh = jwtProvider.createRefreshToken(p.getEmail());
        redisService.save("RT:" + p.getEmail(), refresh, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        ResponseCookie acc = ResponseCookie.from("accessToken", access)
                .httpOnly(false)                // ← 여기 true → false
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(15 * 60).build();

        ResponseCookie ref = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(false)                // ← 여기 true → false
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(14L * 24 * 60 * 60).build();

        // 필요 시만 설정 (로컬은 설정하지 않음)
        if (!cookieDomain.isBlank()) {
            acc = ResponseCookie.from(acc.getName(), acc.getValue())
                    .httpOnly(false)            // ← 여기서도 false로!
                    .secure(cookieSecure)
                    .sameSite(cookieSameSite)
                    .path("/")
                    .domain(cookieDomain)
                    .maxAge(acc.getMaxAge().getSeconds())
                    .build();

            ref = ResponseCookie.from(ref.getName(), ref.getValue())
                    .httpOnly(false)            // ← 여기서도 false로!
                    .secure(cookieSecure)
                    .sameSite(cookieSameSite)
                    .path("/")
                    .domain(cookieDomain)
                    .maxAge(ref.getMaxAge().getSeconds())
                    .build();
        }

        res.addHeader("Set-Cookie", acc.toString());
        res.addHeader("Set-Cookie", ref.toString());
        res.setHeader("Cache-Control", "no-store");

        // ✅ 프론트로 리다이렉트 (프로필별 값)
        res.sendRedirect(frontCallback);
    }

}
