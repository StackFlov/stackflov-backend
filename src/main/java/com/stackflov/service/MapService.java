package com.stackflov.service;

import com.stackflov.domain.*;
import com.stackflov.dto.*;
import com.stackflov.repository.*;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapService {

    private final ReviewRepository reviewRepository;
    private final S3Service s3Service;
    private final CommentRepository commentRepository;
    private final UserService userService;

    // 특정 위치에 리뷰 작성
    @Transactional
    public Long createReview(ReviewRequestDto dto, String userEmail, List<MultipartFile> images) {
        User user = userService.getValidUserByEmail(userEmail);

        Review review = Review.builder()
                .author(user)
                .title(dto.getTitle())
                .address(dto.getAddress())     // ✅ 여기
                .content(dto.getContent())
                .rating(dto.getRating())
                .build();

        Review savedReview = reviewRepository.save(review);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = s3Service.upload(image, "reviews");
                ReviewImage reviewImage = ReviewImage.builder()
                        .review(savedReview)
                        .imageUrl(imageUrl)
                        .build();
                savedReview.addReviewImage(reviewImage); // ✅ 연관관계 편의 메서드 사용
            }
        }
        return savedReview.getId();
    }


    @Transactional(readOnly = true)
    public Page<ReviewListResponseDto> getReview(Pageable pageable, @Nullable String requesterEmail) {
        Page<Review> page = reviewRepository.findByActiveTrue(pageable);
        return page.map(r -> ReviewListResponseDto.from(r, requesterEmail));
    }
    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto dto, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 수정할 수 있습니다.");
        }

        review.update(dto.getTitle(), dto.getAddress(), dto.getContent(), dto.getRating());
    }
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!review.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        review.deactivate(); // 👈 delete -> deactivate 로 변경
    }
    @Transactional
    public void deactivateReviewByAdmin(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 리뷰 비활성화
        review.deactivate();

        // 해당 리뷰에 달린 모든 댓글도 함께 비활성화
        commentRepository.findByReviewIdAndActiveTrue(reviewId).forEach(Comment::deactivate);
    }

    private ReviewSimpleResponseDto toSimpleDto(Review r) {
        return new ReviewSimpleResponseDto(
                r.getId(),
                r.getAddress(),
                r.getContent(),
                r.getRating(),
                r.getCreatedAt().toLocalDate()
        );
    }
}