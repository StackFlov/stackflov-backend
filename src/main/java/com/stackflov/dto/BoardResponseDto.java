package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BoardResponseDto {
    private Long id;
    private String title;
    private String content;
    private int category;
    private String authorEmail;
    private String authorNickname;       // ✅ 추가
    private Long authorId;               // ✅ 추가
    private List<String> imageUrls;
    private int viewCount;                    // ✅ 추가
    private java.time.LocalDateTime createdAt; // ✅ 추가
    private java.time.LocalDateTime updatedAt; // ✅ 추가
}