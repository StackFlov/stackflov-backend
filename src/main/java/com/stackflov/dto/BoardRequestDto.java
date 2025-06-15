package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class BoardRequestDto {

    private Integer category;
    private String title;
    private String content;
    private List<String> imageUrls; // 이미지 URL 목록

    @Builder
    public BoardRequestDto(String title, String content, Integer category, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrls = imageUrls;
    }
}