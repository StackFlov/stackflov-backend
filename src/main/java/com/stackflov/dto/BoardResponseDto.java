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
    private List<String> imageUrls;
}