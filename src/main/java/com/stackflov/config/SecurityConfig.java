package com.stackflov.config;

import com.stackflov.jwt.JwtAuthenticationEntryPoint;
import com.stackflov.jwt.JwtFilter;
import com.stackflov.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개 접근 허용 경로 (인증 없이 접근 가능)
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/auth/login", "/auth/register", "/auth/reissue",
                                "/auth/email/**", "/hello", "/"
                        ).permitAll()
                        // 게시글 조회는 인증 없이 가능
                        .requestMatchers(HttpMethod.GET, "/boards/**").permitAll()
                        // 댓글 조회는 인증 없이 가능
                        .requestMatchers(HttpMethod.GET, "/comments/board/**").permitAll()
                        // 팔로워/팔로잉 조회는 인증 없이 가능
                        .requestMatchers(HttpMethod.GET, "/follows/followers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/follows/following/**").permitAll()
                        // 특정 게시글 북마크 여부 확인
                        .requestMatchers(HttpMethod.GET, "/bookmarks/board/**").permitAll()

                        // 인증된 사용자만 접근 가능 (나머지 모든 경로)
                        // 게시글 관련 (생성, 수정, 삭제) (Board 기능 미포함 시 제거)
                        .requestMatchers(HttpMethod.POST, "/boards").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/boards/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/boards/**").authenticated()
                        // 사용자 관련
                        .requestMatchers(HttpMethod.PUT, "/users/password").authenticated()
                        .requestMatchers("/users/me").authenticated() // 내 정보 조회 및 수정
                        // 로그아웃 (AuthService에 logout 메서드가 있으므로 해당 엔드포인트도 authenticated)
                        .requestMatchers("/auth/logout").authenticated()
                        // 댓글 관련 (작성, 수정, 삭제) (Comment 기능 미포함 시 제거)
                        .requestMatchers(HttpMethod.POST, "/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/comments/**").authenticated()
                        // 팔로우 관련 (Follow 기능 미포함 시 제거)
                        .requestMatchers(HttpMethod.POST, "/follows/follow").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/follows/unfollow").authenticated()
                        // 북마크 관련 (Bookmark 기능 미포함 시 제거)
                        .requestMatchers(HttpMethod.POST, "/bookmarks").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/bookmarks").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bookmarks/my").authenticated()

                        // --- 사용자 계정 비활성화/활성화 API 권한 설정 (수정: 타인 계정 관련 경로 제거) ---
                        // 본인 계정 비활성화: 인증된 사용자라면 누구나 가능
                        .requestMatchers(HttpMethod.PUT, "/users/me/deactivate").authenticated()
                        // --- 사용자 계정 비활성화/활성화 API 권한 설정 끝 ---

                        .anyRequest().authenticated() // 위에 명시되지 않은 모든 요청은 인증 필요
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}