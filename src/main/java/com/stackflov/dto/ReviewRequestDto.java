package com.stackflov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class ReviewRequestDto {
    @Schema(description = "리뷰 제목", example = "원룸 거주 후기")
    private String title;

    @Schema(description = "주소(자유 형식)", example = "서울특별시 종로구")
    private String address;   // ✅ 추가

    @Schema(description = "리뷰 본문", example = "채광 좋고 방음은 보통이에요.")
    private String content;

    @Schema(description = "평점(1~5 정수)", example = "4", minimum = "1", maximum = "5")
    private int rating;

    private List<MultipartFile> images;
}