package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder // DTO에 @Builder가 있어야 Service에서 .builder() 사용 가능
public class BoardResponseDto {
    private Long id;
    private String title;
    private String content;
    private int category;
    private String authorEmail;
    private String authorNickname;
    private Long authorId;
    private List<String> imageUrls;
    private int viewCount;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // === 아래 두 줄 추가 ===
    private long likeCount; // 총 좋아요 수
    private boolean isLiked; // 현재 사용자가 좋아요를 눌렀는지 여부
}
