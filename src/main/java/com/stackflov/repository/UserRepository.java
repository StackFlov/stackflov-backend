package com.stackflov.repository;

import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 자체 로그인용: 이메일로 사용자 조회 (활성/비활성 여부 관계없이)
    Optional<User> findByEmail(String email);

    // 활성 상태의 사용자만 이메일로 조회 (로그인 시 또는 활성 사용자만 필요한 경우 사용)
    Optional<User> findByEmailAndActiveTrue(String email);

    // 소셜 로그인용: 이메일 + 소셜 타입으로 조회 (추후 확장 시 사용)
    // Optional<User> findByEmailAndSocialType(String email, SocialType socialType);
}