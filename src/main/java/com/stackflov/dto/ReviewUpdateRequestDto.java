package com.stackflov.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewUpdateRequestDto {

    @Schema(description = "리뷰 제목")
    private String title;

    @Schema(description = "주소(자유 형식)")
    private String address;

    @Schema(description = "리뷰 본문")
    private String content;

    @Schema(description = "평점(1~5 정수)")
    @Min(1) @Max(5)
    private Integer rating;

    @Schema(description = "삭제할 기존 이미지 ID 목록", example = "[3,7,8]")
    private List<Long> deleteImageIds;

    @Schema(description = "기존 이미지를 전부 교체할지 (true면 기존 전부 삭제 후 새 이미지로 대체)")
    private Boolean replaceAll;
}