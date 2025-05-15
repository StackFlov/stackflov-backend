package com.stackflov.repository;

import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 자체 로그인용: 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 소셜 로그인용: 이메일 + 소셜 타입으로 조회 (추후 확장 시 사용)
    Optional<User> findByEmailAndSocialType(String email, SocialType socialType);

}
