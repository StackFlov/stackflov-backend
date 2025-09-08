package com.stackflov.dto;

import lombok.Getter;

@Getter
public class ReviewRequestDto {
    private String title;
    private String content;
    private int rating;
}