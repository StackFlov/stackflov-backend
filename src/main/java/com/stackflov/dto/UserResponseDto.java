package com.stackflov.dto;

import com.stackflov.domain.Role;
import com.stackflov.domain.SocialType;
import com.stackflov.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {
    private final long id;
    private final String email;
    private final String nickname;
    private final String profileImage;
    private final SocialType socialType;
    private final int level;
    private final Role role;
    private final LocalDateTime createdAt;
    private final String phoneNumber;
    private final String address;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.socialType = user.getSocialType();
        this.level = user.getLevel();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.phoneNumber = user.getPhoneNumber();
        this.address = user.getAddress();
    }
}
