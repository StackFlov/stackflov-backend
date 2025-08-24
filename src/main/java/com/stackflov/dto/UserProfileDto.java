package com.stackflov.dto;

import com.stackflov.domain.User;
import lombok.Getter;

@Getter
public class UserProfileDto {
    private final Long userId;
    private final String nickname;
    private final String profileImage;
    private final int level;
    // (추후 확장) private final long followerCount;
    // (추후 확장) private final long followingCount;

    public UserProfileDto(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.level = user.getLevel();
    }
}