package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardListResponseDto {
    private Long id;
    private String title;
    private String authorEmail;
    private int category;
    private String thumbnailUrl; // 이미지 중 첫 번째
}