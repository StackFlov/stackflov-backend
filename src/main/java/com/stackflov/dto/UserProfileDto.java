package com.stackflov.dto;

import com.stackflov.domain.User;
import lombok.Getter;

@Getter
public class UserProfileDto {
    private final Long userId;
    private final String nickname;
    private final String profileImage;
    private final int level;
    private final long followerCount;
    private final long followingCount;

    public UserProfileDto(User user, long followerCount, long followingCount) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
        this.level = user.getLevel();
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }
}