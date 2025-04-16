package com.stackflov.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // ✅ POST 막힘 방지
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/redis/**",
                                "/hello"
                        ).permitAll()
                        .anyRequest().permitAll() // 👈 일단 전부 허용으로 테스트 (최종 배포 전엔 authenticated로 다시)
                )
                .formLogin(form -> form.disable()) // 🔓 로그인 폼 끄기
                .httpBasic(basic -> basic.disable()) // 🔓 기본 인증 끄기
                .build();
    }
}

