package com.stackflov.dto;

import com.stackflov.domain.Review;
import com.stackflov.domain.ReviewImage;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ReviewResponseDto {
    private final Long id;
    private final String title;
    private final String content;
    private final int rating;
    private final String authorNickname;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<String> imageUrls;

    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.title = review.getTitle();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.authorNickname = review.getAuthor().getNickname();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
        this.imageUrls = review.getReviewImages().stream() // 👈 이미지 URL 매핑
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList());
    }
}