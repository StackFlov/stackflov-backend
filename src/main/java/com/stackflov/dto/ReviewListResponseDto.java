package com.stackflov.dto;

import com.stackflov.domain.Review;
import com.stackflov.domain.ReviewImage;
import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewListResponseDto {
    private Long id;
    private Long locationId;
    private String authorNickname;
    private String content;
    private Integer rating;           // 평점 필드명 맞춰서 변경
    private Integer likeCount;        // 없으면 0
    private Boolean mine;             // 요청자 == 작성자
    private LocalDateTime createdAt;
    private List<String> imageUrls;   // 이미지가 있다면

    public static ReviewListResponseDto from(Review r, @Nullable String requesterEmail) {
        boolean mine = requesterEmail != null && r.getAuthor() != null
                && requesterEmail.equals(r.getAuthor().getEmail());

        return ReviewListResponseDto.builder()
                .id(r.getId())
                .locationId(r.getLocation().getId())
                .authorNickname(r.getAuthor().getNickname())
                .content(r.getContent())
                .rating(r.getRating())
                .mine(mine)
                .createdAt(r.getCreatedAt())
                .imageUrls(
                        r.getReviewImages() == null ? List.of()
                                : r.getReviewImages().stream()
                                .map(ReviewImage::getImageUrl) // 필드명/접근자에 맞게 수정
                                .toList()
                )
                .build();
    }
}

