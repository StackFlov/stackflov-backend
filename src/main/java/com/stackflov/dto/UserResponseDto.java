package com.stackflov.dto;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {
    private final String email;
    private final String nickname;
    private final String profileImage;
    private final SocialType socialType;
    private final int level;
    private final Role role;
    private final Boolean active; // <-- 이 부분을 추가해야 합니다.
    private final LocalDateTime createdAt;

    public UserResponseDto(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.socialType = user.getSocialType();
        this.level = user.getLevel();
        this.role = user.getRole();
        this.active = user.isActive(); // <-- user.isActive() 값을 가져와서 설정합니다.
        this.createdAt = user.getCreatedAt();
    }
}