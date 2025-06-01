package com.stackflov.dto;


import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
public class BoardRequestDto {

    private Integer category;
    private String title;
    private String content;
    private List<String> imageUrls; // 여러 이미지 URL (S3 링크 등)
    @Builder
    public BoardRequestDto(String title, String content, Integer category, List<String> imageUrls) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.imageUrls = imageUrls;
    }
}