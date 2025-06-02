package com.stackflov.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class BoardUpdateRequestDto {
    private String title;
    private String content;
    private Integer category;
    private List<String> imageUrls;
}
