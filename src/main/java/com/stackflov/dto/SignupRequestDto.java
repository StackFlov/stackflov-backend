package com.stackflov.dto;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String email;            // 아이디 (이메일)
    private String password;         // 비밀번호
    private String profileImage;     // S3 이미지 URL
    private String nickname;         // 닉네임
    private SocialType socialType;   // 소셜 로그인 타입 (KAKAO, NAVER, GOOGLE)
    private String socialId;         // 소셜 고유 ID
    private int level;               // 등급
    private Role role;               // 역할 (USER, ADMIN, GUEST)
}
