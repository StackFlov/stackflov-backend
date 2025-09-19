package com.stackflov.config;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import com.stackflov.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminAccountInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdminUser() {
        return args -> {
            String adminEmail = "admin@stackflov.com"; // ✅ 기본 관리자 이메일
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin1234")) // ✅ 기본 비번
                        .nickname("관리자")
                        .role(Role.ADMIN)
                        .socialType(SocialType.NONE)
                        .active(true)
                        .build();
                userRepository.save(admin);
                System.out.println("✅ 기본 관리자 계정 생성 완료: " + adminEmail + " / admin1234");
            }
        };
    }

    // 심볼릭 링크 무중단 배포 테스트
}
