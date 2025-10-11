package com.stackflov.jwt;

import com.stackflov.oauth2.CustomOAuth2User;
import com.stackflov.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/*@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getEmail();
        String role = oAuth2User.getRole();

        String accessToken = jwtProvider.createAccessToken(email, role);
        String refreshToken = jwtProvider.createRefreshToken(email);

        redisService.save("RT:" + email, refreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        // 프론트엔드로 토큰을 전달하기 위한 리디렉션
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect") // 프론트엔드 주소
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}*/

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Value("${app.frontend.base-url}")   // 예: https://app.stackflov.com
    private String frontBaseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) auth.getPrincipal();

        String email = oAuth2User.getEmail();
        String role  = oAuth2User.getRole();

        String accessToken  = jwtProvider.createAccessToken(email, role);
        String refreshToken = jwtProvider.createRefreshToken(email);

        redisService.save("RT:" + email, refreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        String targetUrl = UriComponentsBuilder.fromUriString(frontBaseUrl + "/oauth-redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build(true) // 인코딩
                .toUriString();

        getRedirectStrategy().sendRedirect(req, res, targetUrl);
    }
}
