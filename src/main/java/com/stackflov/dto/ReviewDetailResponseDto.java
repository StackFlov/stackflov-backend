// ReviewDetailResponseDto.java
package com.stackflov.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewDetailResponseDto {
    private Long id;

    private String title;
    private String address;
    private String content;
    private int rating;

    private Long authorId;
    private String authorEmail;
    private String authorNickname;
    private String authorProfileImageUrl;

    private List<String> imageUrls;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private long likeCount;
    private boolean isLiked;
}
