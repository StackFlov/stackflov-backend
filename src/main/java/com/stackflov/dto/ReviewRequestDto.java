package com.stackflov.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class ReviewRequestDto {
    private String title;
    private String content;
    private int rating;
    private List<MultipartFile> images;
}