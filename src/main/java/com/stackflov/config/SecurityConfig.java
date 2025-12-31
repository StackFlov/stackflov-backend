package com.stackflov.config;

import com.stackflov.jwt.JwtAuthenticationEntryPoint;
import com.stackflov.jwt.JwtFilter;
import com.stackflov.jwt.JwtProvider;
import com.stackflov.jwt.OAuth2AuthenticationSuccessHandler;
import com.stackflov.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.stackflov.repository.UserRepository;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final UserRepository userRepository;

    /**
     * (1) Swagger / Health 전용 완전 공개 체인
     * 이 체인에선 JwtFilter를 추가하지 않습니다.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerAndHealthChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/health",
                        "/swagger-ui.html",
                        "/v3/api-docs",             // ← 하위
                        "/v3/api-docs/",
                        "/v3/api-docs.yaml",
                        "/swagger-resources/**",
                        "/webjars/**", "/actuator/health"
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 활성화(전역 설정 사용)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        // 여기엔 JwtFilter 추가 금지!
        return http.build();
    }

    /**
     * (2) 애플리케이션 기본 체인
     */
    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개 엔드포인트 (여기에도 한 번 더 명시해 두면 안전)
                        .requestMatchers(
                                "/swagger-ui/**", "/v3/api-docs/**", "/health",
                                "/auth/login", "/auth/register", "/auth/reissue",
                                "/auth/email/**", "/ws/**",
                                "/oauth2/authorization/**", "/login/oauth2/code/**", "/oauth2/**", "/api/keys/kakao", "/test.html",
                                "/swagger-ui.html",
                                "/v3/api-docs","/v3/api-docs.yaml",
                                "/swagger-resources/**","/webjars/**","/actuator/health",
                                "/error",
                                "/test-chat.html", "/ws-stomp/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Boards
                        .requestMatchers(HttpMethod.GET, "/boards/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/boards/multipart").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/boards/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/boards/**").authenticated()

                        // Users
                        .requestMatchers(HttpMethod.PUT, "/users/password").authenticated()
                        .requestMatchers("/users/me").authenticated()
                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers("/auth/me").authenticated()

                        // Comments
                        .requestMatchers(HttpMethod.POST, "/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/comments/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/comments/board/**").permitAll()

                        // Follows
                        .requestMatchers(HttpMethod.POST, "/follows/follow").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/follows/unfollow").authenticated()
                        .requestMatchers(HttpMethod.GET, "/follows/followers/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/follows/following/**").permitAll()

                        // Bookmarks
                        .requestMatchers(HttpMethod.POST, "/bookmarks").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/bookmarks").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bookmarks/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bookmarks/board/**").permitAll()

                        // Profiles
                        .requestMatchers(HttpMethod.GET, "/profiles/**").authenticated()

                        // Reports
                        .requestMatchers(HttpMethod.POST, "/reports").authenticated()

                        // Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Feed
                        .requestMatchers("/feed").authenticated()

                        //내가 작성한 게시글,
                        .requestMatchers("/my/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/map/**").permitAll() // 지도 위치, 리뷰 조회 등
                        .requestMatchers(HttpMethod.GET, "/notifications/**").permitAll() // 알림 조회 (또는 authenticated)
                        .requestMatchers(HttpMethod.GET, "/comments/review/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/map/locations").authenticated() // 위치 생성
                        //.requestMatchers(HttpMethod.POST, "/map/locations/**/reviews").authenticated() // 리뷰 생성
                        .requestMatchers(HttpMethod.POST, "/map/locations/{locationId}/reviews").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/map/reviews/**").authenticated() // 리뷰 수정
                        .requestMatchers(HttpMethod.DELETE, "/map/reviews/**").authenticated() // 리뷰 삭제
                        .requestMatchers(HttpMethod.POST, "/notifications/**").authenticated() // 알림 읽음 처리 등

                        .requestMatchers(HttpMethod.GET, "/notices/**").permitAll()
                        .requestMatchers("/admin/notices/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/users/*/profile").permitAll()

                        // 나머지
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .addFilterBefore(new JwtFilter(jwtProvider, userRepository), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                .build();
    }

    /**
     * CORS 전역 설정
     * - 필요 시 프런트/도메인 추가
         */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프런트/도메인들 추가
        configuration.setAllowedOrigins(List.of(
                "https://app.stackflov.com",
                "http://localhost:3000",
                "https://stackflov.com",
                "https://www.stackflov.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
