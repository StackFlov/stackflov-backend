package com.stackflov.dto;

import com.stackflov.domain.Role;
import com.stackflov.domain.User;
import lombok.Getter;

// 관리자에게 보여줄 사용자 정보 DTO
@Getter
public class AdminUserDto {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final Role role;
    private final boolean isActive;
    private final java.time.LocalDateTime createdAt;

    private final long boardCount;
    private final long commentCount;

    public AdminUserDto(User user, long boardCount, long commentCount) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.isActive = user.isActive();
        this.createdAt = user.getCreatedAt();
        this.boardCount = boardCount;
        this.commentCount = commentCount;
    }
}