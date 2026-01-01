package com.stackflov.dto;

import com.stackflov.domain.Role;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class UserProfileDetailResponseDto {
    // 1. 사용자 기본 정보
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private int level;
    private Role role;
    private boolean isFollowing; // 요청자가 이 사람을 팔로우 중인지

    // 2. 작성한 게시글 목록 (최근순)
    private List<BoardListResponseDto> boards;

    // 3. 작성한 리뷰 목록 (최근순)
    private List<ReviewListResponseDto> reviews;

    // 4. 팔로워 & 팔로잉 목록
    private List<UserResponseDto> followers;
    private List<UserResponseDto> following;
}