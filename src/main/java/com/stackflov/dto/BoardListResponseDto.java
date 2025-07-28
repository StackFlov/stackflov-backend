package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardListResponseDto {
    private Long id;
    private String title;
    private String authorEmail;
    private int category;
    private Long authorId;          // ✅ 추가
    private String authorNickname;  // ✅ 추가
    private String thumbnailUrl; // 이미지 중 첫 번째
    private int viewCount;               // ← 있어야 하고
    private LocalDateTime createdAt;     // ← 있어야 하고
    private LocalDateTime updatedAt;
    private boolean isBookmarked;
    private long likeCount;
    private boolean isLiked;
}