package com.stackflov.dto;

import com.stackflov.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewListResponseDto {
    private Long id;
    private String title;
    private String address;               // ✅ locationId → address
    private String authorNickname;
    private Long authorId;
    private String content;
    private Integer rating;
    private Integer likeCount;            // 없으면 0으로 세팅
    private Boolean mine;                 // 요청자 == 작성자
    private Boolean isLike;
    private Boolean isBookmarked;
    private LocalDateTime createdAt;
    private List<String> imageUrls;

    public static ReviewListResponseDto from(
            Review r,
            @org.springframework.lang.Nullable String requesterEmail,
            boolean isLike,
            int likeCount,
            List<String> imageUrls,
            boolean isBookmarked
    ) {
        boolean mine = requesterEmail != null
                && r.getAuthor() != null
                && requesterEmail.equals(r.getAuthor().getEmail());

        return ReviewListResponseDto.builder()
                .title(r.getTitle())
                .id(r.getId())
                .address(r.getAddress())                                  // ✅ 변경
                .authorNickname(r.getAuthor() != null ? r.getAuthor().getNickname() : null)
                .authorId(r.getAuthor() != null ? r.getAuthor().getId() : null)
                .content(r.getContent())
                .rating(r.getRating())
                .likeCount(likeCount)                                             // 👍 좋아요 집계 없으면 0
                .mine(mine)
                .isLike(isLike)
                .isBookmarked(isBookmarked)
                .createdAt(r.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}