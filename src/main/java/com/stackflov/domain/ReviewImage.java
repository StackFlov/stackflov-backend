package com.stackflov.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_id")
    private Long id;

    // 이 이미지가 연결된 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // 이미지 URL
    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Builder
    public ReviewImage(Review review, String imageUrl) {
        this.review = review;
        this.imageUrl = imageUrl;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}