package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 회원 아이디 번호

    @Column(nullable = false, unique = true)
    private String email;  // 아이디 (이메일 형식)

    @Column(nullable = false)
    private String password;  // 비밀번호

    private String profileImage;  // S3에 저장된 프로필 이미지 URL

    private String nickname;  // 닉네임

    @Enumerated(EnumType.STRING)
    private SocialType socialType;  // 소셜 로그인 타입

    private String socialId;  // 소셜 서비스 고유 ID

    private int level;  // 등급

    @Enumerated(EnumType.STRING)
    private Role role;  // 권한 (user, admin, guest)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}