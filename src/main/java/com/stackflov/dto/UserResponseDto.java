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
    private int exp;
    private final Role role;
    private final LocalDateTime createdAt;
    private final String phoneNumber;
    private final String address;
    private long followerCount;
    private long followingCount;

    public UserResponseDto(User user, String profileImageUrl) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImage = profileImageUrl;
        this.socialType = user.getSocialType();
        this.level = user.getLevel();
        this.exp = user.getExp();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.phoneNumber = user.getPhoneNumber();
        this.address = user.getAddress();
    }

    public void setCounts(long followerCount, long followingCount) {
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }
}
