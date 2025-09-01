package com.stackflov.dto;

import com.stackflov.domain.Board;
import com.stackflov.domain.BoardImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.stream.Collectors;

import java.util.List;

@Getter
@Builder // DTO에 @Builder가 있어야 Service에서 .builder() 사용 가능
@AllArgsConstructor
@NoArgsConstructor
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
    private long likeCount;
    private boolean isLiked;
}
