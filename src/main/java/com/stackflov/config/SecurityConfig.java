package com.stackflov.config;

import com.stackflov.jwt.JwtAuthenticationEntryPoint;
import com.stackflov.jwt.JwtFilter;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.jwt.OAuth2AuthenticationSuccessHandler;
import com.stackflov.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;  // 빈 주입
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/auth/login", "/auth/register", "/auth/reissue",
                                "/auth/email/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/boards/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/boards").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/boards/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/boards/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/password").authenticated()
                        .requestMatchers("/users/me").authenticated()
                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/comments").authenticated()  // 댓글 작성은 인증된 회원만
                        .requestMatchers(HttpMethod.PUT, "/comments/**").authenticated()  // 댓글 수정은 작성자만
                        .requestMatchers(HttpMethod.DELETE, "/comments/**").authenticated()  // 댓글 삭제는 작성자만
                        .requestMatchers(HttpMethod.GET, "/comments/board/**").permitAll()  // 댓글 조회는 비회원도 가능

                        .requestMatchers(HttpMethod.POST, "/follows/follow").authenticated()  // 팔로우 추가
                        .requestMatchers(HttpMethod.DELETE, "/follows/unfollow").authenticated()  // 팔로우 취소
                        .requestMatchers(HttpMethod.GET, "/follows/followers/**").permitAll()  // 팔로워 조회 (비회원 가능)
                        .requestMatchers(HttpMethod.GET, "/follows/following/**").permitAll()  // 팔로잉 조회 (비회원 가능)

                        .requestMatchers(HttpMethod.POST, "/bookmarks").authenticated() // 북마크 추가 (인증된 회원만)
                        .requestMatchers(HttpMethod.DELETE, "/bookmarks").authenticated() // 북마크 삭제 (인증된 회원만)
                        .requestMatchers(HttpMethod.GET, "/bookmarks/my").authenticated() // 내 북마크 조회 (인증된 회원만)
                        .requestMatchers(HttpMethod.GET, "/bookmarks/board/**").permitAll() // 특정 게시글 북마크 여부 확인 (비회원도 가능)

                        .requestMatchers(HttpMethod.GET, "/profiles/**").authenticated() // 프로필 페이지

                        .requestMatchers(HttpMethod.POST, "/reports").authenticated()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(new JwtFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 사용자 정보 처리 서비스
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 인증 성공 핸들러
                )
                .build();
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


