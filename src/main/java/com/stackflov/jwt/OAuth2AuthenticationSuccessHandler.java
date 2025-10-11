package com.stackflov.jwt;

import com.stackflov.oauth2.CustomOAuth2User;
import com.stackflov.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        CustomOAuth2User p = (CustomOAuth2User) auth.getPrincipal();

        String accessToken  = jwtProvider.createAccessToken(p.getEmail(), p.getRole());
        String refreshToken = jwtProvider.createRefreshToken(p.getEmail());

        redisService.save("RT:" + p.getEmail(), refreshToken, jwtProvider.REFRESH_TOKEN_EXPIRE_TIME);

        res.setStatus(HttpServletResponse.SC_OK);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");

        String json = """
        {"accessToken":"%s","refreshToken":"%s"}
        """.formatted(accessToken, refreshToken);

        res.getWriter().write(json);
        res.getWriter().flush();
    }
}
