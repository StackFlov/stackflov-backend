package com.stackflov.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import java.util.List;
import java.security.Principal;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    public final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60;             // 1시간
    public final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;  // 7일
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    // 액세스 토큰 생성
    public String createAccessToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 만료 시간(ms 단위, 남은 시간)
    public long getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        long now = System.currentTimeMillis();
        return expiration.getTime() - now;
    }

    // 이메일 추출
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            logger.error("JWT 토큰 유효성 검사 실패: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getRole(String token) {
        Object role = parseClaims(token).get("role");
        return role != null ? role.toString() : null;
    }

    public Authentication getAuthentication(String token) {
        // 1. 토큰에서 이메일 추출
        String email = this.getEmail(token);

        // 2. 토큰에서 권한 정보 추출
        String role = this.getRole(token);

        // 3. 권한 설정 (Spring Security 형식에 맞게 "ROLE_USER" 등으로 변환)
        // 만약 DB에 저장된 실제 권한이 필요하다면 UserDetailsService를 주입받아 loadUserByUsername을 호출해야 합니다.
        // 지금은 토큰에 담긴 role 정보를 그대로 사용하도록 구현하겠습니다.
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        // 4. Spring Security 인증 객체 생성 (비밀번호는 null로 처리)
        return new UsernamePasswordAuthenticationToken(email, "", authorities);
    }
}
